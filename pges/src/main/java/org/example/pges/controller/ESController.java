package org.example.pges.controller;


import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParam;
import org.example.pges.entity.po.Document;
import org.example.pges.service.ESService;
import org.example.pges.service.impl.IndexOptimizeHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("es")
public class ESController {

    @Resource
    private ESService esService;

    @Resource
    private IndexOptimizeHandler indexOptimizeHandler;

    @PostMapping("/insert")
    public List<String> index(@RequestBody TextDTO textDTO) {
        return esService.insert(textDTO);
    }

    @PostMapping("/test")
    public void test() {
        indexOptimizeHandler.indexSplitStragegy();
    }

    @PostMapping("/process")
    public Object process() {
        return esService.process();
    }

    @PostMapping("/searchAll")
    public List<Document> searchAll(@RequestBody SearchParam searchParam) {
        return esService.searchAll(searchParam);
    }

    @PostMapping("/searchByPage")
    public List<Document> searchByPage(@RequestBody SearchParam searchParam) {
        return esService.searchByPage(searchParam);
    }

    @PostMapping("/optimize")
    public void optimize() {
        esService.optimize();
    }

}