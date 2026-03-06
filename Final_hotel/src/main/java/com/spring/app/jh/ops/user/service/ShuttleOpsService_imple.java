package com.spring.app.jh.ops.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.jh.ops.user.domain.*;

import com.spring.app.jh.ops.user.model.ShuttleOpsDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShuttleOpsService_imple implements ShuttleOpsService {

    private final ShuttleOpsDAO shuttleDao;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public ShuttleReservePageDTO getReservePage(long reservationId, int sessionMemberNo) {

        ReservationForShuttleDTO res = shuttleDao.selectReservationForShuttle(reservationId);
        assertAccessible(res, sessionMemberNo);

        Date toRideDate = res.getCheckinDate();
        Date fromRideDate = res.getCheckoutDate();

        List<ShuttleTimetableDTO> toList = shuttleDao.selectTimetableList(res.getHotelId(), "TO_HOTEL");
        fillRemaining(toList, toRideDate);

        List<ShuttleTimetableDTO> fromList = shuttleDao.selectTimetableList(res.getHotelId(), "FROM_HOTEL");
        fillRemaining(fromList, fromRideDate);

        ShuttleReservePageDTO page = new ShuttleReservePageDTO();
        page.setReservation(res);
        page.setToRideDate(toRideDate);
        page.setFromRideDate(fromRideDate);
        page.setToList(toList);
        page.setFromList(fromList);

        Long bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        page.setShuttleBookingId(bookingId);

        if (bookingId != null) {
            page.setToBookedLeg(shuttleDao.selectLegByBookingAndType(bookingId, "TO_HOTEL"));
            page.setFromBookedLeg(shuttleDao.selectLegByBookingAndType(bookingId, "FROM_HOTEL"));
        }

        return page;
    }

    @Override
    @Transactional
    public void bookLeg(long reservationId, int sessionMemberNo, String legType, long timetableId, int ticketQty) {

        ReservationForShuttleDTO res = shuttleDao.selectReservationForShuttle(reservationId);
        assertAccessible(res, sessionMemberNo);

        if (!"TO_HOTEL".equals(legType) && !"FROM_HOTEL".equals(legType)) {
            throw new IllegalArgumentException("잘못된 legType 입니다.");
        }

        if (ticketQty <= 0) throw new IllegalArgumentException("티켓 수량은 1 이상이어야 합니다.");
        if (ticketQty > res.getGuestCount()) {
            throw new IllegalArgumentException("티켓 수량은 객실예약 인원(guest_count)을 초과할 수 없습니다.");
        }

        // 레그별 탑승일 고정
        Date rideDate = ("TO_HOTEL".equals(legType)) ? res.getCheckinDate() : res.getCheckoutDate();

        // timetable 조작 방지: 단건 조회로 hotelId/legType 검증
        ShuttleTimetableDTO tt = shuttleDao.selectTimetableOne(timetableId);
        if (tt == null) throw new IllegalStateException("시간표가 존재하지 않습니다.");
        if (tt.getHotelId() == null || tt.getHotelId() != res.getHotelId()) {
            throw new IllegalStateException("시간표-호텔이 일치하지 않습니다.");
        }
        if (!legType.equals(tt.getLegType())) {
            throw new IllegalStateException("시간표-방향(legType)이 일치하지 않습니다.");
        }

        // booking 헤더 upsert (대표일자는 checkin_date로 저장)
        Long bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        if (bookingId == null) {
            shuttleDao.insertBookingHeader(reservationId, res.getHotelId(), sessionMemberNo, res.getCheckinDate());
            bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        }

        // 시간 변경 = 취소 후 재예약 => 같은 legType BOOKED 존재 시 막기
        ShuttleLegDTO existing = shuttleDao.selectLegByBookingAndType(bookingId, legType);
        if (existing != null && "BOOKED".equals(existing.getLegStatus())) {
            throw new IllegalStateException("이미 예약된 편도입니다. 시간 변경은 취소 후 재예약으로 진행하세요.");
        }

        // slot_stock row LOCK → 잔여 검증 → booked_qty 증가
        SlotStockDTO locked = shuttleDao.selectSlotForUpdate(timetableId, rideDate);
        if (locked == null) {
            throw new IllegalStateException("해당 날짜의 셔틀 재고(slot_stock)가 없습니다. (미리 생성 누락)");
        }
        int remaining = locked.getCapacity() - locked.getBookedQty();
        if (remaining < ticketQty) {
            throw new IllegalStateException("잔여 좌석이 부족합니다.");
        }
        shuttleDao.updateBookedQtyPlus(timetableId, rideDate, ticketQty);

        // leg insert (ride_date 포함)
        shuttleDao.insertLeg(bookingId, timetableId, legType, rideDate, tt.getPlaceCode(), tt.getDepartTime(), ticketQty);
    }

    @Override
    @Transactional
    public void cancelLeg(long reservationId, int sessionMemberNo, String legType) {

        ReservationForShuttleDTO res = shuttleDao.selectReservationForShuttle(reservationId);
        assertAccessible(res, sessionMemberNo);

        Long bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        if (bookingId == null) throw new IllegalStateException("취소할 셔틀 예약이 없습니다.");

        ShuttleLegDTO leg = shuttleDao.selectLegByBookingAndType(bookingId, legType);
        if (leg == null || !"BOOKED".equals(leg.getLegStatus())) {
            throw new IllegalStateException("취소할 편도 예약이 없습니다.");
        }

        // 취소 정책: 전날 18시까지
        assertCancelableDeadline(leg.getRideDate());

        // slot_stock row LOCK → booked_qty 감소 → leg cancel
        SlotStockDTO locked = shuttleDao.selectSlotForUpdate(leg.getTimetableId(), leg.getRideDate());
        if (locked == null) throw new IllegalStateException("재고(slot_stock) 데이터가 없습니다.");

        if (locked.getBookedQty() < leg.getTicketQty()) {
            throw new IllegalStateException("재고 데이터가 비정상입니다(booked_qty < ticket_qty).");
        }

        shuttleDao.updateBookedQtyMinus(leg.getTimetableId(), leg.getRideDate(), leg.getTicketQty());
        shuttleDao.cancelLeg(leg.getShuttleLegId());

        // 양쪽 다 BOOKED가 아니면 헤더도 CANCELED
        ShuttleLegDTO toLeg = shuttleDao.selectLegByBookingAndType(bookingId, "TO_HOTEL");
        ShuttleLegDTO fromLeg = shuttleDao.selectLegByBookingAndType(bookingId, "FROM_HOTEL");

        boolean toBooked = (toLeg != null && "BOOKED".equals(toLeg.getLegStatus()));
        boolean fromBooked = (fromLeg != null && "BOOKED".equals(fromLeg.getLegStatus()));

        if (!toBooked && !fromBooked) {
            shuttleDao.updateHeaderCanceled(bookingId);
        }
    }

    // ===== 내부 검증 =====

    private void assertAccessible(ReservationForShuttleDTO res, int sessionMemberNo) {
        if (res == null) throw new IllegalArgumentException("예약 정보가 존재하지 않습니다.");
        if (res.getMemberNo() == null || res.getMemberNo() != sessionMemberNo) {
            throw new IllegalStateException("본인 예약만 셔틀 예약이 가능합니다.");
        }
        if (!"CONFIRMED".equals(res.getReservationStatus())) {
            throw new IllegalStateException("CONFIRMED 예약만 셔틀 예약이 가능합니다.");
        }
    }

    private void fillRemaining(List<ShuttleTimetableDTO> list, Date rideDate) {
        for (ShuttleTimetableDTO t : list) {
            SlotStockDTO slot = shuttleDao.selectSlotForDisplay(t.getTimetableId(), rideDate);
            if (slot == null) {
                t.setCapacity(0);
                t.setBookedQty(0);
                t.setRemaining(0);
            } else {
                t.setCapacity(slot.getCapacity());
                t.setBookedQty(slot.getBookedQty());
                t.setRemaining(slot.getCapacity() - slot.getBookedQty());
            }
        }
    }

    private void assertCancelableDeadline(Date rideDate) {
        LocalDate ride = rideDate.toInstant().atZone(KST).toLocalDate();
        LocalDateTime deadline = LocalDateTime.of(ride.minusDays(1), LocalTime.of(18, 0));
        LocalDateTime now = LocalDateTime.now(KST);
        if (now.isAfter(deadline)) {
            throw new IllegalStateException("취소 가능 시간(전날 18시)을 초과했습니다.");
        }
    }
}