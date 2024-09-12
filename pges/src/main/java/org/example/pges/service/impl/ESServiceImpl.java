package org.example.pges.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.segmentation.WordRefiner;
import org.example.pges.constance.IndexNumConst;
import org.example.pges.constance.NodeCodeConst;
import org.example.pges.constance.NumConst;
import org.example.pges.dao.DocumentMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParam;
import org.example.pges.entity.dto.WordSegement;
import org.example.pges.entity.po.Document;
import org.example.pges.entity.po.ESIndex;
import org.example.pges.entity.po.ItemNode;
import org.example.pges.service.ESService;
import org.example.pges.utils.ESDataTypeUtils;
import org.example.pges.utils.ESDateUtils;
import org.example.pges.utils.IdGenerator;
import org.example.pges.utils.IndexBuilder;
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
    DocumentMapper documentMapper;

    @Override
    public List<String> insert(TextDTO textDTO) {
        //查询库中当前不存在的索引
        List<Word> seg = WordSegmenter.seg(textDTO.getText(),SegmentationAlgorithm.MaxNgramScore);
        List<Word> refine = WordRefiner.refine(seg);
        List<String> wordsSet = refine.stream().map(Word::getText).collect(Collectors.toList());
        return wordsSet;
    }

    /**
     * 处理数据库中的数据，生成索引
     * @return {@link List }<{@link String }>
     */
    @Override
    public Object process() {
        //获取待处理的病历数据
        List<Document> byOffset = documentMapper.findByOffset(IndexNumConst.MAX_COUNT);
        if(CollUtil.isEmpty(byOffset)){
            return null;
        }
        //存储key -> id集合
        Map<String,List<Document>> wordToDocsMap = new HashMap<>(16);

        for (Document document : byOffset) {
            String contentText = document.getContentText();
            List<String> seg = WordSegmenter.seg(contentText).stream().map(Word::getText).distinct().toList();
            for (String word : seg) {
                List<Document> idList = wordToDocsMap.getOrDefault(word, new ArrayList<>());
                idList.add(document);
                wordToDocsMap.put(word,idList);
            }
        }

        //更新索引数据库
        List<ESIndex> insertList = new ArrayList<>();
        List<ESIndex> updateList = new ArrayList<>();

        for(String word : wordToDocsMap.keySet()){
            List<Document> list = wordToDocsMap.get(word);
            //判断当前key是否存在
            int hasIndex = esMapper.hasIndex(word, NodeCodeConst.B1014);
            if(hasIndex == 0){
                firstInsert(word, list,insertList);
            }else{
                //对于已经存在的key，需要判断数据库中的时间段是否包含当前时间段
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("code",NodeCodeConst.B1014);
                queryWrapper.eq("word",word);
                List<ESIndex> byWordAndCode = esMapper.selectList(queryWrapper);
                
                //处理所有日期在当前时间段之内的数据
                for (ESIndex esIndex : byWordAndCode) {
                    List<Document> currentDateIntervalBussinessData = list.stream()
                            .filter(e -> ESDateUtils.between(esIndex.getBeginTime(), esIndex.getEndTime(), e.getCreateTime()))
                            .collect(Collectors.toList());
                    if(CollUtil.isEmpty(currentDateIntervalBussinessData)){
                        continue;
                    }
                    List<ItemNode> indexNodeList = esIndex.getIndexNodeList();
                    if(indexNodeList != null){
                        indexNodeList.addAll(IndexBuilder.buildIndexNodeList(word,currentDateIntervalBussinessData));
                    }else{
                        indexNodeList = IndexBuilder.buildIndexNodeList(word,currentDateIntervalBussinessData);
                    }
                    esIndex.setIndexNodeList(indexNodeList);
                    //插入到更新集合中
                    updateList.add(esIndex);
                    //排除掉已经处理的数据
                    list.removeAll(currentDateIntervalBussinessData);
                }
                //如果不存在新数据对应的时间段，则采取扩展策略，将其插入到数据库中
                if(CollUtil.isNotEmpty(list)){
                    firstInsert(word,list,insertList);
                }
            }
        }
        esMapper.insert(insertList);
        esMapper.updateById(updateList);
        List<Long> collect = byOffset.stream().map(Document::getOutEmrDetailId).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(collect)){
            documentMapper.updateEsFlag(collect);
        }
//        process();
        return null;
    }

    /**
     * 首次插入数据
     * @param key 关键词
     * @param list 文档集合
     * @param insertList 最终插入集合
     */
    private void firstInsert(String key, List<Document> list, List<ESIndex> insertList) {
        Map<Date[],List<Document>> map = new HashMap<>(16);
        //初始化时间Map,用于设置某个时间段内对应的所有业务id
        for (Document document : list) {
            Date[] dateSegment = ESDateUtils.getDateSegment(document.getCreateTime());
            if(null == dateSegment){
                continue;
            }
            List<Document> orDefault = map.getOrDefault(dateSegment, new ArrayList<>());
            orDefault.add(document);
            map.put(dateSegment,orDefault);
        }
        for(Date[] date : map.keySet()){
            List<Document> documents = map.getOrDefault(date, new ArrayList<>());
            ESIndex esIndex = new ESIndex().setWord(key)
                    .setBeginTime(date[0])
                    .setEndTime(date[1])
                    .setCode(NodeCodeConst.B1014)
                    .setId(IdGenerator.generateId())
                    .setIndexNodeList(IndexBuilder.buildIndexNodeList(key, documents));
            insertList.add(esIndex);
        }
    }

    /**
     * 实现关键词检索功能
     * @param searchParam
     * @return {@link List }<{@link Document }>
     */

    @Override
    public List<Document> searchAll(SearchParam searchParam) {
        //检索条件处理
        //searchParamDTO.setText(splitSearchWord(searchParamDTO.getText()));
        //处理时间段
        List<Document> documents = esMapper.searchByParam(searchParam);
        return documents;
    }

    /**
     * 分页查询
     * @param searchParam
     * @return {@link List }<{@link Document }>
     */

    @Override
    public List<Document> searchByPage(SearchParam searchParam) {
        return null;
    }

    @Override
    public List<WordSegement> test(String word) {
        List<WordSegement> result = new ArrayList<>();
        return result;
    }

    @Override
    public void optimize() {
        //对于已经分好时间段的数组，如果占用长度过小，则进行合并优化
        List<String> leastIndex = esMapper.getLeastIndex(IndexNumConst.MIN_LENGTH);
        if(CollUtil.isEmpty(leastIndex)){
            return;
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("code",NodeCodeConst.B1014);
        queryWrapper.in("word",leastIndex);
        List<ESIndex> list = esMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(list)){
            return;
        }
        //分组出相同word
        Map<String, List<ESIndex>> wordMap = list.stream().collect(Collectors.groupingBy(ESIndex::getWord));
        List<Long> removeIds = new ArrayList<>();
        List<ESIndex> updateList = new ArrayList<>();
        for(String word : wordMap.keySet()){
            List<ESIndex> esIndices = wordMap.get(word);
            esIndices = esIndices.stream()
                    .sorted(Comparator.comparing(ESIndex::getBeginTime))
                    .collect(Collectors.toList());
            mergeIndex(esIndices,removeIds);
            updateList.addAll(esIndices);
        }
        esMapper.updateById(updateList);
        if(CollUtil.isNotEmpty(removeIds)){
            esMapper.deleteByIds(removeIds);
        }
    }

    /**
     * 索引合并
     */

    private void mergeIndex(List<ESIndex> esIndexList, List<Long> removedIds){
        if(esIndexList.size() < NumConst.INT_2){
            return;
        }
        ESIndex first = esIndexList.get(0);
        ESIndex second = esIndexList.get(1);
        if(first.getIds().length + second.getIds().length < IndexNumConst.MAX_LENGTH){
            Long[] firstIds = first.getIds();
            Long[] secondIds = second.getIds();
            Long[] ids = ESDataTypeUtils.mergeArrayDistinct(firstIds, secondIds);
            second.setIds(ids).setBeginTime(first.getBeginTime());
            removedIds.add(first.getId());
            esIndexList.remove(0);
            mergeIndex(esIndexList,removedIds);
        }
    }

    /**
     * 对检索条件进行分词处理
     * @param wordList
     */

    private List<String> splitSearchWord(List<String> wordList){
        if(CollUtil.isEmpty(wordList)){
            return null;
        }
        Set<String> wordSet = new HashSet<>();
        wordList.forEach(word -> {
            List<Word> seg = WordSegmenter.seg(word);
            wordSet.addAll(seg.stream().map(Word::getText).collect(Collectors.toList()));
        });
        //取出数据库中与其关联度最高的文本

        return wordSet.stream().toList();
    }


}