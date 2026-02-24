package com.spring.app.ih.dining.controller;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.service.DiningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/dining") 
public class DiningController {

    private final DiningService diningService;

    public DiningController(DiningService diningService) {
        this.diningService = diningService;
    }

    @GetMapping("/all") // 주소: http://localhost:8080/dining/all
    public List<DiningDTO> getAllDining() {
        return diningService.getDiningList();
    }
}