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

    // 예약 캘린더 (객실 배정)
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @GetMapping("/calendar")
    public String reservationCalendar() {

        return "hk/admin/reservation/calendar";
    }

    // 예약 관리 페이지
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @GetMapping("/manage")
    public String reservationManage(Model model){

        List<Map<String,Object>> checkinList = reservationService.getTodayCheckinList();
        List<Map<String,Object>> checkoutList = reservationService.getTodayCheckoutList();
        List<Map<String,Object>> stayList = reservationService.getStayList();
        List<Map<String,Object>> checkoutCompleteList =
                reservationService.getCheckoutCompleteList();

        System.out.println("checkinList = " + checkinList);
        System.out.println("checkoutList = " + checkoutList);
        
        model.addAttribute("checkinList", checkinList);
        model.addAttribute("checkoutList", checkoutList);
        model.addAttribute("stayList", stayList);
        model.addAttribute("checkoutCompleteList", checkoutCompleteList);

        return "hk/admin/reservation/reservationManage";
    }

    // 체크인 처리
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @PostMapping("/checkin")
    public String checkin(@RequestParam("reservationId") int reservationId){

        reservationService.checkinReservation(reservationId);

        return "redirect:/admin/reservation/manage";
    }

    // 체크아웃 처리
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @PostMapping("/checkout")
    public String checkout(@RequestParam("reservationId") int reservationId){

        reservationService.checkoutReservation(reservationId);

        return "redirect:/admin/reservation/manage";
    }

}