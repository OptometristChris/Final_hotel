package com.spring.app.jh.security.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.jh.security.domain.AdminDTO;
import com.spring.app.jh.security.domain.Session_AdminDTO;
import com.spring.app.jh.security.service.AdminService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티07-ADMIN-BRANCH) ===== */
@Controller
@RequiredArgsConstructor
@RequestMapping(value="/admin/branch/")
@PreAuthorize("hasRole('ADMIN_BRANCH')")  // ★ BRANCH 전용 컨트롤러(클래스 레벨로 고정)
public class AdminBranchController {

	private final AdminService adminService;

	/*
	   BRANCH 전용 기능

	   1) BRANCH 대시보드
	      - /admin/branch/dashboard

	   2) BRANCH 내 정보(프로필)
	      - /admin/branch/account/myInfo
	      - /admin/branch/account/profileEdit (GET/POST)
	*/


	// ============================================================
	// 0. BRANCH 대시보드
	// ============================================================

	@GetMapping("dashboard")
	public String dashboard() {

		return "admin/branch/branch_dashboard";
		// src/main/resources/templates/admin/branch/branch_dashboard.html
	}


	// ============================================================
	// 1. BRANCH 내 정보(프로필) 보기/수정
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

		return "admin/branch/account/myInfo";
		// src/main/resources/templates/admin/branch/account/myInfo.html
	}


	@GetMapping("account/profileEdit")
	public String profileEditForm(HttpSession session, Model model){

		Session_AdminDTO sad = (Session_AdminDTO) session.getAttribute("sessionAdminDTO");
		if(sad == null) {
			return "redirect:/admin/login";
		}

		AdminDTO adminDto = adminService.getAdminDetail(sad.getAdmin_no()); // TODO
		model.addAttribute("adminDto", adminDto);

		return "admin/branch/account/profileEditForm";
		// src/main/resources/templates/admin/branch/account/profileEditForm.html
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

		return "admin/branch/account/profileEditResult";
		// src/main/resources/templates/admin/branch/account/profileEditResult.html
	}

}