package com.spring.app.hk.hotel.service;

import java.util.Map;

public interface HotelService {

    // 호텔 + 이미지 저장
    void saveHotel(Map<String, String> map);
}