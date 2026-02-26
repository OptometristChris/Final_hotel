package com.spring.app.hk.reservation.service;

import java.util.Map;

public interface ReservationService {

	// 예약 db 저장하기
    void saveReservation(Map<String, String> map);

}