package com.spring.app.hk.room.domain;

import lombok.Data;

@Data
public class RoomTypeDTO {

    private int room_type_id;
    private int brand_id;
    private String room_grade;
    private String bed_type;
    private String view_type;
    private String room_name;
    private int base_price;
    
    private String image_url;
}
