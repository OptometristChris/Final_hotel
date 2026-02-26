package com.spring.app.ih.dining.service;

import com.spring.app.ih.dining.model.DiningDTO;
import com.spring.app.ih.dining.mapper.DiningMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DiningService {

    private final DiningMapper diningMapper;

    public DiningService(DiningMapper diningMapper) {
        this.diningMapper = diningMapper;
    }

    public List<DiningDTO> getDiningList() {
        return diningMapper.getDiningList();
    }
}