package com.spring.app.hk.room.domain;

import lombok.Data;

@Data
public class RoomTypeDTO {

	// room type
    private int room_type_id;
    private int brand_id;
    private String room_grade;
    private String bed_type;
    private String view_type;
    private String room_name;
    private int base_price;
    private String image_url;
    
    // room stock
    private Integer availableCount; // 날짜 범위 내 최소 재고
    
    // room min price
    private Integer minPrice;
}
