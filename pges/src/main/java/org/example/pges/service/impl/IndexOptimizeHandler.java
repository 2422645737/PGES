package org.example.pges.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.pges.constance.IndexNumConst;
import org.example.pges.constance.NodeCodeConst;
import org.example.pges.constance.NumConst;
import org.example.pges.dao.DocumentMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.po.Document;
import org.example.pges.entity.po.ESIndex;
import org.example.pges.utils.ESDataTypeUtils;
import org.example.pges.utils.ESDateUtils;
import org.example.pges.utils.IdGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: ES索引优化实现类
 * @fileName: ESIndexOptimizationServiceImpl
 * @author: wanghui
 * @createAt: 2024/08/20 04:46:04
 * @updateBy:
 * @copyright: 众阳健康
 */

@Service
public class IndexOptimizeHandler{
    @Resource
    private ESMapper esMapper;

    @Resource
    private DocumentMapper documentMapper;

    /**
     * 索引合并策略（针对同一词汇大量出现的优化）
     */

    public void indexMergeStrategy() {
        List<String> leastIndex = esMapper.getLeastIndex(IndexNumConst.MIN_LENGTH);
        if(leastIndex == null){
            return;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("code", NodeCodeConst.B1014);
        queryWrapper.in("word",leastIndex);
        List<ESIndex> list = esMapper.selectList(queryWrapper);
        //分组出相同word
        Map<String, List<ESIndex>> wordMap = list.stream().collect(Collectors.groupingBy(ESIndex::getWord));
        List<Long> removeIds = new ArrayList<>();
        List<ESIndex> updateList = new ArrayList<>();
        for(String word : wordMap.keySet()){
            List<ESIndex> esIndices = wordMap.get(word);
            //按照时间排序
            esIndices = esIndices.stream().sorted(Comparator.comparing(ESIndex::getBeginTime)).collect(Collectors.toList());
            mergeIndex(esIndices,removeIds);
            //最终得到的是需要进行update操作的数据
            updateList.addAll(esIndices);
        }
        esMapper.updateById(updateList);
        if(CollUtil.isNotEmpty(removeIds)){
            esMapper.deleteByIds(removeIds);
        }
    }

    /**
     * 索引拆分策略（对于某个分词连接大量id的优化）
     * 拆分前：word1 (2023-02-10 ~ 2023-02-20) 1,2,3,...(4000个)
     * 拆分后：word1 (2023-02-10 ~ 2023-02-15) 1,2,3,...(2000个)
     *       word1 (2023-02-15 ~ 2023-02-20) 2001,2002,2003,...(2000个)
     */


    public void indexSplitStragegy() {
        //1、查询出体积过大的索引
        List<Long> idList = esMapper.getMostIndex(IndexNumConst.MAX_LENGTH);
        if(CollUtil.isEmpty(idList)){
            return;
        }
        List<ESIndex> mostIndex = esMapper.selectBatchIds(idList);
        List<ESIndex> insertList = new ArrayList<>();
        List<Long> removeIdList = new ArrayList<>();
        for (ESIndex index : mostIndex) {
            int length = index.getIds().length;
            int count = (length / IndexNumConst.MAX_LENGTH) + 1;
            List<Date[]> dates = ESDateUtils.splitDate(index.getBeginTime(), index.getEndTime(), count);

            List<Document> entityList = new ArrayList<>();
            //数量超过数据库最大限制时，采用分段查询
            List<Long[]> ids = ESDataTypeUtils.splitArray(index.getIds(), IndexNumConst.DATABASE_MAX_LIMIT);
            for(int i = 0;i < ids.size();i++){
                List<Document> documents = documentMapper.searchTimeByOutEmrDetailIds(Arrays.stream(ids.get(i)).toList());
                entityList.addAll(documents);
            }
            for (Date[] date : dates) {
                List<Long> collect = entityList.stream()
                        .filter(e -> ESDateUtils.between(date[0], date[1], e.getCreateTime()))
                        .map(Document::getOutEmrDetailId)
                        .collect(Collectors.toList());
                if(CollUtil.isEmpty(collect)){
                    continue;
                }
                ESIndex indexPo = new ESIndex()
                        .setWord(index.getWord())
                        .setIds(ESDataTypeUtils.arrayListToArray(collect))
                        .setCode(index.getCode())
                        .setBeginTime(date[0])
                        .setEndTime(date[1])
                        .setId(IdGenerator.generateId());
                insertList.add(indexPo);
                entityList.removeAll(collect);
            }

            removeIdList.add(index.getId());
        }
        esMapper.insert(insertList);
        if(CollUtil.isNotEmpty(removeIdList)){
            esMapper.deleteByIds(removeIdList);
        }
    }

    /**
     * 合并数量较少的索引数据，减少空间占用
     * 合并前：1000,2000,3000,100,200,300
     *
     * 合并中：[1000,2000],[3000],[100,200,300]
     *
     * 合并后：3000,3000,600
     * @param esIndexList 待合并集合
     * @param removedIds 合并完成之后，需要删除的集合
     */

    private void mergeIndex(List<ESIndex> esIndexList, List<Long> removedIds){
        if(esIndexList.size() < NumConst.INT_2){
            return;
        }
        if(esIndexList.get(0).getIds().length >= IndexNumConst.MAX_LENGTH){
            esIndexList.remove(0);
            mergeIndex(esIndexList,removedIds);
        }
        ESIndex first = esIndexList.get(0);
        ESIndex second = esIndexList.get(1);
        if(first.getIds().length + second.getIds().length <= IndexNumConst.MAX_LENGTH){
            Long[] ids = ESDataTypeUtils.mergeArrayDistinct(first.getIds(), second.getIds());
            second.setIds(ids);
            second.setBeginTime(first.getBeginTime());
            removedIds.add(first.getId());
        }
        esIndexList.remove(0);
        mergeIndex(esIndexList,removedIds);
    }
}