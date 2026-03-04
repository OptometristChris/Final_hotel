package com.spring.app.hk.hotel.model;

import java.util.List;
import java.util.Map;

public interface HotelDAO {

	// 호텔 리스트 가져오기
	List<Map<String,Object>> selectHotelList();
	
    // 호텔 insert
    int insertHotel(Map<String, Object> paraMap);

    // 호텔 이미지 insert
    int insertHotelImage(Map<String, Object> paraMap);
}