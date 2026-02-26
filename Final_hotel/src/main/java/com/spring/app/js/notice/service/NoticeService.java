package com.spring.app.js.notice.service;

import java.util.List;

import com.spring.app.js.notice.domain.NoticeDTO;

public interface NoticeService {
	// 1. 공지사항 목록 조회 (호텔 지점별)
    List<NoticeDTO> getNoticeList(Long hotelId);

    // 2. 공지사항 상세 조회
    NoticeDTO getNoticeDetail(Long noticeId);

    // 3. 새 공지사항 등록
    void registerNotice(NoticeDTO dto);

    // 4. 수정 및 삭제
    int updateNotice(NoticeDTO dto);
    int deleteNotice(Long noticeId);
	
    
}
