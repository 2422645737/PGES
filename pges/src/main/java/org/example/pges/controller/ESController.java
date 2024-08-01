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

    @PostMapping("/search")
    public List<BusinessPO> search(@RequestBody SearchParamDTO searchParamDTO) {
        return esService.search(searchParamDTO);
    }
}