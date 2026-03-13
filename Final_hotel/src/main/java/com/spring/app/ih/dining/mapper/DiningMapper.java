package com.spring.app.ih.dining.mapper;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;
import com.spring.app.jh.security.domain.MemberDTO;
import com.spring.app.jh.security.domain.Session_MemberDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper 
public interface DiningMapper {
    
	List<DiningDTO> getDiningList(Map<String, Object> paraMap);

    DiningDTO getDiningDetail(@Param("dining_id") int dining_id);
    
    int registerReservation(DiningReservationDTO reservationDTO, String impUid, Session_MemberDTO member);

	int insertReservation(DiningReservationDTO reservationDTO);

	int insertPayment(Map<String, Object> payMap);

	String getDiningName(Long diningId);
}