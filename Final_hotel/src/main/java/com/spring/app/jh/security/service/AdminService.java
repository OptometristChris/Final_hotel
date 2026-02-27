package com.spring.app.jh.security.service;

import java.util.List;
import java.util.Map;

import com.spring.app.jh.security.domain.AdminDTO;

public interface AdminService {

	// ===== AdminUserDetailsService 에서 사용하는 메서드들 ==== //
	// !!!! 로그인 처리를 위해 사용되어지는 것임. !!!! //
	AdminDTO findByAdminid(String username);
	// ===== AdminUserDetailsService 에서 사용하는 메서드들 ==== //

	
	// ===== AdminController 에서 사용하는 메서드들 ==== //
	// 관리자 상세(내정보/상세보기)
	AdminDTO getAdminDetail(int admin_no);

	// 관리자 권한 목록
	List<String> getAdminAuthorities(int admin_no);

	// 내 프로필 수정(HQ/BRANCH 공통)
	int updateAdminProfile(AdminDTO adminDto);

	// BRANCH 관리자 목록(검색/페이징 틀)
	List<AdminDTO> getBranchAdminList(Map<String, String> paraMap);
	int getBranchAdminTotalCount(Map<String, String> paraMap);

	// BRANCH 관리자 발급(INSERT + 기본권한 부여)  ※ 비밀번호 해시 포함
	int insert_branchAdmin(AdminDTO adminDto) throws Exception;

	// HQ가 BRANCH 계정 정보 수정
	int updateAdminByHq(AdminDTO adminDto);

	// enabled 변경
	int updateAdminEnabled(Map<String, Object> paraMap);
	// ===== AdminController 에서 사용하는 메서드들 ==== //
	
	
	
	

}
