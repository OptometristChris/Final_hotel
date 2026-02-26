package com.spring.app.jh.security.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.jh.security.domain.MemberDTO;

@Mapper
public interface MemberDAO {

	// ===== MemberUserDetailsService 에서 사용하는 메서드 ===== //
	MemberDTO findByMemberid(@Param("memberid") String username);
	// ===== MemberUserDetailsService 에서 사용하는 메서드 ===== //
	

	// ===== MemberController 에서 사용하는 메서드 ===== //
	int member_id_check(@Param("memberid") String memberid);

	int emailDuplicateCheck(@Param("email") String email);

	int insert_member(MemberDTO memberdto);

	int passwdChange(Map<String, String> paraMap);
	
	int passwdModifyDate(@Param("memberid") String memberid);

	List<MemberDTO> getAllMember();

	int lastPasswdChangeMonth(@Param("memberid") String memberid);

	void update_last_login(@Param("memberid") String memberid);

	void insertLoginhistory(@Param("memberNo") Integer memberNo,
            				@Param("clientip") String clientip);
	
	int insert_member_authority_by_member_no(@Param("memberNo") Integer memberNo);
	
	List<String> authorityListByMemberNo(@Param("memberNo") Integer memberNo);
	
	int update_member_profile(MemberDTO memberdto);
	// ===== MemberController 에서 사용하는 메서드 ===== //


	


	

}
