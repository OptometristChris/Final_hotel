package com.spring.app.jh.ops.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
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

        // ✅ bookingId 먼저 조회 (reservationId -> bookingId)
        Long bookingId = shuttleDao.selectBookingIdByReservation(reservationId);

        // ✅ myQty 기본값 0 세팅
        for (var t : toList) t.setMyQty(0);
        for (var t : fromList) t.setMyQty(0);

        // ✅ bookingId가 있으면 기존 BOOKED 레그를 읽어서 myQty 채우기 (bookingId 기준!)
        if (bookingId != null) {

            var toLegs = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "TO_HOTEL");
            var fromLegs = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "FROM_HOTEL");

            java.util.Map<Long, Integer> toQtyMap = new java.util.HashMap<>();
            for (var l : toLegs) toQtyMap.put(l.getTimetableId(), l.getTicketQty());

            java.util.Map<Long, Integer> fromQtyMap = new java.util.HashMap<>();
            for (var l : fromLegs) fromQtyMap.put(l.getTimetableId(), l.getTicketQty());

            for (var t : toList) t.setMyQty(toQtyMap.getOrDefault(t.getTimetableId(), 0));
            for (var t : fromList) t.setMyQty(fromQtyMap.getOrDefault(t.getTimetableId(), 0));
        }

        ShuttleReservePageDTO page = new ShuttleReservePageDTO();
        page.setReservation(res);
        page.setToRideDate(toRideDate);
        page.setFromRideDate(fromRideDate);
        page.setToList(toList);
        page.setFromList(fromList);

        page.setShuttleBookingId(bookingId);

        // ✅ 다중 레그 구조면 아래 단건 세팅은 의미가 약함.
        // 필요 없다면 제거하고, 필요하면 "목록/합계"로 바꾸는 게 맞음.
        // if (bookingId != null) {
        //     page.setToBookedLeg(shuttleDao.selectLegByBookingAndType(bookingId, "TO_HOTEL"));
        //     page.setFromBookedLeg(shuttleDao.selectLegByBookingAndType(bookingId, "FROM_HOTEL"));
        // }

        return page;
    }


    
    @Override
    @Transactional
    public void confirm(long reservationId, int sessionMemberNo,
                        List<Long> toTimetableIds, List<Integer> toQtys,
                        List<Long> fromTimetableIds, List<Integer> fromQtys) {

        ReservationForShuttleDTO res = shuttleDao.selectReservationForShuttle(reservationId);
        assertAccessible(res, sessionMemberNo);

        // booking 헤더 upsert
        Long bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        if (bookingId == null) {
            shuttleDao.insertBookingHeader(reservationId, res.getHotelId(), sessionMemberNo, res.getCheckinDate());
            bookingId = shuttleDao.selectBookingIdByReservation(reservationId);
        }

        // 입력 정규화(Null 방어)
        if (toTimetableIds == null) toTimetableIds = List.of();
        if (toQtys == null) toQtys = List.of();
        if (fromTimetableIds == null) fromTimetableIds = List.of();
        if (fromQtys == null) fromQtys = List.of();

        // 방향별 ride_date 고정
        Date toRideDate = res.getCheckinDate();
        Date fromRideDate = res.getCheckoutDate();

        // 방향별 합계 검증(각각 <= guest_count)
        int toSum = sumNonNeg(toQtys);
        int fromSum = sumNonNeg(fromQtys);

        if (toSum > res.getGuestCount()) {
            throw new IllegalArgumentException("호텔행(체크인) 총 인원은 최대 티켓 수를 초과할 수 없습니다.");
        }
        if (fromSum > res.getGuestCount()) {
            throw new IllegalArgumentException("행선지행(체크아웃) 총 인원은 최대 티켓 수를 초과할 수 없습니다.");
        }

        // 기존 BOOKED 레그 조회(방향별)
        var existingTo = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "TO_HOTEL");
        var existingFrom = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "FROM_HOTEL");

        // map: timetableId -> qty / legId
        var exToQty = new java.util.HashMap<Long, Integer>();
        var exToLegId = new java.util.HashMap<Long, Long>();
        for (var l : existingTo) { exToQty.put(l.getTimetableId(), l.getTicketQty()); exToLegId.put(l.getTimetableId(), l.getShuttleLegId()); }

        var exFromQty = new java.util.HashMap<Long, Integer>();
        var exFromLegId = new java.util.HashMap<Long, Long>();
        for (var l : existingFrom) { exFromQty.put(l.getTimetableId(), l.getTicketQty()); exFromLegId.put(l.getTimetableId(), l.getShuttleLegId()); }

        // desired map 만들기(0은 제외해도 되지만 delta 계산 편의상 포함)
        var desiredTo = buildDesiredMap(toTimetableIds, toQtys);
        var desiredFrom = buildDesiredMap(fromTimetableIds, fromQtys);

        // delta 대상 timetableId 세트(방향별)
        var toAllIds = unionKeys(exToQty, desiredTo);
        var fromAllIds = unionKeys(exFromQty, desiredFrom);

        // 1) TO_HOTEL 처리
        applyDirectionDelta(
            bookingId, res.getHotelId(), "TO_HOTEL", toRideDate,
            toAllIds, desiredTo, exToQty, exToLegId
        );

        // 2) FROM_HOTEL 처리
        applyDirectionDelta(
            bookingId, res.getHotelId(), "FROM_HOTEL", fromRideDate,
            fromAllIds, desiredFrom, exFromQty, exFromLegId
        );

        // 3) 최종 상태 반영
        var remainTo = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "TO_HOTEL");
        var remainFrom = shuttleDao.selectBookedLegListByBookingAndType(bookingId, "FROM_HOTEL");

        boolean hasToBooked = (remainTo != null && !remainTo.isEmpty());
        boolean hasFromBooked = (remainFrom != null && !remainFrom.isEmpty());

        if (!hasToBooked && !hasFromBooked) {
            // 레그가 하나도 없으면 → 헤더 취소
            shuttleDao.updateHeaderCanceled(bookingId);
        } else {
            // 레그가 하나라도 있으면 → 헤더 BOOKED 보장
            shuttleDao.updateHeaderBooked(bookingId);
        }
    }

    private int sumNonNeg(List<Integer> list) {
        int s=0;
        for (Integer v : list) {
            int x = (v==null?0:v);
            if (x < 0) throw new IllegalArgumentException("티켓 수량은 0 이상이어야 합니다.");
            s += x;
        }
        return s;
    }

    private java.util.Map<Long,Integer> buildDesiredMap(List<Long> ids, List<Integer> qtys){
        var map = new java.util.HashMap<Long,Integer>();
        int n = Math.min(ids.size(), qtys.size());
        for (int i=0;i<n;i++){
            Long id = ids.get(i);
            int q = (qtys.get(i)==null?0:qtys.get(i));
            if (id == null) continue;
            if (q < 0) throw new IllegalArgumentException("티켓 수량은 0 이상이어야 합니다.");
            map.put(id, q); // 같은 id가 중복이면 마지막 값이 최종
        }
        return map;
    }

    private java.util.List<Long> unionKeys(java.util.Map<Long,Integer> a, java.util.Map<Long,Integer> b){
        var set = new java.util.HashSet<Long>();
        set.addAll(a.keySet());
        set.addAll(b.keySet());
        var list = new java.util.ArrayList<>(set);
        list.sort(Long::compareTo); // 락 순서 고정(데드락 방지)
        return list;
    }

    private void applyDirectionDelta(
        long bookingId, int hotelId, String legType, Date rideDate,
        java.util.List<Long> timetableIds,
        java.util.Map<Long,Integer> desired,
        java.util.Map<Long,Integer> existingQty,
        java.util.Map<Long,Long> existingLegId
    ){
        for (Long timetableId : timetableIds) {

            int want = desired.getOrDefault(timetableId, 0);
            int have = existingQty.getOrDefault(timetableId, 0);
            int delta = want - have;

            if (delta == 0) continue;

            // timetable 검증(조작 방지)
            ShuttleTimetableDTO tt = shuttleDao.selectTimetableOne(timetableId);
            if (tt == null) throw new IllegalStateException("시간표가 존재하지 않습니다.");
            if (tt.getHotelId() == null || tt.getHotelId() != hotelId) throw new IllegalStateException("시간표-호텔 불일치");
            if (!legType.equals(tt.getLegType())) throw new IllegalStateException("시간표-방향 불일치");

            // 감소(취소/감축)는 취소 마감 적용: 전날 18시(해당 timetable의 departTime 기준으로)
            if (delta < 0) {
                assertCancelableDeadline(rideDate, tt.getDepartTime());
            }

            // slot_stock 락 + booked_qty 반영
            SlotStockDTO locked = shuttleDao.selectSlotForUpdate(timetableId, rideDate);
            if (locked == null) throw new IllegalStateException("slot_stock 누락(미리 생성 누락)");

            if (delta > 0) {
                int remaining = locked.getCapacity() - locked.getBookedQty();
                if (remaining < delta) throw new IllegalStateException("잔여 좌석이 부족합니다.");
                shuttleDao.updateBookedQtyPlus(timetableId, rideDate, delta);
            } else {
                int dec = Math.abs(delta);
                if (locked.getBookedQty() < dec) throw new IllegalStateException("재고 데이터 비정상(booked_qty 부족)");
                shuttleDao.updateBookedQtyMinus(timetableId, rideDate, dec);
            }

            // leg 테이블 반영
            Long legId = existingLegId.get(timetableId);

            if (want == 0) {
                // have>0 인 상태에서 0으로 => cancel
                if (legId != null) {
                    shuttleDao.cancelLeg(legId);
                } else {
                    // 혹시 BOOKED 목록엔 없지만 실제 레그가 있을 수 있음(비정상/잔재)
                    ShuttleLegDTO anyLeg = shuttleDao.selectLegByBookingAndTimetable(bookingId, timetableId, rideDate);
                    if (anyLeg != null && "BOOKED".equals(anyLeg.getLegStatus())) {
                        shuttleDao.cancelLeg(anyLeg.getShuttleLegId());
                    }
                }
            } else {
                if (legId == null) {
                    // ✅ BOOKED 목록엔 없지만 CANCELED 레그가 있을 수 있으므로 먼저 찾아서 부활
                    ShuttleLegDTO existed = shuttleDao.selectLegByBookingAndTimetable(bookingId, timetableId, rideDate);

                    if (existed != null) {
                        // CANCELED 포함, 어떤 상태든 “부활 + qty 반영”
                        shuttleDao.updateLegQtyBooked(existed.getShuttleLegId(), want);
                    } else {
                        // 진짜 없을 때만 insert
                        shuttleDao.insertLeg(bookingId, timetableId, legType, rideDate,
                                             tt.getPlaceCode(), tt.getDepartTime(), want);
                    }
                } else {
                    // 있으면 qty update (BOOKED로 복구)
                    shuttleDao.updateLegQtyBooked(legId, want);
                }
            }
        }
    }

    // 취소 정책: 전날 18시까지(탑승일+출발시간 기준)
    private void assertCancelableDeadline(Date rideDate, String departTimeHHmm) {
        java.time.LocalDate ride = rideDate.toInstant().atZone(KST).toLocalDate();
        java.time.LocalTime depart = java.time.LocalTime.parse(departTimeHHmm);
        java.time.LocalDateTime departAt = java.time.LocalDateTime.of(ride, depart);

        java.time.LocalDateTime deadline = java.time.LocalDateTime.of(departAt.toLocalDate().minusDays(1), java.time.LocalTime.of(18,0));
        java.time.LocalDateTime now = java.time.LocalDateTime.now(KST);

        if (now.isAfter(deadline)) {
            throw new IllegalStateException("취소 가능 시간(전날 18시)을 초과했습니다.");
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

	@Override
	public ShuttleConfirmPageDTO getConfirmPage(int memberNo) {
		// 1) 카드 뼈대(bookingId/reservationId 중심) 조회
		List<ShuttleConfirmCardDTO> skeleton = shuttleDao.selectActiveShuttleBookings(memberNo);
		if (skeleton == null) skeleton = List.of();

		LocalDate today = LocalDate.now(KST);
		var cards = new ArrayList<ShuttleConfirmCardDTO>();

		for (ShuttleConfirmCardDTO card : skeleton) {
			if (card == null || card.getReservationId() == null || card.getShuttleBookingId() == null) continue;

			long reservationId = card.getReservationId();
			long bookingId = card.getShuttleBookingId();

			// 2) 좌측 1/3: 예약 정보(호텔/객실/이미지/체크인아웃/인원) 채우기
			ReservationForShuttleDTO res = shuttleDao.selectReservationForCard(reservationId);
			if (res == null) continue;

			card.setHotelName(res.getHotelName());
			card.setRoomName(res.getRoomName());
			card.setRoomImageUrl(res.getRoomImageUrl());
			card.setReservationStatus(res.getReservationStatus());
			card.setGuestCount(res.getGuestCount() == null ? 0 : res.getGuestCount());

			// 3) 표시 규칙(오늘 날짜 기준)
			LocalDate checkout = toLocalDate(res.getCheckoutDate());
			LocalDate checkin = toLocalDate(res.getCheckinDate());

			boolean showTo = (checkout != null && !checkout.isBefore(today)); // checkout >= today
			boolean showFrom = (checkout != null && !checkout.isBefore(today)) && (checkout.equals(today) || checkout.isAfter(today));
			// FROM은 ride_date(=checkout) < today 이면 숨김
			if (checkout != null && checkout.isBefore(today)) showFrom = false;

			card.setShowTo(showTo);
			card.setShowFrom(showFrom);

			if (!showTo && !showFrom) {
				continue; // 둘 다 숨김이면 카드 제외
			}

			card.setCheckinText(fmtDate(checkin));
			card.setCheckoutText(fmtDate(checkout));
			card.setToRideDateText(fmtDate(checkin));
			card.setFromRideDateText(fmtDate(checkout));

			// 4) 우측 2/3: 레그 상세(BOOKED) 읽어서 TO/FROM 분리
			List<ShuttleLegDTO> legs = shuttleDao.selectBookedLegsByBooking(bookingId);
			if (legs == null) legs = List.of();

			List<ShuttleConfirmLegItemDTO> toLegs = legs.stream()
				.filter(l -> l != null && "TO_HOTEL".equals(l.getLegType()))
				.sorted(Comparator.comparing(ShuttleLegDTO::getDepartTime, Comparator.nullsLast(String::compareTo)))
				.map(this::toLegItem)
				.collect(Collectors.toList());

			List<ShuttleConfirmLegItemDTO> fromLegs = legs.stream()
				.filter(l -> l != null && "FROM_HOTEL".equals(l.getLegType()))
				.sorted(Comparator.comparing(ShuttleLegDTO::getDepartTime, Comparator.nullsLast(String::compareTo)))
				.map(this::toLegItem)
				.collect(Collectors.toList());

			card.setToLegs(toLegs);
			card.setFromLegs(fromLegs);

			cards.add(card);
		}

		ShuttleConfirmPageDTO page = new ShuttleConfirmPageDTO();
		page.setCards(cards);
		page.setValidCount(cards.size());

		// memberName 은 별도 조회(회원/게스트 모두 tbl_member_security.name)
		try {
			String memberName = shuttleDao.selectMemberName(memberNo);
			page.setMemberName(memberName);
		} catch (Exception ignore) {
			page.setMemberName(null);
		}

		return page;
	}

	private LocalDate toLocalDate(Date d) {
		if (d == null) return null;
		return d.toInstant().atZone(KST).toLocalDate();
	}

	private String fmtDate(LocalDate d) {
		if (d == null) return "";
		return d.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private ShuttleConfirmLegItemDTO toLegItem(ShuttleLegDTO leg) {
		ShuttleConfirmLegItemDTO item = new ShuttleConfirmLegItemDTO();
		item.setDepartTime(leg.getDepartTime());
		item.setTicketQty(leg.getTicketQty() == null ? 0 : leg.getTicketQty());
		item.setPlaceName(placeName(leg.getPlaceCode()));
		return item;
	}

	private String placeName(String placeCode) {
		if (placeCode == null) return "";
		return switch (placeCode) {
			case "SEOUL_STATION" -> "서울역";
			case "GIMPO" -> "김포공항";
			case "INCHEON" -> "인천공항";
			default -> placeCode;
		};
	}
}