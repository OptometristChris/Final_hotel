package com.spring.app.jh.ops.user.domain;

import lombok.Data;

@Data
public class ShuttleTimetableDTO {
    private Long timetableId;
    private Integer hotelId;

    private String legType;     // TO_HOTEL / FROM_HOTEL
    private String placeCode;   // SEOUL_STATION / GIMPO / INCHEON
    private String placeName;
    private String departTime;  // HH24:MI

    // 화면용 계산값
    private Integer capacity;
    private Integer bookedQty;
    private Integer remaining;

    // ✅ 내가 이 시간표에 입력/예약한 수량(없으면 0)
    private Integer myQty;
}