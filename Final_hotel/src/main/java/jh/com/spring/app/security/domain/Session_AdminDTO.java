package jh.com.spring.app.security.domain;

import lombok.Data;

@Data
public class Session_AdminDTO {

    private String adminid;
    private String name;
    private String admin_type; // HQ / BRANCH
    private Long fk_hotel_id;  // BRANCH면 NOT NULL, HQ면 null
}
