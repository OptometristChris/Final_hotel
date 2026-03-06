package com.spring.app.hk.admin.room.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.common.FileManager;
import com.spring.app.hk.admin.room.service.AdminRoomService;
import com.spring.app.hk.room.domain.RoomTypeDTO;
import com.spring.app.jh.security.domain.CustomAdminDetails;
import com.spring.app.jh.security.domain.Session_AdminDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/room")
public class AdminRoomController {

    private final AdminRoomService roomService;

    // 이미지 업로드 위해 추가
    private final FileManager fileManager;
    
    @Value("${file.images-dir}")
    private String imagesDir;
    
    // ==========================
    // 지점 관리자 객실 관리 페이지
    // ==========================
    
    // 지점 객실 조회
    @PreAuthorize("hasRole('ADMIN_BRANCH')")
    @GetMapping("/branch/list")
    public String roomList(Model model, HttpSession session) {
    	
    	// 로그인 사용자 가져오기
    	Session_AdminDTO loginAdmin = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");

    	if(loginAdmin == null){
            return "redirect:/admin/login";
        }
    	
    	Integer adminNo = loginAdmin.getAdmin_no();
    	
    	// 지점 객실 조회
        List<RoomTypeDTO> roomList = roomService.getRoomListByManager(adminNo);

        model.addAttribute("roomList", roomList);
        
        if(!roomList.isEmpty()){
            model.addAttribute("hotelName", roomList.get(0).getHotel_name());
        }

        return "hk/branch/room/roomlist";
    }
    

	 // 객실 등록 페이지 진입
	 @PreAuthorize("hasRole('ADMIN_BRANCH')")
	 @GetMapping("/register")
	 public String roomRegisterPage() {
	
	     return "hk/branch/room/roomRegister";
	 }
    
    
     // 지점 객실 등록 신청 (외부경로. 이미지까지)
	 @PreAuthorize("hasRole('ADMIN_BRANCH')")
	 @PostMapping("register")
	 public String registerRoom(
	         @RequestParam Map<String,String> map,
	         @RequestParam("roomImage") MultipartFile roomImage,
	         Authentication authentication
	 ) {

	     try {

	    	// 로그인한 관리자 정보 가져오기
	         CustomAdminDetails loginAdmin = (CustomAdminDetails) authentication.getPrincipal();

	         Integer adminNo = loginAdmin.getAdminDto().getAdmin_no();
	         String adminType = loginAdmin.getAdminDto().getAdmin_type();

	         // 관리자 번호
	         Map<String,Object> paraMap = new HashMap<>(map);

	         paraMap.put("admin_no", adminNo);

	         // 승인 상태
	         if("HQ".equals(adminType)){
	             paraMap.put("approve_status","APPROVED");
	         }else{
	             paraMap.put("approve_status","PENDING");
	         }

	         // 이미지 업로드
	         if(!roomImage.isEmpty()){
	        	 
	        	 // 업로드 위치 : file_images/room/파일명.jpg
	             String roomPath = imagesDir + File.separator + "room";

	             String savedName =
	                     fileManager.doFileUpload(
	                             roomImage.getBytes(),
	                             roomImage.getOriginalFilename(),
	                             roomPath);

	             paraMap.put("image_url", "/file_images/room/" + savedName);
	         }

	         roomService.saveRoom(paraMap);

	     } catch(Exception e){
	         e.printStackTrace();
	   
	     }

	      // 등록 후 객실 리스트 이동
	     return "redirect:/admin/room/branch/list";
	 
	 }
    
    // ==========================
    // 총괄 관리자 객실 관리 페이지 (승인/반려)
    // ==========================   
    // 승인요청 (지점관리자) 목록 조회
    @PreAuthorize("hasRole('ADMIN_HQ')")
    @GetMapping("/pending")
    public String pendingRoomList(Model model) {

        List<RoomTypeDTO> roomList = roomService.getPendingRoomList();

        model.addAttribute("roomList", roomList);

        return "hk/admin/room/pendingRoomList";
    }
    
    
    // 객실 승인 처리
    @PostMapping("/approve")
    public String approveRoom(@RequestParam int roomTypeId, HttpSession session) {

    	// 로그인 사용자 가져오기
        Session_AdminDTO loginAdmin = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");

        int adminId = loginAdmin.getAdmin_no();

        // 객실 승인
        roomService.approveRoom(roomTypeId, adminId);

        return "redirect:/admin/room/pending";
    }
    
    // 객실 반려 처리
    @PostMapping("/reject")
    public String rejectRoom(@RequestParam int roomTypeId, String reason, HttpSession session) {

    	// 로그인 사용자 가져오기
        Session_AdminDTO loginAdmin =  (Session_AdminDTO) session.getAttribute("sessionAdminDTO");

        int adminId = loginAdmin.getAdmin_no();

        // 객실 반려
        roomService.rejectRoom(roomTypeId, adminId, reason);

        return "redirect:/admin/room/pending";
    }
    
    
    
    
    
}