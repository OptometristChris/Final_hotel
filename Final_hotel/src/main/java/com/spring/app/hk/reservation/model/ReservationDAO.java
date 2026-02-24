package com.spring.app.hk.reservation.model;

import com.spring.app.hk.reservation.domain.ReservationDTO;

public interface ReservationDAO {

	// 예약 생성
    int insertReservation(ReservationDTO dto);
}