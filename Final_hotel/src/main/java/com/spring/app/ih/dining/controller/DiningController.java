package com.spring.app.ih.dining.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.service.DiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller // 뷰 페이지 없이 데이터를 직접 출력해줍니다.
@RequestMapping("/dining")
public class DiningController {

    @Autowired
    private DiningService service;

    @GetMapping("/all") // http://localhost:9081/final_hotel/dining/all
    public String getAll(
        @RequestParam(value = "hotel_id", required = false) Integer hotel_id,
        @RequestParam(value = "d_type", required = false) String d_type,
        Model model) {

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("hotel_id", hotel_id);
        paraMap.put("d_type", d_type);

    	// System.out.println(">>> 넘어온 파라미터 : " + paraMap);
    	
        List<DiningDTO> diningList = service.getDiningList(paraMap);
        
        model.addAttribute("diningList", diningList);
        model.addAttribute("selectedHotel", paraMap.get("hotel_id"));
        model.addAttribute("selectedType", paraMap.get("d_type"));
        
        return "dining/all";
    }
    
    
    @GetMapping("/detail/{dining_id}") // http://localhost:9081/final_hotel/dining/detail/1
    public String diningDetail(@PathVariable("dining_id") int dining_id, Model model) {
        
        // 한 개의 식당 정보 가져오기
        DiningDTO dining = service.getDiningDetail(dining_id);
        model.addAttribute("dining", dining);
        return "dining/detail"; // templates/dining/detail.html
    }
    
    
    
    
}