package com.spring.app.ih.dining.service;

import java.util.List;
import java.util.Map;
import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.model.DiningReservationDTO;
import com.spring.app.jh.security.domain.MemberDTO;
import com.spring.app.jh.security.domain.Session_MemberDTO;

public interface DiningService {
	
    List<DiningDTO> getDiningList(Map<String, Object> paraMap);
    
    DiningDTO getDiningDetail(int dining_id);

	int registerReservation(DiningReservationDTO reservationDTO, String impUid, Session_MemberDTO member);

	String getDiningName(int dining_id);

	List<DiningReservationDTO> findNonMemberReservations(String name, String email, String password);

	void cancelReservation(Long id);
}