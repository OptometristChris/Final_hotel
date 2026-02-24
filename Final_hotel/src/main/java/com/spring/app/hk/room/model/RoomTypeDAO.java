package com.spring.app.hk.room.model;

import java.util.List;
import java.util.Map;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface RoomTypeDAO {

	// 객실 목록 페이지 (최초 진입) 조회
	List<RoomTypeDTO> selectRoomTypeList();
	
	// 객실 필터 조회 (AJAX 필터용 JSON 반환)
    List<RoomTypeDTO> selectRoomTypeByFilter(Map<String, String> paraMap);

    // 객실 상세 페이지 조회
	RoomTypeDTO selectRoomDetail(Long roomId);

	// 날짜별 가격 조회
	List<Map<String, Object>> selectCalendarPrice(int room_id);
	
}
