package org.example.pges.service;

import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.po.BusinessPO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ESService {
    List<String> insert(TextDTO textDTO);

    Object process();

    List<BusinessPO> search(SearchParamDTO searchParamDTO);
}