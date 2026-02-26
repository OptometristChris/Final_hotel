package com.spring.app.js.notice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam(value = "hotelId", required = false, defaultValue = "0") Long hotelId,
            @RequestParam(value = "searchType", required = false) String searchType, 
            @RequestParam(value = "keyword", required = false) String keyword,       
            Model model) {
        
        // 검색 조건을 포함하여 서비스 호출
        // (주의: 기존 getNoticeList(hotelId) 메서드를 getNoticeList(hotelId, searchType, keyword)로 서비스에서 오버로딩하거나 수정해야 합니다)
        List<NoticeDTO> notices = noticeService.getNoticeList(hotelId, searchType, keyword);

        model.addAttribute("notices", notices);
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("searchType", searchType); // html 드롭다운 상태 유지
        model.addAttribute("keyword", keyword);       // html 입력창 검색어 유지
        
        return "js/notice/list"; 
    }

    // 2. 상세 페이지
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, 
                         @RequestParam(value = "hotelId", defaultValue = "0") Long hotelId, 
                         Model model) {
        model.addAttribute("notice", noticeService.getNoticeDetail(id));
        model.addAttribute("hotelId", hotelId); 
        return "js/notice/detail";
    }
    
    // 3. 작성 페이지
    @GetMapping("/write")
    public String showWriteForm(@RequestParam(value = "hotelId", required = false, defaultValue = "1") Long hotelId, Model model) {
        model.addAttribute("hotelId", hotelId);
        return "js/notice/write";
    }

    // 4. 작성 처리
    @PostMapping("/write")
    public String insertNotice(NoticeDTO dto) {
        if(dto.getAdminNo() == null) {
            dto.setAdminNo(2L); 
        }
        noticeService.registerNotice(dto);
        return "redirect:/notice/list?hotelId=" + dto.getFkHotelId();
    }
    
    // 5. 수정 페이지
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        model.addAttribute("hotelId", notice.getFkHotelId()); 
        return "js/notice/edit"; 
    }

    // 6. 수정 처리
    @PostMapping("/edit")
    public String updateNotice(NoticeDTO dto) {
        noticeService.updateNotice(dto);
        return "redirect:/notice/detail/" + dto.getNoticeId() + "?hotelId=" + dto.getFkHotelId();
    }
    
    // 7. 삭제 처리
    @PostMapping("/delete")
    public String deleteNotice(@RequestParam("noticeId") Long noticeId, RedirectAttributes rttr) {
        // 삭제 전 해당 글의 hotelId를 미리 가져오면 목록 이동 시 편리합니다.
        NoticeDTO notice = noticeService.getNoticeDetail(noticeId);
        Long hotelId = (notice != null) ? notice.getFkHotelId() : 0L;

        int result = noticeService.deleteNotice(noticeId);
        
        if(result > 0) {
            rttr.addFlashAttribute("message", "공지사항이 성공적으로 삭제되었습니다.");
        } else {
            rttr.addFlashAttribute("message", "삭제에 실패하였습니다.");
        }
        
        // 삭제 후 해당 지점 목록으로 이동하도록 개선
        return "redirect:/notice/list?hotelId=" + hotelId;
    }
}