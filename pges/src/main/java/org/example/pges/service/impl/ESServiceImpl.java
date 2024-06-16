package org.example.pges.service.impl;

import jakarta.annotation.Resource;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.dao.ESDao;
import org.example.pges.entity.TextDTO;
import org.example.pges.service.ESService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ESServiceImpl implements ESService {
    @Resource
    ESDao esDao;

    @Override
    public List<String> insert(TextDTO textDTO) {
        List<Word> words = WordSegmenter.seg(textDTO.getText());
        //分词结果去重
        List<String> wordsSet = new HashSet<>(words).stream().map(Word::getText).collect(Collectors.toList());
        System.out.println("分词后的数量" + wordsSet.size());
        //查询库中当前不存在的索引
        int i = esDao.hasIndex(wordsSet);
        return wordsSet;
    }
}
