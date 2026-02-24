package com.spring.app.hk.reservation.service;

import com.spring.app.hk.reservation.domain.ReservationDTO;

public interface ReservationService {

    // 예약 생성
    int createReservation(ReservationDTO reservationDTO);
}