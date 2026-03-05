package com.spring.app.hk.hotel.model;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HotelDAO_imple implements HotelDAO {

    @Autowired
    private SqlSessionTemplate sqlsession;

    // 호텔 리스트 가져오기
    @Override
	public List<Map<String, Object>> selectHotelList() {
    	 return sqlsession.selectList("hotel.selectHotelList");
	}
    
    // 호텔 상세페이지 이동
    @Override
	public Map<String, Object> selectHotelDetail(Long hotelId) {
		return sqlsession.selectOne("hotel.selectHotelDetail", hotelId);
	}
    
    // 호텔 insert
    @Override
    public int insertHotel(Map<String, Object> paraMap) {
        return sqlsession.insert("hotel.insertHotel", paraMap);
    }

    // 호텔 이미지 insert
    @Override
    public int insertHotelImage(Map<String, Object> paraMap) {
        return sqlsession.insert("hotel.insertHotelImage", paraMap);
    }

    // 호텔 상세페이지 내 수정하기
	@Override
	public int updateHotel(Map<String, Object> param) {
		 return sqlsession.update("hotel.updateHotel", param);
	}

	// 호텔 상세페이지 내 비활성화하기
	@Override
	public int deleteHotel(int hotel_id) {
	    return sqlsession.update("hotel.deleteHotel", hotel_id);
	}
	

	
}