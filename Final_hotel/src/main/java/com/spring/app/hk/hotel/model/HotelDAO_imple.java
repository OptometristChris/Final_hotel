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

	
}