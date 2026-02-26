package com.spring.app.hk.reservation.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.reservation.model.ReservationDAO;

@Service
@Transactional
public class ReservationService_imple implements ReservationService {

    @Autowired
    private ReservationDAO reservationDAO;

    // 예약 db 저장하기
    @Override
    public void saveReservation(Map<String, String> map) {

        Map<String, Object> paraMap = new HashMap<>(map);

        // 테스트용
        paraMap.put("member_no", 4);   // ← 여기
        paraMap.put("total_price", 100);
        
        // 1️) PAYMENT insert
        reservationDAO.insertPayment(paraMap);

        System.out.println("생성된 payment_id = " + paraMap.get("payment_id"));

        // 2️) RESERVATION insert
        reservationDAO.insertReservation(paraMap);

        System.out.println("예약 + 결제 저장 완료");
    }
}
