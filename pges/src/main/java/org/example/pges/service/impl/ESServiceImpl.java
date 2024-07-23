package org.example.pges.service.impl;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.dao.BusinessMapper;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.TextDTO;
import org.example.pges.entity.po.BusinessPO;
import org.example.pges.service.ESService;
import org.springframework.stereotype.Service;

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
        List<Word> words = WordSegmenter.seg(textDTO.getText());
        //分词结果去重
        List<String> wordsSet = new HashSet<>(words).stream().map(Word::getText).collect(Collectors.toList());
        System.out.println("分词后的数量" + wordsSet.size());
        for (String s : wordsSet) {
            System.out.println(s);
        }
        //查询库中当前不存在的索引

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
        return map;
    }
}