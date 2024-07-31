package org.example.pges.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.constance.IndexNumConst;
import org.example.pges.constance.NodeCodeConst;
import org.example.pges.dao.BusinessMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.po.BusinessPO;
import org.example.pges.entity.po.ESIndexPo;
import org.example.pges.service.ESService;
import org.example.pges.utils.ESDateUtils;
import org.example.pges.utils.IdGenerator;
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
     * 处理数据库中的数据，生成索引
     * @return {@link List }<{@link String }>
     */
    @Override
    public Object process() {
        //获取待处理的病历数据
        List<BusinessPO> byOffset = businessMapper.findByOffset(IndexNumConst.MAX_COUNT);
        //存储key -> id集合
        Map<String,List<BusinessPO>> map = new HashMap<>(16);
        for (BusinessPO businessPO : byOffset) {
            String contentText = businessPO.getContentText();
            List<Word> seg = WordSegmenter.seg(contentText);
            for (Word word : seg) {
                List<BusinessPO> idList = map.getOrDefault(word.getText(), new ArrayList<>());
                idList.add(businessPO);
                map.put(word.getText(),idList);
            }
        }

        //更新索引数据库
        for(String key : map.keySet()){
            List<BusinessPO> list = map.get(key);
            //将list按照创建时间排序
            list = list.stream().sorted(Comparator.comparing(BusinessPO::getCreateTime)).toList();
            //判断当前key是否存在
            int hasIndex = esMapper.hasIndex(key, NodeCodeConst.B1014);
            if(hasIndex == 0){
                firstInsert(key, list);
            }else{
                //对于已经存在的key，需要判断数据库中的时间段是否包含当前时间段
                List<ESIndexPo> byWordAndCode = esMapper.getByWordAndCode(key, NodeCodeConst.B1014);

                for (ESIndexPo esIndexPo : byWordAndCode) {
                    //获取所有在当前时间段之内的数据
                    List<BusinessPO> currentDateIntervalBussinessData = list.stream().filter(e -> ESDateUtils.between(esIndexPo.getBeginTime(), esIndexPo.getEndTime(), e.getCreateTime())).collect(Collectors.toList());
                    if(CollUtil.isEmpty(currentDateIntervalBussinessData)){
                        continue;
                    }
                    Long[] ids = esIndexPo.getIds();
                    List<Long> businessIds = currentDateIntervalBussinessData.stream().map(BusinessPO::getOutEmrDetailId).collect(Collectors.toList());
                    Long[] newIds = new Long[ids == null ? currentDateIntervalBussinessData.size() : ids.length + currentDateIntervalBussinessData.size()];

                }
                //获取当前数据库中保存的id
                Long[] b1014 = (Long[]) esMapper.getIdsByWordAndCode(key, "B1014");
                //求交集
                Set<Long> set = new HashSet(list);
                set.addAll(Arrays.stream(b1014).collect(Collectors.toList()));
                esMapper.updateIndex(key,set.stream().toList().toArray(new Long[0]),"B1014");

            }
        }

        List<Long> collect = byOffset.stream().map(BusinessPO::getOutVisitRecordId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(collect)){
            businessMapper.updateEsFlag(collect);
        }
        return map;
    }

    /**
     * 首次插入数据
     * @param key
     * @param list
     */
    private void firstInsert(String key, List<BusinessPO> list) {
        //分割时间段
        List<Date[]> dateSegment = ESDateUtils.getDateSegment(list.get(0).getCreateTime(), list.get(list.size() - 1).getCreateTime());
        //将数据按照时间段分组
        Map<Date,List<Long>> businessMapByCreateTime = new HashMap<>();
        for (Date[] dates : dateSegment) {
            List<BusinessPO> businessPOS = list.stream().filter(e -> ESDateUtils.between(dates[0],dates[1],e.getCreateTime())).collect(Collectors.toList());
            ESIndexPo esIndexPo = new ESIndexPo();
            esIndexPo.setWord(key);
            esIndexPo.setIds(businessPOS.stream().map(BusinessPO::getOutEmrDetailId).toList().toArray(new Long[0]));
            esIndexPo.setBeginTime(dates[0]);
            esIndexPo.setEndTime(dates[1]);
            esIndexPo.setCode(NodeCodeConst.B1014);
            esIndexPo.setId(IdGenerator.generateId());
            esMapper.insertIndex(esIndexPo);
        }
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