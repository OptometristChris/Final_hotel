package com.spring.app.hk.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.hk.reservation.mail.ReservationMailService;
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
    
    private final ReservationMailService reservationMailService; // 추가 : 메일

    // 결제 성공 후 db 저장하기
    @Override
    public String saveReservation(Map<String, String> map) {

        Map<String, Object> paraMap = new HashMap<>(map);

        // 로그인 사용자 정보 세팅
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        paraMap.put("member_no", userDetails.getMemberDto().getMemberNo());
        
        // 2. [추가] 인원수(guest_count) 처리
        // 프론트에서 넘어온 값이 없으면 기본값 1, 혹은 0으로 세팅하여 ORA-17004 방지
        String guestCount = map.getOrDefault("guest_count", "1"); 
        paraMap.put("guest_count", guestCount);

        // 3. [추가] imp_uid 처리
        // 결제 UID가 null이면 빈 문자열이라도 넣어 오라클 에러 방지
        if (paraMap.get("imp_uid") == null) {
            paraMap.put("imp_uid", ""); 
        }

        // ■ [수정 포인트 1] 가격 결정 로직
        // 프로모션 예약은 'applied_price'를 쓰고, 일반 예약은 기존처럼 처리합니다.
        if (map.containsKey("applied_price") && !map.get("applied_price").isEmpty()) {
            paraMap.put("total_price", map.get("applied_price")); // 프로모션 최종가
        } else {
            // 기존에 100으로 고정하셨던 부분을 room_price 등 실제 값으로 변경 권장
            paraMap.put("total_price", map.getOrDefault("room_price", "100")); 
        }
        
        // ■ [수정 포인트 2] 날짜 파싱 및 취소 마감일
        int roomId = Integer.parseInt(map.get("room_type_id"));
        LocalDate checkIn = LocalDate.parse(map.get("check_in"));
        LocalDate checkOut = LocalDate.parse(map.get("check_out"));

        LocalDateTime cancelDeadline = checkIn.atStartOfDay().minusDays(1);
        paraMap.put("cancel_deadline", cancelDeadline);
        paraMap.put("refund_amount", 0);
        
        // 재고 차감 (기존 로직 유지)
        roomStockService.decreaseStockByDateRange(roomId, checkIn, checkOut);
        
        // 4) PAYMENT insert (프로모션 시 넘어온 imp_uid 등이 paraMap에 포함됨)
        reservationDAO.insertPayment(paraMap);

        // 5) RESERVATION insert (paraMap에 promotion_id가 있으면 MyBatis에서 처리)
        reservationDAO.insertReservation(paraMap);
        
        Long reservationId = (Long) paraMap.get("reservation_id");

        // 예약 코드 생성 (기존 포맷 유지)
        String reservationCode = "R" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                               + "-" + String.format("%04d", reservationId);

        // 메일 전송 (기존 로직 유지)
        try {
            reservationMailService.sendReservationMail(
                    userDetails.getMemberDto().getEmail(),
                    userDetails.getMemberDto().getName(),
                    reservationCode,
                    map.get("hotel_name"),
                    map.get("room_name"),
                    map.get("check_in"),
                    map.get("check_out"),
                    String.valueOf(paraMap.get("total_price"))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return reservationCode;
    }

    
    // 예약 완료 페이지
	@Override
	public Map<String, Object> getReservationByCode(String code) {
		return reservationDAO.findByReservationCode(code);
	}


	// 예약 페이지 내 객실 기본 정보 조회
	@Override
	public Map<String, Object> getRoomInfo(int room_type_id) {

	    Map<String, Object> roomInfo = reservationDAO.getRoomInfo(room_type_id);

	    if (roomInfo == null) {
	        throw new IllegalArgumentException("해당 객실이 존재하지 않습니다.");
	    }

	    return roomInfo;
	}


	// 마이페이지 예약 목록 조회
	@Override
	public List<Map<String, Object>> selectMyReservationList(int memberNo) {
		return reservationDAO.selectMyReservationList(memberNo);
	}


	// 예약 취소
	@Override
	public int cancelReservation(long reservationId) {
	    return reservationDAO.cancelReservation(reservationId);
	}


	// 예약 취소 내역 조회
	@Override
	public List<Map<String, Object>> selectMyCancelReservationList(int memberNo) {
		return reservationDAO.selectMyCancelReservationList(memberNo);
	}
}
