package com.spring.app.hk.admin.room.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.app.hk.room.domain.RoomTypeDTO;

@Repository
public class AdminRoomDAO_imple implements AdminRoomDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // 지점 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getRoomListByManager(Integer adminNo) {
        return sqlsession.selectList("adminRoom.getRoomListByManager", adminNo);
    }
    
    // 객실 등록 신청시
    // 1. 객실 등록
    @Override
    public void insertRoom(Map<String, Object> paraMap) {
        sqlsession.insert("adminRoom.insertRoom", paraMap);

    }

    // 2. 이미지 등록
    @Override
    public void insertRoomImage(Map<String, Object> paraMap) {
        sqlsession.insert("adminRoom.insertRoomImage", paraMap);

    }
    
    
    // 총관관리자
    // 승인 대기 객실 목록
    @Override
    public List<RoomTypeDTO> getPendingRoomList() {
        return sqlsession.selectList("adminRoom.getPendingRoomList");
    }

    // 객실 승인
    @Override
    public void approveRoom(int roomTypeId, int adminId) {

        Map<String, Object> param = new HashMap<>();
        param.put("roomTypeId", roomTypeId);
        param.put("adminId", adminId);

        sqlsession.update("adminRoom.approveRoom", param);
    }

    // 객실 반려
    @Override
    public void rejectRoom(int roomTypeId, int adminId, String reason) {

        Map<String, Object> param = new HashMap<>();
        param.put("roomTypeId", roomTypeId);
        param.put("adminId", adminId);
        param.put("reason", reason);

        sqlsession.update("adminRoom.rejectRoom", param);
    }

	

	
}