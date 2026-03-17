package com.spring.app.ih.dining.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.ih.dining.model.DiningReservationDTO;
import com.spring.app.ih.dining.service.DiningService;

@Controller
@RequestMapping("/admin/dining")
public class AdminDiningController {

    @Autowired
    private DiningService diningservice;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, @RequestParam Map<String, Object> paraMap) {
        // 대시보드 상단 카운트
        Map<String, Object> counts = diningservice.getDashboardCounts();
        model.addAttribute("counts", counts);
        
        // 예약 목록 조회 
        List<DiningReservationDTO> resList = diningservice.getAllReservationsAdmin(paraMap);
        model.addAttribute("resList", resList);
        
        return "dining/admin/admin_dashboard"; 
    }

    // AJAX를 이용한 상태 변경 처리
    @PostMapping("/updateStatus")
    @ResponseBody
    public String updateStatus(@RequestParam("resId") Long resId, @RequestParam("status") String status) {
        String upperStatus = status.toUpperCase();
    	
    	int n = diningservice.updateReservationStatusAdmin(resId, upperStatus);
        return (n==1) ? "success" : "fail";
    }
    
    @PostMapping("/registerManual")
    @ResponseBody
    public String registerManual(DiningReservationDTO dto) {

        dto.setStatus("CONFIRMED"); 
        
        int n = diningservice.registerManual(dto);
        return (n == 1) ? "success" : "fail";
    }
    
    // 예약 상세 조회
    @GetMapping("/detail")
    @ResponseBody 
    public DiningReservationDTO getReservationDetail(@RequestParam("resId") Long resId) {
        
        DiningReservationDTO detail = diningservice.getReservationDetail(resId);
        System.out.println(">>> 가져온 데이터: " + detail);
        return detail; 
    }
    
    // 다이닝 예약 관리
    
    
    @GetMapping("/setting")
    public String settingPage(Model model) {

        List<Map<String, Object>> blockList = diningservice.getBlockList(); 
        List<Map<String, Object>> diningList = diningservice.getDiningList(); 
        
        model.addAttribute("blockList", blockList);
        model.addAttribute("diningList", diningList);
        
        return "dining/admin/control_setting";
    }
    
    // 차단 등록
    @PostMapping("/block/register")
    @ResponseBody
    public String registerBlock(@RequestParam Map<String, Object> params) {
        try {
            // 여기서 필요한 추가 데이터 가공 (예: 날짜 형식 맞추기 등)
        	diningservice.insertBlock(params);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    // 차단 해제
    @PostMapping("/block/delete")
    @ResponseBody
    public String deleteBlock(@RequestParam("blockId") Long blockId) {
    	diningservice.deleteBlock(blockId);
        return "success";
    }

}