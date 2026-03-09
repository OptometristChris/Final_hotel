package com.spring.app.hk.reservation.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.app.hk.reservation.service.ReservationService;
import com.spring.app.jh.security.domain.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    
    // 예약 정보 입력 페이지 (객실 정보 조회, 숙박일 계산, 총 객실 기본요금 계산)
    @GetMapping("/form")
    public String reservationForm(
            @RequestParam("room_type_id") int room_type_id,
            @RequestParam("check_in") String check_in,
            @RequestParam("check_out") String check_out,
            Authentication auth, 
            Model model) {

    	// 객실 기본 정보 조회 (DAO → ROOM + HOTEL JOIN)
        Map<String, Object> roomInfo = reservationService.getRoomInfo(room_type_id);
    	
        // 로그인 사용자 이름 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        
        String name = userDetails.getMemberDto().getName();    
        String mobile = userDetails.getMemberDto().getMobile();
        String email = userDetails.getMemberDto().getEmail();
        Integer memberNo = userDetails.getMemberDto().getMemberNo(); // 중요
        
        // 숙박일 계산
        LocalDate inDate = LocalDate.parse(check_in);
        LocalDate outDate = LocalDate.parse(check_out);
        long nights = ChronoUnit.DAYS.between(inDate, outDate);
        
        // 기본 객실 요금 계산 (1박 요금 * 숙박일)
        int basePrice = ((Number) roomInfo.get("BASE_PRICE")).intValue();
        int maxCapacity = ((Number) roomInfo.get("MAX_CAPACITY")).intValue();
        int totalRoomPrice = basePrice * (int)nights;
               
        // 추가
        model.addAttribute("memberName", name);
        model.addAttribute("memberMobile", mobile);
        model.addAttribute("memberEmail", email);
        model.addAttribute("memberNo", memberNo); 
        
        model.addAttribute("room_type_id", room_type_id);
        model.addAttribute("check_in", check_in);
        model.addAttribute("check_out", check_out);
        model.addAttribute("nights", nights);
        
        model.addAttribute("hotel_name", roomInfo.get("HOTEL_NAME"));
        model.addAttribute("room_name", roomInfo.get("ROOM_NAME"));
        model.addAttribute("max_capacity", maxCapacity);
        model.addAttribute("base_price", totalRoomPrice);

        	
        return "hk/reservation/form";
    }
    
    
    // 예약 저장용
    @PostMapping("/save")
    public String saveReservation(@RequestParam Map<String, String> map) {

        reservationService.saveReservation(map);

        return "redirect:/reservation/success";
    }
    
    
    // 예약 완료 페이지
    @GetMapping("/complete")
    public String reservationComplete(@RequestParam("code") String code,
                                      Model model) {

    	System.out.println("넘어온 code = " + code);
    	
        Map<String,Object> reservation =
                reservationService.getReservationByCode(code);

        System.out.println("조회 결과 = " + reservation);
        
        model.addAttribute("reservation", reservation);

        return "hk/reservation/complete";
    }
    
}