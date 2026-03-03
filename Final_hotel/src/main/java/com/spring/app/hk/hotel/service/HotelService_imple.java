package com.spring.app.hk.hotel.service;

import java.util.HashMap;
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

            paraMap.put("fk_hotel_id", hotelId);
            paraMap.put("image_url", map.get("main_image"));
            paraMap.put("is_main", "Y");
            paraMap.put("sort_order", 1);

            hotelDAO.insertHotelImage(paraMap);
        }

        System.out.println("호텔 + 이미지 저장 완료");
    }
}