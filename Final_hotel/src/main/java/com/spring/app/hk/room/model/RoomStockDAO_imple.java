package com.spring.app.hk.room.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RoomStockDAO_imple implements RoomStockDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    @Override
    public int selectStockForUpdate(int room_type_id, LocalDate stay_date) {

        Map<String,Object> paraMap = new HashMap<>();
        paraMap.put("room_type_id", room_type_id);
        paraMap.put("stay_date", stay_date);

        return sqlsession.selectOne("room.selectStockForUpdate", paraMap);
    }

    @Override
    public int decreaseStock(int room_type_id, LocalDate stay_date) {

        Map<String,Object> paraMap = new HashMap<>();
        paraMap.put("room_type_id", room_type_id);
        paraMap.put("stay_date", stay_date);

        return sqlsession.update("room.decreaseStock", paraMap);
    }

    @Override
    public int selectPrice(int room_type_id, LocalDate stay_date) {

        Map<String,Object> paraMap = new HashMap<>();
        paraMap.put("room_type_id", room_type_id);
        paraMap.put("stay_date", stay_date);

        return sqlsession.selectOne("room.selectPrice", paraMap);
    }
}