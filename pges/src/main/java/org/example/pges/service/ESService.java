package org.example.pges.service;

import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParam;
import org.example.pges.entity.dto.WordSegement;
import org.example.pges.entity.po.Document;

import java.util.List;


public interface ESService {
    List<String> insert(TextDTO textDTO);

    /**
     * 数据处理
     * @return {@link Object }
     */

    Object process();

    /**
     * 检索全部
     * @param searchParam
     * @return {@link List }<{@link Document }>
     */

    List<Document> searchAll(SearchParam searchParam);
    /**
     * 分页检索
     * @param searchParam
     * @return {@link List }<{@link Document }>
     */

    List<Document> searchByPage(SearchParam searchParam);

    /**
     * 测试函数
     */

    List<WordSegement> test(String word);

    /**
     * 数据库优化
     */
    void optimize();
}