package org.example.pges.service;

import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.dto.WordSegementDTO;
import org.example.pges.entity.po.BusinessPO;

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
     * @param searchParamDTO
     * @return {@link List }<{@link BusinessPO }>
     */

    List<BusinessPO> searchAll(SearchParamDTO searchParamDTO);
    /**
     * 分页检索
     * @param searchParamDTO
     * @return {@link List }<{@link BusinessPO }>
     */

    List<BusinessPO> searchByPage(SearchParamDTO searchParamDTO);

    /**
     * 测试函数
     */

    List<WordSegementDTO> test(String word);

    /**
     * 数据库优化
     */
    void optimize();
}