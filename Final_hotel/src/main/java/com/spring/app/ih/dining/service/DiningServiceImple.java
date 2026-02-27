package com.spring.app.ih.dining.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.app.ih.dining.mapper.DiningMapper;
import com.spring.app.ih.dining.model.DiningDTO;

@Service
public class DiningServiceImple implements DiningService {

    @Autowired
    private DiningMapper mapper;

    // 목록 조회 (필터링용 Map 포함)
    @Override
    public List<DiningDTO> getDiningList(Map<String, Object> paraMap) {
        return mapper.getDiningList(paraMap);
    }

    // 다이닝 매장 상세 조회
    @Override
    public DiningDTO getDiningDetail(int dining_id) {
        return mapper.getDiningDetail(dining_id);
    }
}