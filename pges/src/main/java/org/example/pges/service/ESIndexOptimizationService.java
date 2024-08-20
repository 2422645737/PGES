package org.example.pges.service;

import org.springframework.stereotype.Service;

/**
 * @description: ES索引优化接口
 * @fileName: ESIndexOptimizationService
 * @author: wanghui
 * @createAt: 2024/08/20 04:42:12
 * @updateBy:
 */
public interface ESIndexOptimizationService {

    /**
     * 索引合并策略（针对同一词汇大量出现的优化）
     */

    void indexMergeStrategy();

    /**
     * 索引拆分策略（对于某个分词连接大量id的优化）
     */

    void indexSplitStragegy();
}