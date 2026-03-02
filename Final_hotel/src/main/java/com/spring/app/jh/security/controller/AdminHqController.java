package com.spring.app.jh.security.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.jh.security.domain.AdminDTO;
import com.spring.app.jh.security.domain.Session_AdminDTO;
import com.spring.app.jh.security.service.AdminService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티07-ADMIN-HQ) ===== */
@Controller
@RequiredArgsConstructor
@RequestMapping(value="/admin/hq/")
@PreAuthorize("hasRole('ADMIN_HQ')")  // ★ HQ 전용 컨트롤러(클래스 레벨로 고정)
public class AdminHqController {

	private final AdminService adminService;

	/*
	   HQ 전용 기능

	   1) HQ 대시보드
	      - /admin/hq/dashboard

	   2) HQ 내 정보(프로필)
	      - /admin/hq/account/myInfo
	      - /admin/hq/account/profileEdit (GET/POST)

	   3) BRANCH 관리자 계정 발급/관리
	      - /admin/hq/admins/**

	   ※ 로그인 성공 후 세션 저장/redirect는 AdminAuthenticationSuccessHandler 에서 처리한다.
	*/


	// ============================================================
	// 0. HQ 대시보드
	// ============================================================

	@GetMapping("dashboard")
	public String dashboard() {

		return "admin/hq/hq_dashboard";
		// src/main/resources/templates/admin/hq/hq_dashboard.html
	}


	// ============================================================
	// 1. HQ 내 정보(프로필) 보기/수정
	// ============================================================

	@GetMapping("account/myInfo")
	public String myInfo(HttpSession session, Model model){

		Session_AdminDTO sad = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");

		// 세션이 없다면 비정상 -> 다시 로그인
		if(sad == null) {
			return "redirect:/admin/login";
		}

		AdminDTO adminDto = adminService.getAdminDetail(sad.getAdmin_no()); // TODO
		model.addAttribute("adminDto", adminDto);

		return "admin/hq/account/myInfo";
		// src/main/resources/templates/admin/hq/account/myInfo.html
	}


	@GetMapping("account/profileEdit")
	public String profileEditForm(HttpSession session, Model model){

		Session_AdminDTO sad = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");
		if(sad == null) {
			return "redirect:/admin/login";
		}

		AdminDTO adminDto = adminService.getAdminDetail(sad.getAdmin_no()); // TODO
		model.addAttribute("adminDto", adminDto);

		return "admin/hq/account/profileEditForm";
		// src/main/resources/templates/admin/hq/account/profileEditForm.html
	}


	@PostMapping("account/profileEdit")
	public String profileEditEnd(AdminDTO adminDto, HttpSession session, Model model){

		/*
		   ★ 핵심: 어떤 관리자인지(PK=admin_no)는 세션에서 강제 주입해야 한다.
		   - 사용자가 form에서 admin_no를 조작하는 것을 막기 위함.
		*/
		Session_AdminDTO sad = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");
		if(sad == null) {
			return "redirect:/admin/login";
		}

		adminDto.setAdmin_no(sad.getAdmin_no());

		int n = adminService.updateAdminProfile(adminDto); // TODO (name/email/mobile 등)
		model.addAttribute("result", n);

		return "admin/hq/account/profileEditResult";
		// src/main/resources/templates/admin/hq/account/profileEditResult.html
	}


	// ============================================================
	// 2. HQ 전용: BRANCH 계정 발급/목록/상세/수정/활성화
	// ============================================================

	// BRANCH 계정 목록(호텔별/검색/페이징 틀)
	@GetMapping("admins")
	public String branchAdminList(@RequestParam Map<String, String> paraMap, Model model) {

	    // 1) pageNo 기본값
	    String pageNo = paraMap.get("pageNo");
	    if (pageNo == null || pageNo.trim().isEmpty()) {
	        paraMap.put("pageNo", "1");
	    }

	    // 2) sizePerPage 기본값
	    String sizePerPage = paraMap.get("sizePerPage");
	    if (sizePerPage == null || sizePerPage.trim().isEmpty()) {
	        paraMap.put("sizePerPage", "10");
	    }

	    // enabled 기본값
	    if (!paraMap.containsKey("enabled")) paraMap.put("enabled", "ALL");
	    
	    List<AdminDTO> adminList = adminService.getBranchAdminList(paraMap);
	    int totalCount = adminService.getBranchAdminTotalCount(paraMap);

	    model.addAttribute("adminList", adminList);
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("paraMap", paraMap);

	    return "admin/hq/admin_list";
	}


	// BRANCH 계정 발급 form
	@GetMapping("admins/new")
	public String branchAdminCreateForm(){

		return "admin/hq/admin_create_form";
		// src/main/resources/templates/admin/hq/admin_create_form.html
	}


	// BRANCH 계정 발급 처리(INSERT + 기본 권한 부여)
	@PostMapping("admins")
	public String branchAdminCreateEnd(AdminDTO adminDto, Model model) {

	    try {
	        int n = adminService.insert_branchAdmin(adminDto);

	        if(n == 1) {
	            model.addAttribute("message", "지점 관리자 계정 발급이 정상적으로 처리되었습니다.");
	        }
	        else {
	            model.addAttribute("message", "지점 관리자 계정 발급이 실패했습니다.");
	        }
	    }
	    catch (Exception e) {
	        model.addAttribute("message", e.getMessage() != null ? e.getMessage()
	                : "지점 관리자 계정 발급 중 장애가 발생했습니다.");

	        e.printStackTrace();
	    }

	    return "admin/hq/admin_create_result";
	    // src/main/resources/templates/admin/hq/admin_create_result.html
	}


	// 관리자 상세(보통 HQ가 BRANCH 계정 클릭해서 조회)
	@GetMapping("admins/detail/{admin_no}")
	public String adminDetail(@PathVariable("admin_no") int admin_no, Model model){

		AdminDTO adminDto = adminService.getAdminDetail(admin_no); // TODO
		List<String> authorityList = adminService.getAdminAuthorities(admin_no); // TODO(선택)

		model.addAttribute("adminDto", adminDto);
		model.addAttribute("authorityList", authorityList);

		return "admin/hq/admin_detail";
		// src/main/resources/templates/admin/hq/admin_detail.html
	}


	// 관리자 정보 수정(보통 HQ가 BRANCH 계정 정보 수정)
	@PostMapping("admins/update")
	public String adminUpdateEnd(AdminDTO adminDto, Model model){

		int n = adminService.updateAdminByHq(adminDto);
		model.addAttribute("result", n);

		return "admin/hq/admin_update_result";
		// src/main/resources/templates/admin/hq/admin_update_result.html
	}


	// 관리자 활성/비활성(enabled) 변경
	@PostMapping("admins/{admin_no}/enabled")
	@ResponseBody
	public Map<String, Object> updateAdminEnabled(@PathVariable("admin_no") int admin_no,
												  @RequestParam(name="enabled") String enabled){

		/*
		   enabled:
		   - '1' : 활성
		   - '0' : 중지
		*/

		Map<String, Object> paraMap = new HashMap<>();
		paraMap.put("admin_no", admin_no);
		paraMap.put("enabled", enabled);

		int n = adminService.updateAdminEnabled(paraMap); // TODO

		Map<String, Object> result = new HashMap<>();
		result.put("result", n);
		return result;
	}

}