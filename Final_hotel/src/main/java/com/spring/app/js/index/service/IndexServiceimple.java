package com.spring.app.js.index.service;

import com.spring.app.hk.room.domain.RoomTypeDTO;
import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.js.banner.domain.BannerDTO;
import com.spring.app.js.index.model.IndexDAO;
import com.spring.app.js.promotion.domain.PromotionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class IndexServiceimple implements IndexService {

    @Autowired
    private IndexDAO inDao; // 데이터베이스 접근 객체

    // 메인 배너 리스트 조회
    @Override
    public List<BannerDTO> getMainBannerList() {
        return inDao.getMainBannerList();
    }

    // 메인용 객실 리스트
    @Override
    public List<RoomTypeDTO> getMainRoomList() {
        // 나중에 여기서 "브랜드별 노출 순위" 등의 로직을 추가할 수 있습니다.
        return inDao.getMainRoomList();
    }

    // 메인용 다이닝 리스트
    @Override
    public List<DiningDTO> getMainDiningList() {
        return inDao.getMainDiningList();
    }

    // 메인용 프로모션 리스트
    @Override
    public List<PromotionDTO> getPromoCardList() {
        return inDao.getPromoCardList();
    }

    // 객실 검색
	@Override
	public List<RoomTypeDTO> getAvailableRooms(Map<String, Object> paraMap) {
		return inDao.getAvailableRooms(paraMap);
	}

	// 호텔 리스트
	@Override
	public List<Map<String, String>> getHotelList() {
		return inDao.getHotelList();
	}
	
	// 호텔 이미지 가져오기
	@Override
	public List<Map<String, Object>> getHotelImages(String hotelId) {
	    return inDao.getHotelImages(hotelId);
	}
	
	// [추가] 특정 호텔의 기존 배너 정보 가져오기 (데이터 불러오기용)
    @Override
    public Map<String, Object> getBannerByHotelId(String hotelId) {
        return inDao.getBannerByHotelId(hotelId);
    }

    // [변경] 메인 배너 저장 (등록/수정 통합 - Merge 쿼리 호출)
    @Override
    public int saveBanner(Map<String, String> paraMap) {
        // 기존 insertBanner 대신 MyBatis의 saveBanner(MERGE INTO)를 호출합니다.
        return inDao.saveBanner(paraMap);
    }
	
}