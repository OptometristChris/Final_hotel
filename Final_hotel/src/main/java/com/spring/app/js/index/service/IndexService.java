package com.spring.app.js.index.service;

import com.spring.app.hk.room.domain.RoomTypeDTO;
import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.js.banner.domain.BannerDTO;
import com.spring.app.js.promotion.domain.PromotionDTO;

import java.util.List;
import java.util.Map;

public interface IndexService {
    // 메인 배너 리스트 조회
    List<BannerDTO> getMainBannerList();

    // 메인용 객실 리스트
    List<RoomTypeDTO> getMainRoomList();

    // 메인용 다이닝 리스트
    List<DiningDTO> getMainDiningList();

    // 메인용 프로모션 리스트
    List<PromotionDTO> getPromoCardList();

    // 객실 검색
	List<RoomTypeDTO> getAvailableRooms(Map<String, Object> paraMap);

	// 호텔 리스트 
	List<Map<String, String>> getHotelList();

	// 호텔 이미지 가져오기
	List<Map<String, Object>> getHotelImages(String hotelId);

	// [신설] 특정 호텔의 기존 배너 정보 가져오기 (배너 관리 - 데이터 불러오기용)
    Map<String, Object> getBannerByHotelId(String hotelId);

    // [변경] 메인 배너 저장 (등록/수정 통합 - Merge 쿼리 호출용)
    int saveBanner(Map<String, String> paraMap);
}