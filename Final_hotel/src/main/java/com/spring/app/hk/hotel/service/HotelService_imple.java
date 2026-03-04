package com.spring.app.hk.hotel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.spring.app.common.FileManager;
import com.spring.app.hk.hotel.model.HotelDAO;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelService_imple implements HotelService {

    private final HotelDAO hotelDAO;
    private final FileManager fileManager;

    @Value("${file.images-dir}")
    private String imagesDir;

    // 호텔 리스트 가져오기
    @Override
	public List<Map<String, Object>> getHotelList() {
    	  return hotelDAO.selectHotelList();
	}
 
    
    // 호텔 상세페이지 이동
   	@Override
   	public Map<String, Object> getHotelDetail(Long hotelId) {
   		return hotelDAO.selectHotelDetail(hotelId);
   	}

    
    // 호텔 등록하기
    @Override
    public void saveHotel(Map<String, String> map) {

        Map<String, Object> paraMap = new HashMap<>(map);

        // 1️. 기본값 세팅
        paraMap.put("approve_status", "PENDING");
        paraMap.put("active_yn", "Y");

        // 2️. 호텔 insert
        hotelDAO.insertHotel(paraMap);

        // 생성된 hotel_id 꺼내기 (selectKey 사용 전제)
        int hotelId = (int) paraMap.get("hotel_id");

        // 3️. 대표 이미지 처리
        if(map.get("main_image") != null) {

        	String imagePath = "/file_images/hotel/" + map.get("main_image");
        	
            paraMap.put("fk_hotel_id", hotelId);
            paraMap.put("image_url", imagePath);
            paraMap.put("is_main", "Y");
            paraMap.put("sort_order", 1);

            hotelDAO.insertHotelImage(paraMap);
        }

        System.out.println("호텔 + 이미지 저장 완료");
    }


    // 호텔 상세페이지 내 수정하기
	@Override
	public int updateHotel(Map<String, Object> param) {
		return hotelDAO.updateHotel(param);
	}


   
	
}