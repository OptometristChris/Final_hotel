package com.spring.app.js.promotion.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.js.promotion.domain.PromotionDTO;
import com.spring.app.js.promotion.model.PromotionDAO;

@Service
public class PromotionServiceimple implements PromotionService {

    @Autowired
    private PromotionDAO proDao;

    // 특정 호텔의 현재 진행 중인 프로모션 목록 조회
    @Override
    public List<PromotionDTO> getPromotionList(int hotelId) {
        // Mapper를 통해 DB에서 조인된 프로모션 목록을 가져옵니다.
        // 현재 날짜 기준 필터링 및 활성화 여부는 Mapper SQL(WHERE절)에서 처리됩니다.
        return proDao.getPromotionList(hotelId);
    }

    // 프로모션 상세 정보 조회
    @Override
    public PromotionDTO getPromotionDetail(int promotionId) {
        // 특정 프로모션의 상세 내용을 조회합니다.
        return proDao.getPromotionDetail(promotionId);
    }
    
    // 프로모션 등록
    @Transactional
    public int insertPromotion(Map<String, String> paraMap) {
    	
    	// 3. 프로모션 마스터 등록 (PK를 추출하여 paraMap에 담음)
        int result1 = proDao.insertPromotionMaster(paraMap);
        
        // 4. 프로모션 배너 등록 (위에서 추출한 PK를 FK로 사용)
        int result2 = 0;
        if(result1 > 0) {
            result2 = proDao.insertPromotionBanner(paraMap);
        }
        
        return result2;
    }
    
    // 프로모션 삭제
    @Override
    @Transactional // 둘 중 하나라도 실패하면 롤백
    public int deletePromotion(int promotionId) {
        // 자식 테이블(Banner) 먼저 삭제
        int n1 = proDao.deletePromotionBanner(promotionId);
        // 부모 테이블(Master) 삭제
        int n2 = proDao.deletePromotionMaster(promotionId);
        
        return n2; // 최종 성공 여부 반환
    }
    
    
}