package com.spring.app.hk.reservation.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.app.hk.reservation.domain.ReservationDTO;
import com.spring.app.hk.reservation.service.ReservationService;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    
    @GetMapping("/form")
    public String reservationForm(
            @RequestParam("room_type_id") int room_type_id,
            @RequestParam("check_in") String check_in,
            @RequestParam("check_out") String check_out,
            Model model) {

        model.addAttribute("room_type_id", room_type_id);
        model.addAttribute("check_in", check_in);
        model.addAttribute("check_out", check_out);

        return "reservation/form";
    }
    
    @PostMapping("/create")
    public String createReservation(
            @RequestParam("room_type_id") int room_type_id,
            @RequestParam("check_in") String check_in,
            @RequestParam("check_out") String check_out) {

        ReservationDTO dto = new ReservationDTO();

        dto.setRoom_type_id(room_type_id);
        dto.setCheckin_date(LocalDate.parse(check_in));
        dto.setCheckout_date(LocalDate.parse(check_out));

        // 로그인 없으니까 고정
        dto.setMember_no(3);
        dto.setGuest_count(2);

        int result = reservationService.createReservation(dto);

        return "redirect:/reservation/success";
    }
}