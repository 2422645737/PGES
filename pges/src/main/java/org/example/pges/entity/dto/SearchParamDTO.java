package org.example.pges.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamDTO {
    public List<String> text;
}