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

        int sizePerPage = 10;
        int startRow = (curPage - 1) * sizePerPage + 1;
        int endRow = startRow + sizePerPage - 1;

        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("hotelId", hotelId);
        paraMap.put("searchType", searchType);
        paraMap.put("keyword", keyword);
        paraMap.put("startRow", startRow);
        paraMap.put("endRow", endRow);

        // [추가] 고정글 리스트 가져오기 (isTop = 'Y')
        // 페이징과 관계없이 해당 지점(또는 전체)의 고정글을 항상 가져옵니다.
        List<NoticeDTO> topNotices = noticeService.getTopNotices(hotelId);
        
        // [수정] 일반글 리스트 가져오기 (isTop = 'N'만 가져오도록 SQL 수정 필요)
        List<NoticeDTO> notices = noticeService.getNoticeList(paraMap);
        
        // 2. 총 개수 가져오기 (isTop = 'N'인 데이터만 카운트하도록 SQL 수정 권장)
        int totalCount = noticeService.getTotalCount(paraMap);
        int totalPage = (int) Math.ceil((double) totalCount / sizePerPage);

        // 뷰로 전달할 데이터들
        model.addAttribute("topNotices", topNotices); // 고정글 별도 전달
        model.addAttribute("notices", notices);       // 일반글 전달
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
    public String updateNotice(NoticeDTO dto, RedirectAttributes rttr) {
    	int result = noticeService.updateNotice(dto);
    	
    	if(result > 0) {
            rttr.addFlashAttribute("message", "수정 완료.");
        } else {
            rttr.addFlashAttribute("message", "수정 실패.");
        }
    	
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