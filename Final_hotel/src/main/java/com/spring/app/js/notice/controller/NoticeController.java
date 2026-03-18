package com.spring.app.js.notice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.app.jh.security.domain.AdminDTO;
import com.spring.app.jh.security.domain.CustomAdminDetails;
import com.spring.app.js.notice.domain.NoticeDTO;
import com.spring.app.js.notice.service.NoticeService;

@Controller
@RequestMapping("/notice")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;

    // 1. 목록 및 검색 처리
    @GetMapping("/list")
    public String list(
            @RequestParam(value = "hotelId", defaultValue = "0") Long hotelId,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "curPage", defaultValue = "1") int curPage,
            Model model) {

        // [추가] DB에서 호텔 리스트 가져오기 (탭 생성을 위함)
        List<Map<String, String>> hotelList = noticeService.getHotelList();
        model.addAttribute("hotelList", hotelList);

        int sizePerPage = 10;
        int startRow = (curPage - 1) * sizePerPage + 1;
        int endRow = startRow + sizePerPage - 1;

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("hotelId", hotelId);
        paraMap.put("searchType", searchType);
        paraMap.put("keyword", keyword);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);

        // 고정글 리스트 가져오기
        List<NoticeDTO> topNotices = noticeService.getTopNotices(hotelId);
        
        // 일반글 리스트 가져오기
        List<NoticeDTO> notices = noticeService.getNoticeList(paraMap);
        
        // 총 개수 가져오기
        int totalCount = noticeService.getTotalCount(paraMap);
        int totalPage = (int) Math.ceil((double) totalCount / sizePerPage);

        // 뷰로 전달할 데이터들
        model.addAttribute("topNotices", topNotices);
        model.addAttribute("notices", notices);       
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPage", totalPage);

        return "js/notice/list";
    }

    // 2. 상세 페이지
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, 
                         @RequestParam(value = "hotelId", defaultValue = "0") Long hotelId, 
                         Model model) {
        
        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("hotelList", noticeService.getHotelList());

        // [추가] 로그인한 관리자의 지점 ID를 꺼내서 모델에 담기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomAdminDetails) {
            AdminDTO adminDto = ((CustomAdminDetails) auth.getPrincipal()).getAdminDto();
            model.addAttribute("myHotelId", adminDto.getFk_hotel_id());
        }
        
        return "js/notice/detail";
    }
    
    // 3. 작성 페이지
    @GetMapping("/write")
    public String showWriteForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomAdminDetails) {
            CustomAdminDetails adminDetails = (CustomAdminDetails) auth.getPrincipal();
            AdminDTO adminDto = adminDetails.getAdminDto();

            if (adminDto != null) {
                // 본사 관리자 차단 로직
                boolean isHq = auth.getAuthorities().stream()
                                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_HQ"));
                if (isHq) return "redirect:/notice/list?hotelId=0";

                // 지점 리스트는 이미 noticeService.getHotelList() 등을 통해 model에 담긴다고 가정
                model.addAttribute("hotelList", noticeService.getHotelList());
                
                // 핵심: 세션에 저장된 본인의 지점 ID만 전달
                model.addAttribute("myHotelId", adminDto.getFk_hotel_id());
            }
        }
        return "js/notice/write";
    }

    @PostMapping("/write")
    public String insertNotice(NoticeDTO dto) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof CustomAdminDetails) {
            CustomAdminDetails adminDetails = (CustomAdminDetails) auth.getPrincipal();
            AdminDTO adminDto = adminDetails.getAdminDto();
            
            // 1. 작성자 번호 강제 세팅
            if (adminDto.getAdmin_no() != null) {
                dto.setAdminNo(Long.valueOf(String.valueOf(adminDto.getAdmin_no()))); 
            }
            
            // 2. [보안] 세션의 지점 ID로 무조건 고정 (사용자 조작 방지)
            if (adminDto.getFk_hotel_id() != null) {
                dto.setFkHotelId(Long.valueOf(String.valueOf(adminDto.getFk_hotel_id())));
            }
        }

        if(dto.getIsTop() == null) dto.setIsTop("N");

        noticeService.registerNotice(dto);
        
        return "redirect:/notice/list?hotelId=" + dto.getFkHotelId();
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("hotelList", noticeService.getHotelList());
        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        model.addAttribute("hotelId", notice.getFkHotelId()); 
        return "js/notice/edit"; 
    }

    @PostMapping("/edit")
    public String updateNotice(NoticeDTO dto, RedirectAttributes rttr) {
        // [보안 추가] 수정 시에도 본인 지점 ID로 강제 고정
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomAdminDetails) {
            CustomAdminDetails adminDetails = (CustomAdminDetails) auth.getPrincipal();
            dto.setFkHotelId(Long.valueOf(String.valueOf(adminDetails.getAdminDto().getFk_hotel_id())));
        }

        int result = noticeService.updateNotice(dto);
        rttr.addFlashAttribute("message", result > 0 ? "수정 완료." : "수정 실패.");
        return "redirect:/notice/detail/" + dto.getNoticeId() + "?hotelId=" + dto.getFkHotelId();
    }
    
    @PostMapping("/delete")
    public String deleteNotice(@RequestParam("noticeId") Long noticeId, RedirectAttributes rttr) {
        NoticeDTO notice = noticeService.getNoticeDetail(noticeId);
        Long hotelId = (notice != null) ? notice.getFkHotelId() : 0L;
        int result = noticeService.deleteNotice(noticeId);
        rttr.addFlashAttribute("message", result > 0 ? "성공적으로 삭제되었습니다." : "삭제 실패.");
        return "redirect:/notice/list?hotelId=" + hotelId;
    }
}