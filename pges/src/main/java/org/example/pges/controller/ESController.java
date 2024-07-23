package org.example.pges.controller;


import org.example.pges.entity.TextDTO;
import org.example.pges.service.ESService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("es")
public class ESController {

    @Resource
    private ESService esService;
    @PostMapping("/insert")
    public List<String> index(@RequestBody TextDTO textDTO) {
        return esService.insert(textDTO);
    }

    @PostMapping("/process")
    public Object process() {
        return esService.process();
    }
}