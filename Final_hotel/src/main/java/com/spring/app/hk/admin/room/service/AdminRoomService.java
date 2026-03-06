package com.spring.app.hk.admin.room.service;

import java.util.List;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface AdminRoomService {

	// 객실 목록 조회
    List<RoomTypeDTO> getRoomList();

}