package org.example.pges.controller;


import jakarta.annotation.Resource;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.example.pges.entity.TextDTO;
import org.example.pges.service.ESService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("es")
public class ESController {

    @Resource
    private ESService esService;
    @PostMapping("/insert")
    public List<String> index(@RequestBody TextDTO textDTO) {
        return esService.insert(textDTO);

    }
}
