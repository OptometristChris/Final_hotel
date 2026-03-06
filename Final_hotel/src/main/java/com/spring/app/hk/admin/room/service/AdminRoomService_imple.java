package com.spring.app.hk.admin.room.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.admin.room.model.AdminRoomDAO;
import com.spring.app.hk.room.domain.RoomTypeDTO;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRoomService_imple implements AdminRoomService {

    private final AdminRoomDAO adminRoomDAO;

    // 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getRoomList() {

        List<RoomTypeDTO> roomList = adminRoomDAO.getRoomList();

        return roomList;
    }
}