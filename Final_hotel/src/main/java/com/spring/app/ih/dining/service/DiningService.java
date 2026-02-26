package com.spring.app.ih.dining.service;

import java.util.List;
import java.util.Map;
import com.spring.app.ih.dining.model.DiningDTO;

public interface DiningService {
	
    List<DiningDTO> getDiningList(Map<String, Object> paraMap);
    
    DiningDTO getDiningDetail(int dining_id);
}