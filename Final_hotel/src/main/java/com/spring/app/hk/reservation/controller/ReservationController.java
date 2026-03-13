package com.spring.app.hk.reservation.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
            @RequestParam("room_price") int room_price,
            @RequestParam(value="currency", required=false) String currency,
            @RequestParam(value="tax", required=false) Boolean tax,
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
        LocalDate inDate = LocalDate.parse(check_in.trim());
        LocalDate outDate = LocalDate.parse(check_out.trim());
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
        
        model.addAttribute("room_price", room_price);   // 추가 (가격 합)
        model.addAttribute("currency", currency);       // 추가 (환율, 세금)
        model.addAttribute("tax", tax);
        	
        return "hk/reservation/form";
    }
    
    
    // 예약 저장용
    // ReservationController.java 내 수정

    @PostMapping("/save")
    public String saveReservation(@RequestParam Map<String, String> map, Model model) {
        
        // 1. 넘어온 결제 정보 확인 (디버깅용)
        System.out.println("결제 성공 UID: " + map.get("payment_imp_uid"));
        System.out.println("최종 결제 금액: " + map.get("applied_price"));
        System.out.println("프로모션 ID: " + map.get("promotion_id"));

        // 2. 서비스 단에서 예약 정보 저장
        // (이때 결제 테이블에도 insert하거나, 예약 테이블에 imp_uid를 같이 저장해야 합니다.)
        String reservationCode = reservationService.saveReservation(map);

        // 3. 성공 시 완료 페이지로 이동 (기존 complete 경로로 리다이렉트)
        return "redirect:/reservation/complete?code=" + reservationCode;
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
    
	 // =======================================================
	 // 마이페이지 : 로그인 회원의 예약 목록 조회
	 // =======================================================
	 @GetMapping("/mypage")
	 public String myReservationList(Authentication auth, Model model) {
	
	     // 로그인 사용자 정보 가져오기
	     CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
	     int memberNo = userDetails.getMemberDto().getMemberNo();
	
	     // 예약 목록 조회
	     List<Map<String,Object>> reservationList =
	             reservationService.selectMyReservationList(memberNo);
	
	     // 화면에 전달
	     model.addAttribute("reservationList", reservationList);
	
	     return "hk/reservation/reservationList";
	 }
	 
	// ======================================
	// 예약 취소
	// ======================================
	@PostMapping("/cancel")
	@ResponseBody
	public String cancelReservation(@RequestParam("reservation_id") long reservationId){

	    int result = reservationService.cancelReservation(reservationId);

	    if(result == 1){
	        return "success";
	    }

	    return "fail";
	}
	
	
	// 예약 취소 내역 조회하기
	@GetMapping("/mypage/cancel")
	@ResponseBody
	public List<Map<String,Object>> myCancelReservationList(Authentication auth){

	    // 로그인 사용자 정보
	    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
	    int memberNo = userDetails.getMemberDto().getMemberNo();

	    // 취소 내역 조회
	    return reservationService.selectMyCancelReservationList(memberNo);

	}
	    
    
}