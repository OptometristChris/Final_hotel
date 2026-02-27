package com.spring.app.js.notice.service;

import java.util.List;
import java.util.Map;
import com.spring.app.js.notice.domain.NoticeDTO;

public interface NoticeService {
    // 1. 공지사항 목록 조회 (호텔 지점별) - 기존 유지
    List<NoticeDTO> getNoticeList(Long hotelId);

    // 2. 공지사항 상세 조회 - 기존 유지
    NoticeDTO getNoticeDetail(Long noticeId);

    // 3. 새 공지사항 등록 - 기존 유지
    void registerNotice(NoticeDTO dto);

    // 4. 수정 및 삭제 - 기존 유지
    int updateNotice(NoticeDTO dto);
    int deleteNotice(Long noticeId);

    // [추가] 5. 검색 및 페이징이 포함된 목록 조회 (Map 사용)
    List<NoticeDTO> getNoticeList(Map<String, Object> paraMap);

    // [추가] 6. 조건에 맞는 게시글 총 개수 조회 (페이징 계산용)
    int getTotalCount(Map<String, Object> paraMap);

    // 기존의 (Long hotelId, String searchType, String keyword) 메서드는 
    // 위 5번(Map 방식)으로 대체하거나 필요시 유지하세요.
    List<NoticeDTO> getNoticeList(Long hotelId, String searchType, String keyword);

	List<NoticeDTO> getTopNotices(Long hotelId);
}