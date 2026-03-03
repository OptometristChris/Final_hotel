package com.spring.app.hk.hotel.model;

import java.util.Map;

public interface HotelDAO {

    // 호텔 insert
    int insertHotel(Map<String, Object> paraMap);

    // 호텔 이미지 insert
    int insertHotelImage(Map<String, Object> paraMap);
}