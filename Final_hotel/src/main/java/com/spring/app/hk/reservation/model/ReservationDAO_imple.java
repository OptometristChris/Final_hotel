package com.spring.app.hk.reservation.model;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spring.app.hk.reservation.domain.ReservationDTO;

@Repository
public class ReservationDAO_imple implements ReservationDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    @Override
    public int insertReservation(ReservationDTO dto) {
        return sqlsession.insert("reservation.insertReservation", dto);
    }
}