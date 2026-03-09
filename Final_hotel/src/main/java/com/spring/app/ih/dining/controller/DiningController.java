package com.spring.app.ih.dining.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;
import com.spring.app.ih.dining.service.DiningService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller // 뷰 페이지 없이 데이터를 직접 출력해줍니다.
@RequestMapping("/dining")
public class DiningController {

    @Autowired
    private DiningService diningService;


    @GetMapping("/all") // http://localhost:9081/final_hotel/dining/all
    public String getAll(
        @RequestParam(value = "hotel_id", required = false) Integer hotel_id,
        @RequestParam(value = "d_type", required = false) String d_type,
        Model model) {

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("hotel_id", hotel_id);
        paraMap.put("d_type", d_type);

    	// System.out.println(">>> 넘어온 파라미터 : " + paraMap);
    	
        List<DiningDTO> diningList = diningService.getDiningList(paraMap);
        
        model.addAttribute("diningList", diningList);
        model.addAttribute("selectedHotel", paraMap.get("hotel_id"));
        model.addAttribute("selectedType", paraMap.get("d_type"));
        
        return "dining/all";
    }
    
    
    @GetMapping("/detail/{dining_id}") // http://localhost:9081/final_hotel/dining/detail/1
    public String diningDetail(@PathVariable("dining_id") int dining_id, Model model) {
        
        // 한 개의 식당 정보 가져오기
        DiningDTO dining = diningService.getDiningDetail(dining_id);
        model.addAttribute("dining", dining);
        return "dining/detail"; // templates/dining/detail.html
    }
    

    @GetMapping("/reserve/{dining_id}")
    public String showReservationPage(@PathVariable("dining_id") int dining_id, Model model) {
        DiningDTO dining = diningService.getDiningDetail(dining_id);
        
        String[] allTimes = dining.getAvailable_times().split(",");
        
        List<String> lunchSlots = new ArrayList<>();
        List<String> dinnerSlots = new ArrayList<>();

        for (String time : allTimes) {
            int hour = Integer.parseInt(time.split(":")[0]);
            if (hour < 15) { // 15시 이전은 Lunch
                lunchSlots.add(time);
            } else {         // 15시 이후는 Dinner
                dinnerSlots.add(time);
            }
        }

        model.addAttribute("dining", dining);
        model.addAttribute("lunchSlots", lunchSlots);
        model.addAttribute("dinnerSlots", dinnerSlots);
        
        return "dining/reservation";
    }
    
    

    
    @PostMapping("/reserve/confirm")
    public String reserveConfirm(DiningReservationDTO reservationDTO, HttpSession session, Model model) {

        session.setAttribute("tempReservation", reservationDTO);
        
        int adultPrice = 20000;
        int childPrice = 10000;
        int totalAmount = (reservationDTO.getAdultCount() * adultPrice) + 
                           (reservationDTO.getChildCount() * childPrice);

        model.addAttribute("res", reservationDTO);
        model.addAttribute("totalAmount", totalAmount); 
        
        return "dining/reserve_confirm";  
    }
    
    
    
    @GetMapping("/reserve/complete")
    public String reserveComplete(
            @RequestParam("imp_uid") String impUid,
            @RequestParam("diningId") Long diningId,
            HttpSession session, 
            Model model) {

        DiningReservationDTO reservationDTO = (DiningReservationDTO) session.getAttribute("tempReservation");

        if (reservationDTO == null) {
            return "common/error"; // "세션이 만료되었습니다. 다시 시도해주세요."
        }
        reservationDTO.setDiningId(diningId);
        
        int result = diningService.registerReservation(reservationDTO, impUid);
        
        if(result > 0) {
            session.removeAttribute("tempReservation");
            
            model.addAttribute("res", reservationDTO);
            return "dining/reserve_success"; 
        } else {
            return "common/error";
        }
    }

    // 예약 완료 페이지 매핑
    @GetMapping("/reserve_success")
    public String reserveSuccess(@RequestParam("resNo") String resNo, Model model) {
        model.addAttribute("resNo", resNo);
        return "dining/reserve_success";
    }
    
}