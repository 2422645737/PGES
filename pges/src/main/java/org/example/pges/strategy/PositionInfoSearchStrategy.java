package org.example.pges.strategy;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.dao.ESMapper;
import org.example.pges.entity.dto.SearchParam;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 基于位置信息的检索策略（Position Index）
 * @fileName: PositionInfoSearchStrategy
 * @author: wanghui
 * @createAt: 2024/09/12 05:08:25
 * @updateBy:
 * @copyright:
 *
 */
@Component
public class PositionInfoSearchStrategy implements SearchStrategy{
    @Resource
    ESMapper esMapper;

    @Override
    public void execute(SearchParam param) {
        String text = param.getText();

        List<Word> seg = WordSegmenter.seg(text);
    }
}