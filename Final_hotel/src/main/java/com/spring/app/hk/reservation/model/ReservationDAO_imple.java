package com.spring.app.hk.reservation.model;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ReservationDAO_imple implements ReservationDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // payment 테이블에 insert
    @Override
    public int insertPayment(Map<String, Object> paraMap) {
        return sqlsession.insert("reservation.insertPayment", paraMap);
    }
    
    // reservation 테이블에 insert
    @Override
    public int insertReservation(Map<String, Object> paraMap) {
        return sqlsession.insert("reservation.insertReservation", paraMap);
    }
}
