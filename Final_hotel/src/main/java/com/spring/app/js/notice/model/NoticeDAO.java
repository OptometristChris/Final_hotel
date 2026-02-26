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
    
    // 검색 및 필터링 기능이 포함된 목록 조회
	List<NoticeDTO> selectNoticeListWithSearch(Map<String, Object> paraMap);
}