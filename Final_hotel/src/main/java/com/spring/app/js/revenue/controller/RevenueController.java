package com.spring.app.js.revenue.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.js.revenue.service.RevenueService;

@Controller
@RequestMapping("/admin")
public class RevenueController {

    @Autowired
    private RevenueService revenueService;

    // 1. 처음 페이지 접속
    @GetMapping("/revenue")
    public String revenueMain(Model model, 
                               @RequestParam(value = "month", defaultValue = "") String month) {
        // 날짜 파라미터가 없으면 현재 년-월 설정
        if(month.equals("")) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        List<Map<String, String>> hotelList = revenueService.getHotelList();
        model.addAttribute("hotelList", hotelList);
        model.addAttribute("selectedMonth", month);
        return "js/revenue/revenue"; 
    }

    // 2. AJAX 요청 시 데이터 반환
    @GetMapping("/revenue/data")
    @ResponseBody
    public Map<String, Object> getRevenueData(@RequestParam(value = "month") String month,
                                              @RequestParam(value = "hotelName", defaultValue = "호텔 시엘") String hotelName) {
        Map<String, Object> data = new HashMap<>();
        
        // 맵퍼에서 요구하는 파라미터 구성 (month, hotelName)
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("month", month);
        paraMap.put("hotelName", hotelName); // HTML에서 전달받은 지점명 사용
        
        // 서비스 메서드 호출
        data.put("summary", revenueService.getRevenueSummary(paraMap));
        data.put("dailyTrends", revenueService.getDailyRevenue(paraMap));
        data.put("roomTypePortion", revenueService.getRoomTypePortion(paraMap));
        data.put("revenueList", revenueService.getMonthlyPaymentList(paraMap));
        
        return data; 
    }
}