package org.example.pges.service;

import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.dto.WordSegementDTO;
import org.example.pges.entity.po.BusinessPO;

import java.util.List;


public interface ESService {
    List<String> insert(TextDTO textDTO);

    Object process();

    List<BusinessPO> search(SearchParamDTO searchParamDTO);

    /**
     * 测试函数
     */

    List<WordSegementDTO> test(String word);
}