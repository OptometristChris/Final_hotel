package com.spring.app.jh.security.service;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder; // ★ 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private final PasswordEncoder passwordEncoder;  

	@Override
	public int member_id_check(String memberid) {
		int n = memberDao.member_id_check(memberid);
		return n;
	}

	@Override
	public int emailDuplicateCheck(String email) {
		int n = 0;
		// 암호화된 이메일 넣기
		try {
			n = memberDao.emailDuplicateCheck(aES256.encrypt(email));
		}  catch (UnsupportedEncodingException | GeneralSecurityException e) {
			e.printStackTrace();
		}
		
		return n;
	}

	

	@Transactional(rollbackFor = Exception.class)
	@Override
	public int insert_member(MemberDTO memberdto) throws Exception {
		
		// ★ 비밀번호 해시화(단방향 암호화)는 Service에서 처리한다.
		String hashedUserPwd = passwordEncoder.encode(memberdto.getPasswd());
		memberdto.setPasswd(hashedUserPwd);
		
		// 양방향 암호화 필요 : email, 휴대폰번호
		// email
		memberdto.setEmail(aES256.encrypt(memberdto.getEmail())); 
		// 휴대폰번호
		if(memberdto.getHp1() != null && memberdto.getHp1().trim().length() > 0 &&
		   memberdto.getHp2() != null && memberdto.getHp2().trim().length() > 0 &&
		   memberdto.getHp3() != null && memberdto.getHp3().trim().length() > 0 ) { // script 부분에서 유효성검사 끝내고 와서 원래 이 조건 필요없긴 하다.
			
			memberdto.setMobile( aES256.encrypt(memberdto.getHp1() + "-" + memberdto.getHp2() + "-" + memberdto.getHp3() ) );
		}
		

		int result = 0;
		// 2개의 테이블에 insert 진행 필요
		// 회원정보 저장하기(insert)
	    int n1 = memberDao.insert_member(memberdto); // selectKey로 memberNo 채워짐

	    if (n1 == 1) {
	        Integer memberNo = memberdto.getMemberNo();
	        result = memberDao.insert_member_authority_by_member_no(memberNo);

	        if (result != 1) {
	            throw new RuntimeException("회원 권한(ROLE_USER) 부여에 실패했습니다. memberNo=" + memberNo);
	        }
	    }

	    return result;
	}

	@Transactional
	@Override
	public int passwdChange(Map<String, String> paraMap) {
		
		// ★ 비밀번호 해시화(단방향 암호화)는 Service에서 처리한다.
		String hashedUserPwd = passwordEncoder.encode(paraMap.get("passwd"));
		paraMap.put("passwd", hashedUserPwd);
		
	    int n = memberDao.passwdChange(paraMap);

	    if(n == 1) {
	        memberDao.passwdModifyDate(paraMap.get("memberid"));
	    }
	    return n;
	}

	@Override
	public List<MemberDTO> getAllMember() {

		List<MemberDTO> memberDtoList = memberDao.getAllMember();

		if (memberDtoList == null) return null;

		List<MemberDTO> result = new ArrayList<>();

		for (MemberDTO memberDto : memberDtoList) {
			applyDecrypt(memberDto);
			applyAuthorities(memberDto);
			result.add(memberDto);
		}

		return result;
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
	public MemberDTO findByMemberid(String memberid) {

		MemberDTO memberDto = memberDao.findByMemberid(memberid);

		if (memberDto != null) {
			applyDecrypt(memberDto);
			applyAuthorities(memberDto);
		}

		return memberDto;
	}

	@Override
	public void insertLoginhistory(Integer memberNo, String clientip) {
		memberDao.insertLoginhistory(memberNo, clientip);
	}

	@Override
	public int update_member_profile(MemberDTO memberdto) {
		int n = memberDao.update_member_profile(memberdto);
		return n;
	}
	
	
	// =========================================================
	// private 공통 로직
	// =========================================================

	// === 이메일/휴대폰 복호화 ===
	// DB에는 암호문 저장을 전제로 하므로, 화면에 보여주거나 세션에 담기 전에 복호화한다.
	private void applyDecrypt(MemberDTO memberDto) {
		try {
			if (memberDto.getEmail() != null) {
				memberDto.setEmail(aES256.decrypt(memberDto.getEmail()));
			}
			if (memberDto.getMobile() != null) {
				memberDto.setMobile(aES256.decrypt(memberDto.getMobile()));
			}
		} catch (UnsupportedEncodingException | GeneralSecurityException e) {
			// 운영에서는 로거로 교체 권장
			e.printStackTrace();
		}
	}

	// === 권한(ROLE_*) 조회 및 세팅 ===
	// 우리 프로젝트는 권한 테이블이 memberid 기반이 아니라 member_no 기반이다.
	private void applyAuthorities(MemberDTO memberDto) {
		if (memberDto.getMemberNo() == null) return;

		List<String> authorityList = memberDao.authorityListByMemberNo(memberDto.getMemberNo());
		memberDto.setAuthorities(authorityList);
	}
	
}