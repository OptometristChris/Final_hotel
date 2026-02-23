package com.spring.app.jh.security.model;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.spring.app.common.AES256;
import com.spring.app.jh.security.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티10) ===== */
@Repository
@RequiredArgsConstructor  // @RequiredArgsConstructor는 Lombok 라이브러리에서 제공하는 애너테이션으로, final 필드 또는 @NonNull이 붙은 필드에 대해 생성자를 자동으로 생성해준다.
public class MemberDAO_imple implements MemberDAO {

	// DataSourceConfig 에서 @Bean(name="sqlSessionTemplate") 로 등록해둔 Bean 이 주입된다.
	// 현재 프로젝트는 SqlSessionTemplate Bean 이 1개이므로 @Qualifier 지정 없이도 주입 가능하다.
	private final SqlSessionTemplate sqlSessionTemplate;

	private final AES256 aES256;
	// jh.com.spring.app.common.AES256 의 bean 은 별도의 Configuration 클래스에서 Bean 으로 등록되어 있어야 한다.
	// (수업용과 동일하게 @Component를 붙이면 기본생성자 문제로 오류가 날 수 있으므로 Configuration Bean 등록 방식을 사용한다.)

	@Override
	public MemberDTO findByUsername(String username) {

		// username 은 아이디(회원 memberid) 를 뜻한다.
		MemberDTO memberDto = sqlSessionTemplate.selectOne("security_member.findByUsername", username);

		if (memberDto != null) {

			// === 이메일/휴대폰 복호화 ===
			// DB에는 암호문 저장을 전제로 하므로, 화면에 보여주거나 세션에 담기 전에 복호화한다.
			try {
				if (memberDto.getEmail() != null) {
					memberDto.setEmail(aES256.decrypt(memberDto.getEmail()));   // 이메일 복호화
				}
				if (memberDto.getMobile() != null) {
					memberDto.setMobile(aES256.decrypt(memberDto.getMobile())); // 휴대폰 복호화
				}
			} catch (UnsupportedEncodingException | GeneralSecurityException e) {
				e.printStackTrace();
			}

			// === 권한(ROLE_*) 조회 및 세팅 ===
			// 우리 프로젝트는 권한 테이블이 memberid 기반이 아니라 member_no 기반이다.
			List<String> authorityList =
					sqlSessionTemplate.selectList("security_member.authorityListByMemberNo", memberDto.getMemberNo());

			memberDto.setAuthorities(authorityList); // 권한부여
		}

		return memberDto;
	}

	@Override
	public int member_id_check(String memberid) {

		int n = sqlSessionTemplate.selectOne("security_member.member_id_check", memberid);

		return n;
	}

	@Override
	public int emailDuplicateCheck(String email) {

		int n = sqlSessionTemplate.selectOne("security_member.emailDuplicateCheck", email);

		return n;
	}

	@Override
	public void insert_member(MemberDTO memberdto) {
		// security_member.insert_member 는 selectKey 로 memberNo 를 채워준다.
		// 따라서 insert 후 memberdto.getMemberNo() 를 바로 사용할 수 있다.
		sqlSessionTemplate.insert("security_member.insert_member", memberdto);
	}

	@Override
	public int passwdChange(Map<String, String> paraMap) {

		int n = sqlSessionTemplate.update("security_member.passwdChange", paraMap);

		return n;
	}

	@Override
	public List<MemberDTO> getAllMember() {

		List<MemberDTO> memberDtoList = sqlSessionTemplate.selectList("security_member.getAllMember");

		List<MemberDTO> list = null;

		if (memberDtoList != null) {

			list = new ArrayList<>();

			for (MemberDTO memberDto : memberDtoList) {

				// === 이메일/휴대폰 복호화 ===
				try {
					if (memberDto.getEmail() != null) {
						memberDto.setEmail(aES256.decrypt(memberDto.getEmail()));   // 이메일 복호화
					}
					if (memberDto.getMobile() != null) {
						memberDto.setMobile(aES256.decrypt(memberDto.getMobile())); // 휴대폰 복호화
					}
				} catch (UnsupportedEncodingException | GeneralSecurityException e) {
					e.printStackTrace();
				}

				// === 권한 조회(우리 프로젝트는 member_no 기반) ===
				List<String> authorityList =
						sqlSessionTemplate.selectList("security_member.authorityListByMemberNo", memberDto.getMemberNo());

				memberDto.setAuthorities(authorityList); // 권한부여

				list.add(memberDto);
			} // end of for--------------------
		}

		return list;
	}

	@Override
	public int lastPasswdChangeMonth(String memberid) {

		int n = sqlSessionTemplate.selectOne("security_member.lastPasswdChangeMonth", memberid);

		return n;
	}

	@Override
	public void update_last_login(String memberid) {

		sqlSessionTemplate.update("security_member.update_last_login", memberid);
	}

	@Override
	public void insertLoginhistory(Integer memberNo, String clientip) {

		// 우리 프로젝트 DDL 기준으로 tbl_loginhistory 는 memberid 가 아니라 member_no(FK) 로 저장한다.
		// 따라서 insertLoginhistory 는 (memberNo, clientip) 형태로 받도록 설계하는 것이 깔끔하다.
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("member_no", memberNo);
		paramMap.put("clientip", clientip);

		sqlSessionTemplate.insert("security_member.insertLoginhistory", paramMap);
	}

}