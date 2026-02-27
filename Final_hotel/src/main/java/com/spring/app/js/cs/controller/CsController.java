package com.spring.app.js.cs.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.spring.app.js.cs.service.CsService;

@Controller
@RequestMapping("/cs")
public class CsController {

    @Autowired
    private CsService service;

    /**
     * [사용자] FAQ + QnA 통합 고객지원 메인 페이지
     * @param hotelId : 1(호텔 시엘), 2(르시엘) - 기본값 1
     */
    @GetMapping("/main")
    public ModelAndView csMain(ModelAndView mav, 
                               // 수정: value 속성 추가
                               @RequestParam(value = "hotelId", defaultValue = "1") String hotelId,
                               HttpServletRequest request) {

        // 1. 페이징 설정 (QnA 목록용)
        String str_currentShowPageNo = request.getParameter("currentShowPageNo");
        int currentShowPageNo = (str_currentShowPageNo == null) ? 1 : Integer.parseInt(str_currentShowPageNo);

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("hotelId", hotelId);
        paraMap.put("currentShowPageNo", String.valueOf(currentShowPageNo));

        // 2. FAQ 목록 조회 (FAQS 테이블)
        List<Map<String, String>> faqList = service.getFaqListByHotel(hotelId);

        // 3. QnA 목록 조회 (QUESTIONS 테이블)
        int totalCount = service.getQnaTotalCount(paraMap);
        int sizePerPage = 10; 
        int startRno = ((currentShowPageNo - 1) * sizePerPage) + 1;
        int endRno = startRno + sizePerPage - 1;
        
        paraMap.put("startRno", String.valueOf(startRno));
        paraMap.put("endRno", String.valueOf(endRno));
        
        List<Map<String, String>> qnaList = service.getQnaListWithPaging(paraMap);

        // 4. 뷰 전달 데이터 설정
        mav.addObject("faqList", faqList);
        mav.addObject("qnaList", qnaList);
        mav.addObject("hotelId", hotelId); 
        mav.addObject("totalCount", totalCount);
        
        mav.setViewName("js/cs/csList");
        
        return mav;
    }

    /**
     * [사용자] 1:1 문의 작성 페이지 이동
     */
    @GetMapping("/qnaWrite")
    public ModelAndView qnaWrite(ModelAndView mav, 
                                 // 수정: value 속성 추가
                                 @RequestParam(value = "hotelId") String hotelId) {
        
        mav.addObject("hotelId", hotelId);
        mav.setViewName("js/cs/qnaWrite.tiles1");
        return mav;
    }

    /**
     * [사용자] 1:1 문의 상세 보기
     */
    @GetMapping("/qnaDetail")
    public ModelAndView qnaDetail(ModelAndView mav, 
                                  // 수정: value 속성 추가
                                  @RequestParam(value = "qnaId") String qnaId) {
        
        Map<String, String> qnaDetail = service.getQnaDetail(qnaId);
        
        mav.addObject("qna", qnaDetail);
        mav.setViewName("js/cs/qnaDetail.tiles1");
        return mav;
    }
}