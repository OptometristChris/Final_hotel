package jh.com.spring.app.security.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;


@Data
public class AdminDTO {

    private String adminid;
    private String passwd;
    private String enabled;      // '1'/'0'

    private String name;
    private String email;
    private String mobile;

    private String admin_type;   // 'HQ'/'BRANCH'
    private Long fk_hotel_id;    // NUMBER -> Long

    private LocalDate registerday;
    private LocalDate passwd_modify_date;
    private LocalDate last_login_date;

    private List<String> authorities;
}
