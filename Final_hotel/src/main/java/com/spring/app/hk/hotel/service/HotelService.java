package com.spring.app.hk.hotel.service;

import java.util.List;
import java.util.Map;

public interface HotelService {

	// 호텔 리스트 가져오기
	List<Map<String, Object>> getHotelList();
	
    // 호텔 + 이미지 저장
    void saveHotel(Map<String, String> map);

    // 호텔 상세페이지 이동
	Map<String, Object> getHotelDetail(Long hotelId);

	 // 호텔 상세페이지 내 수정하기
	int updateHotel(Map<String, Object> param);

	// 호텔 상세페이지 내 비활성화하기
	int deleteHotel(int hotel_id);

	
}