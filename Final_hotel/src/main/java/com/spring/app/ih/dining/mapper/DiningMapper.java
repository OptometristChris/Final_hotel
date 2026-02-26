package com.spring.app.ih.dining.mapper;

import com.spring.app.ih.dining.model.DiningDTO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper 
public interface DiningMapper {
    
    List<DiningDTO> getDiningList();
}