package com.spring.app.hk.hotel.domain;

import lombok.Data;

@Data
public class HotelImageDTO {

    private int image_id;
    private int fk_hotel_id;
    private String image_url;
    private String is_main;
    private int sort_order;
}