package com.spring.app.hk.admin.room.model;

import java.util.List;
import java.util.Map;

import com.spring.app.hk.room.domain.RoomTypeDTO;

public interface AdminRoomDAO {

	// ======== 지점관리자 ========
	// 지점 객실 목록 조회
	List<RoomTypeDTO> getRoomListByManager(Integer adminNo);
	
	// 객실 등록 신청시
	// 1. 객실 등록
	void insertRoom(Map<String, Object> paraMap);

	// 2. 이미지 저장
	void insertRoomImage(Map<String, Object> paraMap);
	
	// ======== 총괄관리자 ========
	// 승인 대기 객실 목록 조회
	List<RoomTypeDTO> getPendingRoomList();

	// 객실 승인
	void approveRoom(int roomTypeId, int adminId);

	// 객실 반려
	void rejectRoom(int roomTypeId, int adminId, String reason);
	  
	
	
}