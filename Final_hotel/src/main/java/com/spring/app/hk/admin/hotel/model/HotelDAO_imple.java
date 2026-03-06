package com.spring.app.hk.admin.hotel.model;

import java.util.HashMap;
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
    // 운영중 리스트
    @Override
    public List<Map<String,Object>> selectApprovedHotelList(){
        return sqlsession.selectList("hotel.selectApprovedHotelList");
    }

    // 심사대기 리스트
    @Override
    public List<Map<String,Object>> selectPendingHotelList(){
        return sqlsession.selectList("hotel.selectPendingHotelList");
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


	
	// 지점관리자 호텔 조회
	@Override
	public List<Map<String, Object>> selectHotelListByManager(Integer adminNo) {
		return sqlsession.selectList("hotel.selectHotelListByManager", adminNo);
	}

	
	
	
	
	

	
}