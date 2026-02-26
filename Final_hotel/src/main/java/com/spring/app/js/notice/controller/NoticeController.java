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

    @GetMapping("/list")
    public String list(@RequestParam(value = "hotelId",required = false, defaultValue = "0") Long hotelId, Model model) {
        List<NoticeDTO> list = noticeService.getNoticeList(hotelId);
        model.addAttribute("notices", list);
        model.addAttribute("hotelId", hotelId);
        return "js/notice/list"; // notice/list.html 로 이동
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, 
                         @RequestParam(value = "hotelId", defaultValue = "0") Long hotelId, 
                         Model model) {
        model.addAttribute("notice", noticeService.getNoticeDetail(id));
        model.addAttribute("hotelId", hotelId); // 이 값이 html의 목록보기 버튼에 쓰임
        return "js/notice/detail";
    }
    
 // 1. 작성 페이지 띄우기
    @GetMapping("/write")
    public String showWriteForm(@RequestParam(value = "hotelId", required = false, defaultValue = "1") Long hotelId, Model model) {
        // 테스트를 위해 임시 호텔 ID 1번 전달
    	model.addAttribute("hotelId", hotelId);
        return "js/notice/write";
    }

    // 2. 작성 완료 후 데이터 처리
    @PostMapping("/write")
    public String insertNotice(NoticeDTO dto) {
        // 세션 구현 전까지 테스트를 위해 관리자 번호 임시 세팅 (DB에 존재하는 관리자 번호여야 함)
        if(dto.getAdminNo() == null) {
            dto.setAdminNo(2L); 
        }
        
        noticeService.registerNotice(dto);
        return "redirect:/notice/list?hotelId=" + dto.getFkHotelId();
    }
    
    // 3. 수정 페이지 띄우기
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        NoticeDTO notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        // 💡 중요: 수정을 마치고 돌아갈 때 hotelId가 필요할 수 있으므로 명시적으로 전달
        model.addAttribute("hotelId", notice.getFkHotelId()); 
        return "js/notice/edit"; 
    }

    // 4. 수정 처리
    @PostMapping("/edit")
    public String updateNotice(NoticeDTO dto) {
        noticeService.updateNotice(dto);
        // 💡 수정 완료 후 상세페이지로 이동할 때 hotelId를 쿼리스트링으로 붙여주는 것이 안전합니다.
        return "redirect:/notice/detail/" + dto.getNoticeId() + "?hotelId=" + dto.getFkHotelId();
    }
    
    @PostMapping("/delete")
    public String deleteNotice(@RequestParam("noticeId") Long noticeId, RedirectAttributes rttr) {
        
        int result = noticeService.deleteNotice(noticeId);
        
        if(result > 0) {
            rttr.addFlashAttribute("message", "공지사항이 성공적으로 삭제되었습니다.");
        } else {
            rttr.addFlashAttribute("message", "삭제에 실패하였습니다.");
        }
        
        // 삭제 후 목록 페이지로 이동 (hotelId 파라미터가 필요하다면 추가)
        return "redirect:/notice/list";
    }
    
    
}
