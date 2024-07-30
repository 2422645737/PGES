package org.example.pges.service.impl;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.dao.BusinessMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.po.BusinessPO;
import org.example.pges.entity.po.ESIndexPo;
import org.example.pges.service.ESService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @fileName: ESServiceImpl
 * @author: wanghui
 * @createAt: 2024/07/23 09:32:28
 * @updateBy:
 * @copyright: 众阳健康
 */

@Service
public class ESServiceImpl implements ESService {
    @Resource
    ESMapper esMapper;

    @Resource
    BusinessMapper businessMapper;
    @Override
    public List<String> insert(TextDTO textDTO) {
        //查询库中当前不存在的索引
        List<Word> seg = WordSegmenter.seg(textDTO.getText());
        List<String> wordsSet = seg.stream().map(Word::getText).collect(Collectors.toList());
        return wordsSet;
    }

    /**
     * @return {@link List }<{@link String }>
     */
    @Override
    public Object process() {
        //一次读取100条数据
        int offset = 0;
        int limit = 100;
        List<BusinessPO> byOffset = businessMapper.findByOffset(limit, offset);
        Map<String,List<Long>> map = new HashMap<>(16);
        for (BusinessPO businessPO : byOffset) {
            String contentText = businessPO.getContentText();
            List<Word> seg = WordSegmenter.seg(contentText);
            for (Word word : seg) {
                List<Long> idList = map.getOrDefault(word.getText(), new ArrayList<>());
                idList.add(businessPO.getOutEmrDetailId());
                map.put(word.getText(),idList);
            }
        }
        //更新索引数据库
        for(String key : map.keySet()){
            List<Long> list = map.get(key);
            //判断当前key是否存在
            int hasIndex = esMapper.hasIndex(key, "B1014");
            if(hasIndex == 0){
                //不存在索引，则直接插入
                ESIndexPo esIndexPo = new ESIndexPo();
                esIndexPo.setWord(key);
                esIndexPo.setIds(list.toArray(new Long[0]));
                esIndexPo.setBeginTime(new Date());
                esIndexPo.setEndTime(new Date());
                esIndexPo.setCode("B1014");
                esMapper.insertIndex(esIndexPo);
                continue;
            }
            //获取当前数据库中保存的id
            Long[] b1014 = (Long[]) esMapper.getIdsByWordAndCode(key, "B1014");
            //求交集
            Set<Long> set = new HashSet(list);
            set.addAll(Arrays.stream(b1014).collect(Collectors.toList()));
            esMapper.updateIndex(key,set.stream().toList().toArray(new Long[0]),"B1014");
        }

        List<Long> collect = byOffset.stream().map(BusinessPO::getOutVisitRecordId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(collect)){
            businessMapper.updateEsFlag(collect);
        }
        return map;
    }

    /**
     * 实现关键词检索功能
     * @param searchParamDTO
     * @return {@link List }<{@link BusinessPO }>
     */

    @Override
    public List<BusinessPO> search(SearchParamDTO searchParamDTO) {
        List<Object> objects = esMapper.searchByParam(searchParamDTO);
        Set<Long> outEmrDetailsIds = new HashSet<>();
        if(!CollectionUtils.isEmpty(objects)){
            for (Object object : objects) {
                Long[] ids = (Long[]) object;
                outEmrDetailsIds.addAll(Arrays.stream(ids).collect(Collectors.toList()));
            }
        }
        //通过明细id直接检索数据
        List<BusinessPO> businessPOS = businessMapper.searchByOutEmrDetailIds(outEmrDetailsIds.stream().toList());
        return businessPOS;
    }
}