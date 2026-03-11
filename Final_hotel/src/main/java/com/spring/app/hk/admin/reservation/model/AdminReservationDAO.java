package com.spring.app.hk.admin.reservation.model;

import java.util.List;
import java.util.Map;

public interface AdminReservationDAO {

    List<Map<String,Object>> getTodayCheckinList();

    List<Map<String,Object>> getTodayCheckoutList();

    void checkinReservation(int reservationId);

    void checkoutReservation(int reservationId);

	List<Map<String, Object>> getStayList();

	List<Map<String, Object>> getCheckoutCompleteList();

}