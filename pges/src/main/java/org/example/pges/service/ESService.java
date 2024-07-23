package org.example.pges.service;

import org.example.pges.entity.TextDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ESService {
    List<String> insert(TextDTO textDTO);

    Object process();
}