package com.spring.app.hk.room.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.spring.app.hk.room.domain.RoomTypeDTO;
import com.spring.app.hk.room.service.RoomTypeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomTypeService roomService;

    /* ==============================
       1. 객실 목록 페이지 조회
       ============================== */
    @GetMapping("/room/list")
    public ModelAndView roomList(ModelAndView mav) {

        List<RoomTypeDTO> roomList = roomService.getRoomList();

        // 디버깅용
        System.out.println("roomList size = " + roomList.size());
        System.out.println(roomList);

        mav.addObject("roomList", roomList);
        mav.setViewName("room/list");

        return mav;
    }

    /* ==============================
       2. 객실 필터 조회 (AJAX)
       ============================== */
    @GetMapping("/room/filter")
    @ResponseBody
    public List<RoomTypeDTO> filterRoom(
    		@RequestParam(name="hotel", defaultValue="") String hotel,
            @RequestParam(name="room_grade", defaultValue="") String roomGrade,
            @RequestParam(name="bed_type", defaultValue="") String bedType,
            @RequestParam(name="view_type", defaultValue="") String viewType,
            @RequestParam(name="sort", defaultValue="") String sort) {

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("hotel", hotel);
        paraMap.put("room_grade", roomGrade);
        paraMap.put("bed_type", bedType);
        paraMap.put("view_type", viewType);
        paraMap.put("sort", sort); 

        return roomService.getRoomListByFilter(paraMap);
    }

    /* ==============================
       3. 객실 상세 조회
       ============================== */
    @GetMapping("/room/detail")
    public ModelAndView roomDetail(ModelAndView mav,
                                   @RequestParam("room_id") Long roomId) {

        RoomTypeDTO room = roomService.getRoomDetail(roomId);

        mav.addObject("room", room);
        mav.setViewName("room/detail");

        return mav;
    }
    
    /* ==============================
       4. 달력 모달 띄우기
       ============================== */
    @GetMapping("/room/calendar")
    @ResponseBody
    public List<Map<String,Object>> getCalendar(
            @RequestParam(name="room_id")  int room_id) {

        return roomService.getCalendarPrice(room_id);
    }
}