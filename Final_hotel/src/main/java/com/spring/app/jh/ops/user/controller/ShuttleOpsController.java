package com.spring.app.jh.ops.user.controller;

import java.util.Enumeration;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.spring.app.jh.ops.user.domain.ShuttleReservePageDTO;
import com.spring.app.jh.ops.user.service.ShuttleOpsService;
import com.spring.app.jh.security.domain.Session_GuestDTO;
import com.spring.app.jh.security.domain.Session_MemberDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shuttle")
public class ShuttleOpsController {

    private final ShuttleOpsService shuttleService;

    @GetMapping("/reserve")
    public String reserve(@RequestParam("reservationId") long reservationId,
                          HttpSession session,
                          Model model) {
    	

        Integer memberNo = getSessionMemberNo(session);
        if (memberNo == null) return "redirect:/security/login";

        ShuttleReservePageDTO page = shuttleService.getReservePage(reservationId, memberNo);
        model.addAttribute("page", page);

        return "shuttle/reserve";
    }

    @PostMapping("/book")
    public String book(@RequestParam long reservationId,
                       @RequestParam String legType,
                       @RequestParam long timetableId,
                       @RequestParam int ticketQty,
                       HttpSession session) {

        Integer memberNo = getSessionMemberNo(session);
        if (memberNo == null) return "redirect:/security/login";

        shuttleService.bookLeg(reservationId, memberNo, legType, timetableId, ticketQty);
        return "redirect:/shuttle/reserve?reservationId=" + reservationId;
    }

    @PostMapping("/cancel")
    public String cancel(@RequestParam long reservationId,
                         @RequestParam String legType,
                         HttpSession session) {

        Integer memberNo = getSessionMemberNo(session);
        if (memberNo == null) return "redirect:/security/login";

        shuttleService.cancelLeg(reservationId, memberNo, legType);
        return "redirect:/shuttle/reserve?reservationId=" + reservationId;
    }

    private Integer getSessionMemberNo(HttpSession session) {
        Session_MemberDTO m = (Session_MemberDTO) session.getAttribute("sessionMemberDTO");
        if (m != null) return m.getMemberNo();

        Session_GuestDTO g = (Session_GuestDTO) session.getAttribute("guestSession");
        if (g != null) return g.getMemberNo();

        return null;
    }
}