package com.spring.app.hk.admin.room.service;

import java.util.List;
import java.util.Map;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface AdminRoomService {

	// ======== 지점관리자 ========
	// 지점 객실 목록 조회
	List<RoomTypeDTO> getRoomListByManager(Integer adminNo);
	
	// 객실 등록 신청시 객실 + 이미지 저장
	void saveRoom(Map<String, Object> paraMap);
	
	// ======== 총괄관리자 ========
	// 승인 대기 객실 목록 조회
	List<RoomTypeDTO> getPendingRoomList();

	// 객실 승인
	void approveRoom(int roomTypeId, int adminId);

	// 객실 반려
	void rejectRoom(int roomTypeId, int adminId, String reason);

	
	
	

}