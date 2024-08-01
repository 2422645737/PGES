package org.example.pges.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.example.pges.constance.IndexNumConst;
import org.example.pges.constance.NodeCodeConst;
import org.example.pges.dao.BusinessMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.dto.WordSegementDTO;
import org.example.pges.entity.po.BusinessPO;
import org.example.pges.entity.po.ESIndexPo;
import org.example.pges.service.ESService;
import org.example.pges.utils.ESDataTypeUtils;
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
            List<String> seg = WordSegmenter.seg(contentText).stream().map(Word::getText).collect(Collectors.toList());
            seg = seg.stream().distinct().collect(Collectors.toList());
            for (String word : seg) {
                List<BusinessPO> idList = map.getOrDefault(word, new ArrayList<>());
                idList.add(businessPO);
                map.put(word,idList);
            }
        }

        //更新索引数据库
        List<ESIndexPo> insertList = new ArrayList<>();
        List<ESIndexPo> updateList = new ArrayList<>();
        for(String key : map.keySet()){
            List<BusinessPO> list = map.get(key);
            //将list按照创建时间排序
            list = list.stream().sorted(Comparator.comparing(BusinessPO::getCreateTime)).collect(Collectors.toList());
            //判断当前key是否存在
            int hasIndex = esMapper.hasIndex(key, NodeCodeConst.B1014);
            if(hasIndex == 0){
                firstInsert(key, list,insertList);
            }else{
                //对于已经存在的key，需要判断数据库中的时间段是否包含当前时间段
                List<ESIndexPo> byWordAndCode = esMapper.getByWordAndCode(key, NodeCodeConst.B1014);
                
                //处理所有日期在当前时间段之内的数据
                for (ESIndexPo esIndexPo : byWordAndCode) {
                    List<BusinessPO> currentDateIntervalBussinessData = list.stream().filter(e -> ESDateUtils.between(esIndexPo.getBeginTime(), esIndexPo.getEndTime(), e.getCreateTime())).collect(Collectors.toList());
                    if(CollUtil.isEmpty(currentDateIntervalBussinessData)){
                        continue;
                    }
                    List<Long> businessIds = currentDateIntervalBussinessData.stream().map(BusinessPO::getOutEmrDetailId).collect(Collectors.toList());
                    Long[] newIds = ESDataTypeUtils.mergeArray(esIndexPo.getIds(), ESDataTypeUtils.arrayListToArray(businessIds));
                    esIndexPo.setIds(newIds);
                    //排除掉已经处理的数据
                    if(null != currentDateIntervalBussinessData){
                        list.removeAll(currentDateIntervalBussinessData);
                    }
                }
                //对于某个关键词下的新数据，如果库中时间段包含他的时间，则直接进行扩展更新
                updateList.addAll(byWordAndCode);
                //如果不存在新数据对应的时间段，则采取扩展策略，将其插入到数据库中
                if(CollUtil.isNotEmpty(list)){
                    firstInsert(key,list,insertList);
                }
            }
        }
        esMapper.insert(insertList);
        esMapper.updateById(updateList);
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
    private void firstInsert(String key, List<BusinessPO> list,List<ESIndexPo> result) {
        //分割时间段
        List<Date[]> dateSegment = ESDateUtils.getDateSegment(list.get(0).getCreateTime(), list.get(list.size() - 1).getCreateTime());
        //将数据按照时间段分组
        for (Date[] dates : dateSegment) {
            List<BusinessPO> businessPOS = list.stream().filter(e -> ESDateUtils.between(dates[0],dates[1],e.getCreateTime())).collect(Collectors.toList());
            if(CollUtil.isEmpty(businessPOS)){
                continue;
            }
            ESIndexPo esIndexPo = new ESIndexPo();
            esIndexPo.setWord(key);
            esIndexPo.setIds(businessPOS.stream().map(BusinessPO::getOutEmrDetailId).toList().toArray(new Long[0]));
            esIndexPo.setBeginTime(dates[0]);
            esIndexPo.setEndTime(dates[1]);
            esIndexPo.setCode(NodeCodeConst.B1014);
            esIndexPo.setId(IdGenerator.generateId());
            result.add(esIndexPo);
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
        outEmrDetailsIds.stream().distinct().collect(Collectors.toList());
        //通过明细id直接检索数据
        List<BusinessPO> businessPOS = businessMapper.searchByOutEmrDetailIds(outEmrDetailsIds.stream().toList());
        return businessPOS;
    }

    @Override
    public List<WordSegementDTO> test(String word) {
        List<WordSegementDTO> result = new ArrayList<>();
        //
        List<Word> seg = WordSegmenter.seg(word, SegmentationAlgorithm.MaxNgramScore);
        WordSegementDTO MinimalWordCount = new WordSegementDTO();
        MinimalWordCount.setWordAlgorithm(SegmentationAlgorithm.MinimalWordCount.getDes());
        MinimalWordCount.setWords(seg.stream().map(Word::getText).collect(Collectors.toList()));


        result.add(MinimalWordCount);
        return result;
    }
}