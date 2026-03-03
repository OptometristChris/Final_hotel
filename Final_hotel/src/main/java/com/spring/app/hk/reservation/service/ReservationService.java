package com.spring.app.hk.reservation.service;

import java.util.Map;

public interface ReservationService {

	// 예약 db 저장 후 reservation_code 반환
    String saveReservation(Map<String, String> map);

    // 예약 완료 페이지 조회
	Map<String, Object> getReservationByCode(String code);

}