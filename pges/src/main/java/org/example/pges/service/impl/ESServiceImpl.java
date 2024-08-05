package org.example.pges.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.example.pges.constance.IndexNumConst;
import org.example.pges.constance.NodeCodeConst;
import org.example.pges.constance.NumConst;
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
import org.springframework.util.NumberUtils;

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
        if(CollUtil.isEmpty(byOffset)){
            return null;
        }
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
            //判断当前key是否存在
            int hasIndex = esMapper.hasIndex(key, NodeCodeConst.B1014);
            if(hasIndex == 0){
                firstInsert(key, list,insertList);
            }else{
                //对于已经存在的key，需要判断数据库中的时间段是否包含当前时间段
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("code",NodeCodeConst.B1014);
                queryWrapper.eq("word",key);
                List<ESIndexPo> byWordAndCode = esMapper.selectList(queryWrapper);
                
                //处理所有日期在当前时间段之内的数据
                for (ESIndexPo esIndexPo : byWordAndCode) {
                    List<BusinessPO> currentDateIntervalBussinessData = list.stream().filter(e -> ESDateUtils.between(esIndexPo.getBeginTime(), esIndexPo.getEndTime(), e.getCreateTime())).collect(Collectors.toList());
                    if(CollUtil.isEmpty(currentDateIntervalBussinessData)){
                        continue;
                    }
                    List<Long> businessIds = currentDateIntervalBussinessData.stream().map(BusinessPO::getOutEmrDetailId).collect(Collectors.toList());
                    Long[] newIds = ESDataTypeUtils.mergeArrayDistinct(esIndexPo.getIds(), ESDataTypeUtils.arrayListToArray(businessIds));
                    esIndexPo.setIds(newIds);
                    //插入到更新集合中
                    updateList.add(esIndexPo);
                    //排除掉已经处理的数据
                    list.removeAll(currentDateIntervalBussinessData);
                }
                //如果不存在新数据对应的时间段，则采取扩展策略，将其插入到数据库中
                if(CollUtil.isNotEmpty(list)){
                    firstInsert(key,list,insertList);
                }
            }
        }
        esMapper.insert(insertList);
        esMapper.updateById(updateList);
        List<Long> collect = byOffset.stream().map(BusinessPO::getOutEmrDetailId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(collect)){
            businessMapper.updateEsFlag(collect);
        }
        process();
        return null;
    }

    /**
     * 首次插入数据
     * @param key
     * @param list
     */
    private void firstInsert(String key, List<BusinessPO> list,List<ESIndexPo> result) {
        Map<Date[],List<Long>> map = new HashMap<>(16);
        //初始化时间Map,用于设置某个时间段内对应的所有业务id
        for (BusinessPO businessPO : list) {
            Date[] dateSegment = ESDateUtils.getDateSegment(businessPO.getCreateTime());
            if(null == dateSegment){
                continue;
            }
            List<Long> orDefault = map.getOrDefault(dateSegment, new ArrayList<>());
            orDefault.add(businessPO.getOutEmrDetailId());
            map.put(dateSegment,orDefault);
        }
        for(Date[] date : map.keySet()){
            ESIndexPo esIndexPo = new ESIndexPo();
            esIndexPo.setWord(key);
            esIndexPo.setIds(map.getOrDefault(date,new ArrayList<>()).toArray(new Long[0]));
            esIndexPo.setBeginTime(date[0]);
            esIndexPo.setEndTime(date[1]);
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
    public List<BusinessPO> searchAll(SearchParamDTO searchParamDTO) {
        //处理时间段
        List<BusinessPO> businessPOS = esMapper.searchByParam(searchParamDTO);
        return null;
    }

    /**
     * 分页查询
     * @param searchParamDTO
     * @return {@link List }<{@link BusinessPO }>
     */

    @Override
    public List<BusinessPO> searchByPage(SearchParamDTO searchParamDTO) {
        return null;
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

    @Override
    public void optimize() {
        //对于已经分好时间段的数组，如果占用长度过小，则进行合并优化
        List<String> leastIndex = esMapper.getLeastIndex();
        if(leastIndex == null){
            return;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",NodeCodeConst.B1014);
        queryWrapper.in("word",leastIndex);
        List<ESIndexPo> list = esMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(list)){
            return;
        }
        //分组出相同word
        Map<String, List<ESIndexPo>> wordMap = list.stream().collect(Collectors.groupingBy(ESIndexPo::getWord));
        List<Long> removeIds = new ArrayList<>();
        List<ESIndexPo> updateList = new ArrayList<>();
        for(String word : wordMap.keySet()){
            List<ESIndexPo> esIndexPos = wordMap.get(word);
            //按照时间排序
            esIndexPos = esIndexPos.stream().sorted(Comparator.comparing(ESIndexPo::getBeginTime)).collect(Collectors.toList());
            mergeIndex(esIndexPos,removeIds);
            //最终得到的是需要进行update操作的数据
            updateList.addAll(esIndexPos);
        }
        esMapper.updateById(updateList);
        if(CollUtil.isNotEmpty(removeIds)){
            esMapper.deleteByIds(removeIds);
        }
    }

    /**
     * 索引合并
     */

    private void mergeIndex(List<ESIndexPo> esIndexPoList,List<Long> removedIds){
        if(esIndexPoList.size() < NumConst.INT_2){
            return;
        }
        ESIndexPo first = esIndexPoList.get(0);
        ESIndexPo second = esIndexPoList.get(1);
        if(first.getIds().length + second.getIds().length < IndexNumConst.MAX_LENGTH){
            Long[] firstIds = first.getIds();
            Long[] secondIds = second.getIds();
            Long[] ids = ESDataTypeUtils.mergeArrayDistinct(firstIds, secondIds);
            second.setIds(ids);
            second.setBeginTime(first.getBeginTime());
            removedIds.add(first.getId());
            esIndexPoList.remove(0);
            mergeIndex(esIndexPoList,removedIds);
        }
    }

}