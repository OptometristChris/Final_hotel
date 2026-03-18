package com.spring.app.js.notice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // [추가] 상세페이지에서도 탭이 보인다면 호텔 리스트가 필요함
        model.addAttribute("hotelList", noticeService.getHotelList());
        
        model.addAttribute("notice", noticeService.getNoticeDetail(id));
        model.addAttribute("hotelId", hotelId); 
        return "js/notice/detail";
    }
    
    // 3. 작성 페이지
    @GetMapping("/write")
    public String showWriteForm(@RequestParam(value = "hotelId", required = false, defaultValue = "1") Long hotelId, Model model) {
        // [추가] 작성 시 호텔 선택을 위해 리스트 전달
        model.addAttribute("hotelList", noticeService.getHotelList());
        
        model.addAttribute("hotelId", hotelId);
        return "js/notice/write";
    }

    // 이하 작성/수정/삭제 로직은 기존과 동일하되, 
    // 리다이렉트 시 정확한 hotelId를 유지하도록 되어 있어 그대로 사용하시면 됩니다.

    @PostMapping("/write")
    public String insertNotice(NoticeDTO dto) {
        if(dto.getAdminNo() == null) {
            dto.setAdminNo(2L); 
        }
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