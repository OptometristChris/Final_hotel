package com.spring.app.hk.admin.reservation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.spring.app.hk.admin.reservation.service.AdminReservationService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reservation")
public class AdminReservationController {

	private final AdminReservationService reservationService;

	// ======= 지점 관리자 ========= //
	// 예약 캘린더 (객실 배정)
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@GetMapping("/calendar")
	public String reservationCalendar() {

		return "hk/admin/reservation/calendar";
	}

	// 예약 관리 페이지
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@GetMapping("/manage")
	public String reservationManage(Model model) {

		List<Map<String, Object>> checkinList = reservationService.getTodayCheckinList();
		List<Map<String, Object>> checkoutList = reservationService.getTodayCheckoutList();
		List<Map<String, Object>> stayList = reservationService.getStayList();
		List<Map<String, Object>> checkoutCompleteList = reservationService.getCheckoutCompleteList();

		// ===== KPI 계산 =====
	    int todayCheckinCount = checkinList.size();
	    int todayCheckoutCount = checkoutList.size();
	    int stayCount = stayList.size();
		
	    // 점유율 계산 (예: 총 객실수 기준)
	    int totalRoomCount = 100; // 필요하면 서비스에서 가져와도 됨
	    int occupancyRate = 0;
	    
	    if(totalRoomCount > 0) {
	        occupancyRate = (int)(((double) stayCount / totalRoomCount) * 100);
	    }
	    
	    // 바
	    int checkinProgress = 0;

	    if(todayCheckinCount > 0){
	        checkinProgress = (int)(((double) stayCount / todayCheckinCount) * 100);
	    }

	    
		//System.out.println("checkinList = " + checkinList);
		//System.out.println("checkoutList = " + checkoutList);

	    // ===== 리스트 =====
	    model.addAttribute("checkinList", checkinList);
	    model.addAttribute("checkoutList", checkoutList);
	    model.addAttribute("stayList", stayList);
	    model.addAttribute("checkoutCompleteList", checkoutCompleteList);

	    // ===== KPI =====
	    model.addAttribute("todayCheckinCount", todayCheckinCount);
	    model.addAttribute("todayCheckoutCount", todayCheckoutCount);
	    model.addAttribute("stayCount", stayCount);
	    model.addAttribute("occupancyRate", occupancyRate);
	    
	    // === 바 ===
	    model.addAttribute("checkinProgress", checkinProgress);

		return "hk/admin/reservation/reservationManage";
	}

	// 체크인 처리
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@PostMapping("/checkin")
	public String checkin(@RequestParam("reservationId") int reservationId) {

		reservationService.checkinReservation(reservationId);

		return "redirect:/admin/reservation/manage";
	}

	// 체크아웃 처리
	@PreAuthorize("hasRole('ADMIN_BRANCH')")
	@PostMapping("/checkout")
	public String checkout(@RequestParam("reservationId") int reservationId) {

		reservationService.checkoutReservation(reservationId);

		return "redirect:/admin/reservation/manage";
	}

	// ======= 총괄 관리자 ========= //
	// 전체 객실 예약 리스트 조회
	@PreAuthorize("hasRole('ADMIN_HQ')")
	@GetMapping("/list")
	public String adminReservationList(Model model) {

	    List<Map<String,Object>> reservationList =
	            reservationService.selectAdminReservationList();

	    model.addAttribute("reservationList", reservationList);

	    return "hk/admin/reservation/reservationList";
	}

}