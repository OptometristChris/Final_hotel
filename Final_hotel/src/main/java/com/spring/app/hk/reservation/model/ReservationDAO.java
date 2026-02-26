package com.spring.app.hk.reservation.model;

import java.util.Map;

public interface ReservationDAO {

	// payment 테이블에 insert
	int insertPayment(Map<String, Object> paraMap);
	
	// reservation 테이블에 insert
    int insertReservation(Map<String, Object> paraMap);

	

}