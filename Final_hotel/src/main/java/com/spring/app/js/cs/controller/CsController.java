package com.spring.app.js.cs.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.app.js.cs.service.CsService;

@Controller
@RequestMapping("/cs")
public class CsController {

    @Autowired
    private CsService service;

    /**
     * [사용자] FAQ + QnA 통합 고객지원 메인 페이지
     * @param hotelId : 1(호텔 시엘), 2(르시엘) - 기본값 1
     * @param searchKeyword : 검색어 추가
     */
    @GetMapping("/list")
    public ModelAndView csMain(ModelAndView mav, 
                               @RequestParam(value = "hotelId", defaultValue = "1") String hotelId,
                               @RequestParam(value = "searchKeyword", defaultValue = "") String searchKeyword, // 검색어 파라미터 추가
                               HttpServletRequest request) {

        // 1. 페이징 설정
        // HTML에서 curPage로 넘기므로 currentShowPageNo 대신 curPage로 받거나 호환되게 처리
        String str_currentShowPageNo = request.getParameter("curPage"); 
        int currentShowPageNo = (str_currentShowPageNo == null) ? 1 : Integer.parseInt(str_currentShowPageNo);

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("hotelId", hotelId);
        paraMap.put("searchKeyword", searchKeyword); // [중요] DB 조회를 위해 검색어 추가

        // 2. FAQ 조회
        List<Map<String, String>> faqList = service.getFaqListByHotel(hotelId);

        // 3. QnA 조회 및 페이징 계산
        // getQnaTotalCount 쿼리 내부에 searchKeyword 조건이 들어있어야 정확한 페이지수가 계산됩니다.
        int totalCount = service.getQnaTotalCount(paraMap); 
        int sizePerPage = 10; 
        
        int totalPage = (int) Math.ceil((double) totalCount / sizePerPage);
        
        int startRno = ((currentShowPageNo - 1) * sizePerPage) + 1;
        int endRno = startRno + sizePerPage - 1;
        
        paraMap.put("startRno", String.valueOf(startRno));
        paraMap.put("endRno", String.valueOf(endRno));
        
        List<Map<String, String>> qnaList = service.getQnaListWithPaging(paraMap);

        // 4. 뷰 전달 데이터 설정
        mav.addObject("faqList", faqList);
        mav.addObject("qnaList", qnaList);
        mav.addObject("hotelId", hotelId); 
        mav.addObject("searchKeyword", searchKeyword); // [중요] 페이지 이동 시 검색어 유지를 위해 뷰로 다시 전달
        mav.addObject("totalCount", totalCount);
        
        mav.addObject("curPage", currentShowPageNo);  
        mav.addObject("totalPage", totalPage);        
        
        mav.setViewName("js/cs/csList");
        
        return mav;
    }

    /**
     * [사용자] 1:1 문의 작성 페이지 이동
     */
    @GetMapping("/qnaWrite")
    public ModelAndView qnaWrite(ModelAndView mav, 
                                 @RequestParam(value = "hotelId") String hotelId,
                                 HttpSession session,
                                 java.security.Principal principal) {
        
        boolean isMember = (principal != null);
        boolean isGuest = (session.getAttribute("Session_GuestDTO") != null);

        if (!isMember && !isGuest) {
            mav.addObject("message", "로그인이 필요한 서비스입니다.");
            mav.addObject("loc", "/security/login"); 
            mav.setViewName("msg"); 
            return mav;
        }
        
        mav.addObject("hotelId", hotelId);
        mav.setViewName("js/cs/qnaWrite");
        return mav;
    }
    
    @PostMapping("/qnaWriteEnd")
    public ModelAndView qnaWriteEnd(ModelAndView mav, HttpServletRequest request, HttpSession session, java.security.Principal principal) {
        
        String hotelId = request.getParameter("fk_hotel_id");
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String is_secret = "1".equals(request.getParameter("is_secret")) ? "Y" : "N";

        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("fk_hotel_id", hotelId);
        paraMap.put("title", title);
        paraMap.put("content", content);
        paraMap.put("is_secret", is_secret);

        if (principal != null) {
            paraMap.put("writer_name", principal.getName()); 
        } else {
            com.spring.app.jh.security.domain.Session_GuestDTO guest = 
                (com.spring.app.jh.security.domain.Session_GuestDTO) session.getAttribute("Session_GuestDTO");
            
            if (guest != null) {
                paraMap.put("writer_name", guest.getGuestName());
                paraMap.put("lookup_key", guest.getLookupKey()); 
            }
        }

        int n = service.insertQna(paraMap);

        if(n == 1) {
            mav.addObject("message", "문의가 성공적으로 등록되었습니다.");
            mav.addObject("loc", request.getContextPath() + "/cs/list?hotelId=" + hotelId);
        } else {
            mav.addObject("message", "등록에 실패했습니다.");
            mav.addObject("loc", "javascript:history.back()");
        }

        mav.setViewName("msg"); 
        return mav;
    }

    /**
     * [사용자] 1:1 문의 상세 보기
     */
    @GetMapping("/qnaDetail")
    public ModelAndView qnaDetail(ModelAndView mav, @RequestParam(value = "qnaId") String qnaId) {
        Map<String, String> qnaDetail = service.getQnaDetail(qnaId);
        
        mav.addObject("qna", qnaDetail);
        mav.setViewName("js/cs/qnaDetail"); 
        return mav;
    }
    
    @GetMapping("/qnaDelete")
    public String qnaDelete(@RequestParam("qna_id") String qnaId, 
                            @RequestParam("hotelId") String hotelId,
                            java.security.Principal principal, 
                            HttpSession session,
                            RedirectAttributes rttr) {

        Map<String, String> qna = service.getQnaDetail(qnaId);
        String writerIdInDb = String.valueOf(qna.get("WRITER_NAME"));
        
        boolean isOwner = false;

        if (principal != null && principal.getName().equals(writerIdInDb)) {
            isOwner = true;
        } 
        else {
            com.spring.app.jh.security.domain.Session_GuestDTO guest = 
                (com.spring.app.jh.security.domain.Session_GuestDTO) session.getAttribute("Session_GuestDTO");
            
            if (guest != null && guest.getGuestName().equals(writerIdInDb)) {
                isOwner = true; 
            }
        }

        if (isOwner) {
            service.deleteQna(qnaId);
            rttr.addFlashAttribute("message", "삭제되었습니다.");
        } else {
            rttr.addFlashAttribute("message", "본인만 삭제 가능합니다.");
        }

        return "redirect:/cs/list?hotelId=" + hotelId;
    }
}