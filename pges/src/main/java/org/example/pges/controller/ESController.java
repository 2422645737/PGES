package org.example.pges.controller;


import org.example.pges.entity.TextDTO;
import org.example.pges.entity.dto.SearchParamDTO;
import org.example.pges.entity.dto.WordSegementDTO;
import org.example.pges.entity.po.BusinessPO;
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

    @PostMapping("/test")
    public List<WordSegementDTO> test(@RequestBody String word) {
        return esService.test(word);
    }

    @PostMapping("/process")
    public Object process() {
        return esService.process();
    }

    @PostMapping("/searchAll")
    public List<BusinessPO> searchAll(@RequestBody SearchParamDTO searchParamDTO) {
        return esService.searchAll(searchParamDTO);
    }

    @PostMapping("/searchByPage")
    public List<BusinessPO> searchByPage(@RequestBody SearchParamDTO searchParamDTO) {
        return esService.searchByPage(searchParamDTO);
    }

    @PostMapping("/optimize")
    public void optimize() {
        esService.optimize();
    }
}