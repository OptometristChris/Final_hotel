package com.spring.app.hk.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.app.hk.reservation.service.ReservationService;

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

}