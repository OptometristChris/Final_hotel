package com.spring.app.js.notice.domain;

import java.util.Date;

import lombok.Data;

@Data
public class NoticeDTO {
    private Long noticeId;
    private Long adminNo;
    private Long fkHotelId;  // 테이블의 fk_hotel_id와 매칭
    private String title;
    private String content;  // CLOB은 자바에서 String으로 처리
    private String isTop;    // 'Y' 또는 'N'
    private Date createdAt;
    
    // 조인 시 필요한 필드 (선택사항)
    private String hotelName; // 호텔 지점명 노출용
    private String adminName; // 작성자 이름 노출용
}
