package com.spring.app.ih.dining.mapper;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper 
public interface DiningMapper {
    
	List<DiningDTO> getDiningList(Map<String, Object> paraMap);

    DiningDTO getDiningDetail(@Param("dining_id") int dining_id);
    
    int registerReservation(DiningReservationDTO reservationDTO);

	int insertReservation(DiningReservationDTO reservationDTO);

	int insertPayment(Map<String, Object> payMap);

	String getDiningName(Long outletId);
}