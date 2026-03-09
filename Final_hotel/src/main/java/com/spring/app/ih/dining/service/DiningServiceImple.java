package com.spring.app.ih.dining.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.ih.dining.mapper.DiningMapper;
import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;

@Service
public class DiningServiceImple implements DiningService {

    @Autowired
    private DiningMapper diningMapper;

    // 목록 조회 (필터링용 Map 포함)
    @Override
    public List<DiningDTO> getDiningList(Map<String, Object> paraMap) {
        return diningMapper.getDiningList(paraMap);
    }

    // 다이닝 매장 상세 조회
    @Override
    public DiningDTO getDiningDetail(int dining_id) {
        return diningMapper.getDiningDetail(dining_id);
    }
    
    @Transactional
    public int registerReservation(DiningReservationDTO reservationDTO, String impUid) {
        
        int resResult = diningMapper.insertReservation(reservationDTO);
        
        if (resResult > 0) {
        	Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("resNo", reservationDTO.getDiningReservationId()); // 생성된 PK 값!
            paraMap.put("amount", 100); 
            paraMap.put("originalAmount", 100);
            paraMap.put("paymentMethod", "card");
            paraMap.put("status", "PAID");
            paraMap.put("pgTid", impUid);
            
            return diningMapper.insertPayment(paraMap);
        }
        
        return 0;
    }

	@Override
	public String getDiningName(Long diningId) {
		return diningMapper.getDiningName(diningId);
	}



    
}