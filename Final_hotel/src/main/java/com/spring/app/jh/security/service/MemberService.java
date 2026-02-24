package com.spring.app.jh.security.service;

import java.util.List;
import java.util.Map;

import com.spring.app.jh.security.domain.MemberDTO;

public interface MemberService {
	
	

	// ===== MemberController 에서 사용하는 메서드들 ==== //
	// id 중복 검사
	int member_id_check(String memberid);

	// 이메일 중복 검사
	int emailDuplicateCheck(String email);

	// 회원가입
	int insert_member(MemberDTO memberdto) throws Exception;

	// 비밀번호 변경
	int passwdChange(Map<String, String> paraMap);

	// 전체 회원 조회
	List<MemberDTO> getAllMember();
	
	// 회원정보 수정 저장
	int update_member_profile(MemberDTO memberdto);
	
	// 로그인한 사용자 정보(아이디와 성명)를 세션에 저장
	MemberDTO findByMemberid(String memberid);
	// ===== MemberController 에서 사용하는 메서드들 ==== //

	
	
	// ===== MemberAuthenticationSuccessHandler 에서 사용하는 메서드들 ==== //
	// 가장 최근 비밀번호 변경일자 조회
	int lastPasswdChangeMonth(String memberid);

	// 가장 최근 로그인 한 일자를 지금으로 변경
	void update_last_login(String memberid);

	// 로그인 기록 테이블에 최근 로그인 기록 저장
	void insertLoginhistory(Integer memberNo, String clientip);
	// ===== MemberAuthenticationSuccessHandler 에서 사용하는 메서드들 ==== //
	
	
	
	
	

	
	

	
	
	
	
}
