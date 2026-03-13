package com.spring.app.hk.reservation.model;

import java.util.List;
import java.util.Map;

public interface ReservationDAO {

	// payment 테이블에 insert
	int insertPayment(Map<String, Object> paraMap);
	
	// reservation 테이블에 insert
    int insertReservation(Map<String, Object> paraMap);

    // 예약 완료 페이지
    Map<String, Object> findByReservationCode(String reservationCode);

    // 예약 페이지 내 객실 기본 정보 조회
	Map<String, Object> getRoomInfo(int room_type_id);

	// 마이페이지 예약 목록 조회
	List<Map<String, Object>> selectMyReservationList(int memberNo);
	
	// 예약 취소
	int cancelReservation(long reservationId);

	// 예약 취소 내역 조회
	List<Map<String, Object>> selectMyCancelReservationList(int memberNo);

}