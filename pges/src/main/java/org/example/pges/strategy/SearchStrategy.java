package org.example.pges.strategy;


import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.entity.dto.SearchParam;

import java.util.List;

/**
 * @description: 检索策略
 * @fileName: SearchStrategy
 * @author: wanghui
 * @createAt: 2024/09/12 05:02:49
 * @updateBy:
 * @copyright:
 * 目前最优的检索策略就是基于位置信息的检索策略
 */

public interface SearchStrategy {


    void execute(SearchParam param);

    /**
     * 参数预处理函数
     */

    default void paramPreProcess(SearchParam param){
        List<Word> seg = WordSegmenter.seg(param.getText());

    }
}