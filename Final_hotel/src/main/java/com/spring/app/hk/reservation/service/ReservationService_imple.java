package com.spring.app.hk.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.reservation.model.ReservationDAO;
import com.spring.app.hk.room.service.RoomStockService;
import com.spring.app.jh.security.domain.CustomUserDetails;

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

        // 로그인 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        int memberNo = userDetails.getMemberDto().getMemberNo();

        paraMap.put("member_no", memberNo);
        paraMap.put("total_price", 100);
        
        // ■ 날짜 파싱
        int roomId = Integer.parseInt(map.get("room_type_id"));
        LocalDate checkIn = LocalDate.parse(map.get("check_in"));
        LocalDate checkOut = LocalDate.parse(map.get("check_out"));

        // 1) 취소 마감일 계산 (체크인 하루 전 00:00 기준)
        LocalDateTime cancelDeadline = checkIn.atStartOfDay().minusDays(1);
        paraMap.put("cancel_deadline", cancelDeadline);

        // 2) 환불금액 기본값
        paraMap.put("refund_amount", 0);
        
        // 3) 재고 차감
        roomStockService.decreaseStockByDateRange(roomId, checkIn, checkOut);
        
        // 4) PAYMENT insert
        reservationDAO.insertPayment(paraMap);

        System.out.println("생성된 payment_id = " + paraMap.get("payment_id"));

        // 5) RESERVATION insert
        reservationDAO.insertReservation(paraMap);

        System.out.println("예약 + 결제 저장 완료");
    }
}
