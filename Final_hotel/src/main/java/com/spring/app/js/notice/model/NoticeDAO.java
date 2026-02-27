package com.spring.app.js.notice.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.spring.app.js.notice.domain.NoticeDTO;

@Mapper
public interface NoticeDAO {
    List<NoticeDTO> selectNoticeList(Long hotelId); // 지점별 목록 조회
    NoticeDTO selectNoticeDetail(Long noticeId);    // 상세 보기
    int insertNotice(NoticeDTO dto);                // 공지 등록
    int updateNotice(NoticeDTO dto);                // 공지 수정
    int deleteNotice(Long noticeId);                // 공지 삭제
    
    // 페이징 처리를 위한 총 게시물 수 조회 (XML의 getTotalCount와 연결)
    int getTotalCount(Map<String, Object> paraMap);
    
    // 검색 및 필터링 기능이 포함된 목록 조회
	List<NoticeDTO> selectNoticeListWithSearch(Map<String, Object> paraMap);
	
	// 고정글(isTop = 'Y')만 별도로 조회 (모든 페이지 상단 노출용)
	List<NoticeDTO> selectTopNotices(Long id);
}