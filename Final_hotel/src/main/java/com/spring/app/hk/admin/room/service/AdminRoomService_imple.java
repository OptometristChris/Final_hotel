package com.spring.app.hk.admin.room.service;

import java.util.List;
import java.util.Map;

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

    // ======== 지점관리자 ========
    // 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getRoomListByManager(Integer adminNo) {

        List<RoomTypeDTO> roomList = adminRoomDAO.getRoomListByManager(adminNo);

        return roomList;
    }
    
    
    // 객실 등록 신청시 객실 + 이미지 저장
    @Override
    public void saveRoom(Map<String, Object> paraMap) {

        // 1️. 객실 등록
        adminRoomDAO.insertRoom(paraMap);

        // 생성된 room_type_id 가져오기
        int roomTypeId = (int) paraMap.get("room_type_id");

        // 2️. 이미지 저장
        if(paraMap.get("image_url") != null){

            paraMap.put("fk_room_type_id", roomTypeId);
            paraMap.put("is_main", "Y");
            paraMap.put("sort_order", 1);

            adminRoomDAO.insertRoomImage(paraMap);
        }

        System.out.println("객실 + 이미지 저장 완료");
    }

    
    
    
    
    
    
    
    
    // ======== 총괄관리자 ========
    // 승인 대기 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getPendingRoomList() {
        return adminRoomDAO.getPendingRoomList();
    }

    // 객실 승인
    @Override
    public void approveRoom(int roomTypeId, int adminId) {
        adminRoomDAO.approveRoom(roomTypeId, adminId);
    }

    // 객실 반려
    @Override
    public void rejectRoom(int roomTypeId, int adminId, String reason) {
        adminRoomDAO.rejectRoom(roomTypeId, adminId, reason);
    }


	

	
		
	}