package com.spring.app.ih.dining.service;

import java.util.List;
import java.util.Map;
import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;

public interface DiningService {
	
    List<DiningDTO> getDiningList(Map<String, Object> paraMap);
    
    DiningDTO getDiningDetail(int dining_id);

	int registerReservation(DiningReservationDTO reservationDTO, String impUid);

	String getDiningName(Long outletId);
}