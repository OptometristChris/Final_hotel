package com.spring.app.hk.admin.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.hk.admin.hotel.service.HotelService;
import com.spring.app.jh.security.domain.Session_AdminDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/branch/hotel")
public class BranchHotelController {

    private final HotelService hotelService;
    
    // ==========================
    // 지점 관리자 호텔 관리 페이지
    // ==========================
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @GetMapping("/manage")
    public String branchHotelManage(Model model, HttpSession session){

        // 로그인 사용자 가져오기
    	Session_AdminDTO loginAdmin = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");

    	if(loginAdmin == null){
            return "redirect:/admin/login";
        }
    	
    	Integer adminNo = loginAdmin.getAdmin_no();
    	
        // 내 호텔 조회
        List<Map<String,Object>> hotelList =
        hotelService.getHotelListByManager(adminNo);

        // 호텔 없는 경우
        if(hotelList == null || hotelList.isEmpty()){
            model.addAttribute("status","NONE");
            return "hk/branch/hotel/manage";
        }

        Map<String,Object> hotel = hotelList.get(0);

        String approveStatus =
                (String)hotel.get("APPROVE_STATUS");
        
        model.addAttribute("status", approveStatus);
        model.addAttribute("hotelList", hotelList);

        return "hk/branch/hotel/manage";
    }

}