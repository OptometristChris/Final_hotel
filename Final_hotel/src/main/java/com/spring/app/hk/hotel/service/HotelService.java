package com.spring.app.hk.hotel.service;

import java.util.List;
import java.util.Map;

public interface HotelService {

	// 호텔 리스트 가져오기
	List<Map<String, Object>> getHotelList();
	
    // 호텔 + 이미지 저장
    void saveHotel(Map<String, String> map);

	
}