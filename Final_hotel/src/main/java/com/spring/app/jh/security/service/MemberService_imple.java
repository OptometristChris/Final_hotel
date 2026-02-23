package com.spring.app.jh.security.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.spring.app.common.AES256;
import com.spring.app.jh.security.domain.MemberDTO;
import com.spring.app.jh.security.model.MemberDAO;

import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티09) ===== */
@Service
@RequiredArgsConstructor
public class MemberService_imple implements MemberService {
	
	private final MemberDAO memberDao;
	private final AES256 aES256;

	@Override
	public int member_id_check(String memberid) {
		int n = memberDao.member_id_check(memberid);
		return n;
	}

	@Override
	public int emailDuplicateCheck(String email) {
		int n = memberDao.emailDuplicateCheck(email);
		return n;
	}

	@Override
	public void insert_member(MemberDTO memberdto) {
		memberDao.insert_member(memberdto);
	}

	@Override
	public int passwdChange(Map<String, String> paraMap) {
		int n = memberDao.passwdChange(paraMap);
		return n;
	}

	@Override
	public List<MemberDTO> getAllMember() {
		List<MemberDTO> memberDtoList = memberDao.getAllMember();
		return memberDtoList;
	}

	@Override
	public int lastPasswdChangeMonth(String memberid) {
		int n = memberDao.lastPasswdChangeMonth(memberid);
		return n;
	}

	@Override
	public void update_last_login(String memberid) {
		memberDao.update_last_login(memberid);
	}

	@Override
	public MemberDTO findByUsername(String memberid) {
		MemberDTO memberDto = memberDao.findByUsername(memberid);
	    return memberDto;
	}

	@Override
	public void insertLoginhistory(Integer memberNo, String clientip) {
		memberDao.insertLoginhistory(memberNo, clientip);
	}
	
}
