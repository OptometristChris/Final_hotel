package com.spring.app.hk.reservation.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.reservation.model.ReservationDAO;
import com.spring.app.hk.room.service.RoomStockService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService_imple implements ReservationService {

    private final ReservationDAO reservationDAO;
    private final RoomStockService roomStockService;

    // 결제 성공 후 db 저장하기
    @Override
    public void saveReservation(Map<String, String> map) {

        Map<String, Object> paraMap = new HashMap<>(map);

        // 테스트용
        paraMap.put("member_no", 4);   // ← 여기
        paraMap.put("total_price", 100);
        
        // ■ 날짜 파싱
        int roomId = Integer.parseInt(map.get("room_type_id"));
        LocalDate checkIn = LocalDate.parse(map.get("check_in"));
        LocalDate checkOut = LocalDate.parse(map.get("check_out"));

        // ■ 1) 재고 차감
        roomStockService.decreaseStockByDateRange(roomId, checkIn, checkOut);
        
        // 2) PAYMENT insert
        reservationDAO.insertPayment(paraMap);

        System.out.println("생성된 payment_id = " + paraMap.get("payment_id"));

        // 3) RESERVATION insert
        reservationDAO.insertReservation(paraMap);

        System.out.println("예약 + 결제 저장 완료");
    }
}
