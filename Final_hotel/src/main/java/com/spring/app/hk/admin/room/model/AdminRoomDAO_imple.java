package com.spring.app.hk.admin.room.model;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.app.hk.room.domain.RoomTypeDTO;

@Repository
public class AdminRoomDAO_imple implements AdminRoomDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // 객실 목록 조회
    @Override
    public List<RoomTypeDTO> getRoomList() {
        return sqlsession.selectList("adminRoom.getRoomList");
    }
}