package com.spring.app.js.notice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.app.js.notice.domain.NoticeDTO;
import com.spring.app.js.notice.model.NoticeDAO;

@Service
public class NoticeServiceimple implements NoticeService {
    
    @Autowired
    private NoticeDAO noticeDao;

    // 1. 공지사항 목록 조회 (지점별)
    @Override
    public List<NoticeDTO> getNoticeList(Long hotelId) {
    	if (hotelId == null || hotelId == 0) {
            return noticeDao.selectNoticeList(null);
        }
        return noticeDao.selectNoticeList(hotelId);
    }

    // 2. 공지사항 상세 조회
    @Override
    public NoticeDTO getNoticeDetail(Long noticeId) {
        // 상세 조회를 호출하고 결과를 반환합니다.
        return noticeDao.selectNoticeDetail(noticeId);
    }

    // 3. 새 공지사항 등록
    @Override
    public void registerNotice(NoticeDTO dto) {
        // [최적화] 체크박스 미선택 시 기본값 처리 (isTop이 null이면 "N")
        if (dto.getIsTop() == null) {
            dto.setIsTop("N");
        }
        
        // [최적화] 실제 DAO는 기술적인 이름인 'insertNotice' 호출
        int result = noticeDao.insertNotice(dto);
        
        if (result == 0) {
            throw new RuntimeException("공지사항 등록 중 오류가 발생했습니다.");
        }
    }
    
    // 수정 기능
    @Override
    public int updateNotice(NoticeDTO dto) {
        // 수정 시에도 체크박스가 해제되어 들어오면 'N'으로 설정
        if (dto.getIsTop() == null) {
            dto.setIsTop("N");
        }
        return noticeDao.updateNotice(dto);
    }

    // 삭제 기능
    @Override
    public int deleteNotice(Long noticeId) {
        return noticeDao.deleteNotice(noticeId);
    }

    // 검색 및 필터링 기능이 포함된 목록 조회
    @Override
    public List<NoticeDTO> getNoticeList(Long hotelId, String searchType, String keyword) {
        // 1. 파라미터를 담을 Map 생성
        Map<String, Object> paraMap = new HashMap<>();
        
        // 2. hotelId가 0(전체)이면 검색 조건에서 제외하기 위해 null 처리 혹은 그대로 전달
        paraMap.put("hotelId", (hotelId == null || hotelId == 0) ? null : hotelId);
        paraMap.put("searchType", searchType);
        paraMap.put("keyword", keyword);
        
        // 3. DAO에 Map을 전달하여 조회
        return noticeDao.selectNoticeListWithSearch(paraMap);
    }

}