package com.spring.app.hk.admin.room.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.hk.admin.room.service.AdminRoomService;
import com.spring.app.hk.room.domain.RoomTypeDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/room")
public class AdminRoomController {

    private final AdminRoomService roomService;

    @GetMapping("/list")
    public String roomList(Model model) {

        List<RoomTypeDTO> roomList = roomService.getRoomList();

        model.addAttribute("roomList", roomList);

        return "hk/admin/room/roomlist";
    }
}