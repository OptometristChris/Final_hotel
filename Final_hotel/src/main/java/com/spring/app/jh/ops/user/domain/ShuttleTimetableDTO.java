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
}