package com.spring.app.jh.ops.user.service;

import com.spring.app.jh.ops.user.domain.ShuttleReservePageDTO;

public interface ShuttleOpsService {
    ShuttleReservePageDTO getReservePage(long reservationId, int sessionMemberNo);

    void bookLeg(long reservationId, int sessionMemberNo, String legType, long timetableId, int ticketQty);

    void cancelLeg(long reservationId, int sessionMemberNo, String legType);
}