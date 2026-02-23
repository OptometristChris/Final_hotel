package com.spring.app.jh.security.model;

import java.util.List;
import java.util.Map;

import com.spring.app.jh.security.domain.MemberDTO;

public interface MemberDAO {

	// ===== MemberUserDetailsService 에서 사용하는 메서드 ===== //
	MemberDTO findByUsername(String username);
	// ===== MemberUserDetailsService 에서 사용하는 메서드 ===== //
	

	// ===== MemberService_imple 에서 사용하는 메서드 ===== //
	int member_id_check(String memberid);

	int emailDuplicateCheck(String email);

	void insert_member(MemberDTO memberdto);

	int passwdChange(Map<String, String> paraMap);

	List<MemberDTO> getAllMember();

	int lastPasswdChangeMonth(String memberid);

	void update_last_login(String memberid);

	void insertLoginhistory(Integer memberNo, String clientip);
	// ===== MemberService_imple 에서 사용하는 메서드 ===== //

}
