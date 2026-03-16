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
import com.spring.app.jh.security.domain.Session_GuestDTO;

import jakarta.servlet.http.HttpSession;
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
    public String saveReservation(Map<String, String> map, HttpSession session) {

        Map<String, Object> paraMap = new HashMap<>(map);

        Integer memberNo = null;
        String email = null;
        String name = null;

        // 1️. Spring Security 로그인 회원
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            memberNo = userDetails.getMemberDto().getMemberNo();
            email = userDetails.getMemberDto().getEmail();
            name = userDetails.getMemberDto().getName();
        }

        // 2️. 비회원 로그인
        if (memberNo == null) {

            Session_GuestDTO guest = (Session_GuestDTO) session.getAttribute("guestSession");

            if (guest != null) {
                memberNo = guest.getMemberNo();
                name = guest.getGuestName();
            }
        }

        // 로그인 안된 경우 방지
        if (memberNo == null) {
            throw new RuntimeException("로그인 또는 비회원 로그인이 필요합니다.");
        }

        paraMap.put("member_no", memberNo);

        // 인원수 처리
        String guestCount = map.getOrDefault("guest_count", "1");
        paraMap.put("guest_count", guestCount);

        // imp_uid null 방지
        if (paraMap.get("imp_uid") == null) {
            paraMap.put("imp_uid", "");
        }

        // 가격 결정
        if (map.containsKey("applied_price") && !map.get("applied_price").isEmpty()) {
            paraMap.put("total_price", map.get("applied_price"));
        } else {
            paraMap.put("total_price", map.getOrDefault("room_price", "100"));
        }

        // 날짜 처리
        int roomId = Integer.parseInt(map.get("room_type_id"));
        LocalDate checkIn = LocalDate.parse(map.get("check_in"));
        LocalDate checkOut = LocalDate.parse(map.get("check_out"));

        LocalDateTime cancelDeadline = checkIn.atStartOfDay().minusDays(1);
        paraMap.put("cancel_deadline", cancelDeadline);
        paraMap.put("refund_amount", 0);

        // 재고 차감
        roomStockService.decreaseStockByDateRange(roomId, checkIn, checkOut);

        // PAYMENT insert
        reservationDAO.insertPayment(paraMap);

        // RESERVATION insert
        reservationDAO.insertReservation(paraMap);

        Long reservationId = (Long) paraMap.get("reservation_id");

        // 예약 코드 생성
        String reservationCode = "R"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", reservationId);

        // 회원일 경우만 이메일 전송
        if (email != null) {
            try {
                reservationMailService.sendReservationMail(
                        email,
                        name,
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


	// 비회원 예약 조회
	@Override
	public List<Map<String,Object>> findGuestReservation(String name, String phone){

	    Map<String,Object> paraMap = new HashMap<>();
	    paraMap.put("name", name);
	    paraMap.put("phone", phone);

	    return reservationDAO.findGuestReservation(paraMap);
	}


	// 비회원 예약 취소
	@Override
	public int cancelGuestReservation(String reservationCode) {
		return reservationDAO.cancelGuestReservation(reservationCode);
	}


}
