package org.example.pges.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 分词结果
 * @fileName: WordSegementDTO
 * @author: wanghui
 * @createAt: 2024/08/01 05:42:23
 * @updateBy:
 */
@Data
public class WordSegementDTO {
    private String wordAlgorithm;

    private List<String> words;
}