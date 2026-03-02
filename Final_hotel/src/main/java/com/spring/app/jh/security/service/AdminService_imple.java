package com.spring.app.jh.security.service;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.common.AES256;
import com.spring.app.jh.security.domain.AdminDTO;
import com.spring.app.jh.security.model.AdminDAO;

import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티09-ADMIN) ===== */
@Service
@RequiredArgsConstructor
public class AdminService_imple implements AdminService {

	private final AdminDAO adminDao;
	private final AES256 aES256;
	private final PasswordEncoder passwordEncoder; 

	@Override
	public AdminDTO findByAdminid(String adminid) {

		AdminDTO adminDto = adminDao.findByAdminid(adminid);

		if (adminDto != null) {
			applyDecrypt(adminDto);
			applyAuthorities(adminDto);
		}

		return adminDto;
	}

	// =========================================================
	// Controller(관리자 계정관리)에서 쓰는 메서드들
	// =========================================================

	@Override
	public AdminDTO getAdminDetail(int admin_no) {

		AdminDTO adminDto = adminDao.findByAdminNo(admin_no);

		if (adminDto != null) {
			applyDecrypt(adminDto);
			applyAuthorities(adminDto);
		}

		return adminDto;
	}

	@Override
	public List<String> getAdminAuthorities(int admin_no) {
		return adminDao.authorityListByAdminNo(admin_no);
	}

	@Override
	public int updateAdminProfile(AdminDTO adminDto) {

		/*
		   내정보 수정:
		   - 보통 name/email/mobile 정도만
		   - DB에 email/mobile 암호문 저장 전제이므로, update 전에 encrypt 필요
		 */

		applyEncrypt(adminDto);

		return adminDao.updateAdminProfile(adminDto);
	}

	@Override
	public List<AdminDTO> getBranchAdminList(Map<String, String> paraMap) {

		List<AdminDTO> list = adminDao.getBranchAdminList(paraMap);

		// 목록 화면에서도 email/mobile이 필요하면 복호화
		if (list != null) {
			for (AdminDTO dto : list) {
				applyDecrypt(dto);
			}
		}

		return list;
	}

	@Override
	public int getBranchAdminTotalCount(Map<String, String> paraMap) {
		return adminDao.getBranchAdminTotalCount(paraMap);
	}

	@Transactional
	@Override
	public int insert_branchAdmin(AdminDTO adminDto) throws Exception {

	    // 1) 규칙 강제
	    adminDto.setAdmin_type("BRANCH");

	    // 2) 비밀번호 해시화
	    String hashed = passwordEncoder.encode(adminDto.getPasswd());
	    adminDto.setPasswd(hashed);

	    // 3) (DB 암호문 저장 전제면) email/mobile 암호화
	    adminDto.setEmail(aES256.encrypt(adminDto.getEmail()));
	    if(adminDto.getMobile() != null && adminDto.getMobile().trim().length() > 0) {
	        adminDto.setMobile(aES256.encrypt(adminDto.getMobile()));
	    }

	    int result = 0;

	    // 4) 관리자 insert (selectKey로 admin_no 채워져야 함)
	    int n1 = adminDao.insertBranchAdmin(adminDto);

	    if(n1 == 1) {
	        Integer adminNo = adminDto.getAdmin_no();

	        // 5) 기본 권한 부여(ROLE_ADMIN_BRANCH)
	        result = adminDao.insertBranchAdminAuthority(adminNo, "ROLE_ADMIN_BRANCH");

	        if(result != 1) {
	            throw new RuntimeException("관리자 권한(ROLE_ADMIN_BRANCH) 부여 실패. adminNo=" + adminNo);
	        }
	    }

	    return result;
	}

	@Override
	public int updateAdminByHq(AdminDTO adminDto) {

		applyEncrypt(adminDto);

		return adminDao.updateAdminByHq(adminDto);
	}

	@Override
	public int updateAdminEnabled(Map<String, Object> paraMap) {
		return adminDao.updateAdminEnabled(paraMap);
	}

	
	
	
	
	// =========================================================
	// private 공통 로직
	// =========================================================

	// === 이메일/휴대폰 복호화 ===
	// DB에는 암호문 저장을 전제로 하므로, 화면에 보여주거나 세션에 담기 전에 복호화한다.
	private void applyDecrypt(AdminDTO adminDto) {
		try {
			if (adminDto.getEmail() != null) {
				adminDto.setEmail(aES256.decrypt(adminDto.getEmail()));
			}
			if (adminDto.getMobile() != null) {
				adminDto.setMobile(aES256.decrypt(adminDto.getMobile()));
			}
		} catch (UnsupportedEncodingException | GeneralSecurityException e) {
			// 운영에서는 로거로 교체 권장
			e.printStackTrace();
		}
	}

	// === 이메일/휴대폰 암호화 ===
	// DB에 암호문 저장 전제라면 INSERT/UPDATE 전에 encrypt 해야 한다.
	private void applyEncrypt(AdminDTO adminDto) {
		try {
			if (adminDto.getEmail() != null && !adminDto.getEmail().isEmpty()) {
				adminDto.setEmail(aES256.encrypt(adminDto.getEmail()));
			}
			if (adminDto.getMobile() != null && !adminDto.getMobile().isEmpty()) {
				adminDto.setMobile(aES256.encrypt(adminDto.getMobile()));
			}
		} catch (UnsupportedEncodingException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	// === 권한(ROLE_*) 조회 및 세팅 ===
	// 우리 프로젝트는 권한 테이블이 adminid 기반이 아니라 admin_no 기반이다.
	private void applyAuthorities(AdminDTO adminDto) {
		if (adminDto.getAdmin_no() == null) return;

		List<String> authorityList = adminDao.authorityListByAdminNo(adminDto.getAdmin_no());

		// null 방지
		if (authorityList == null) {
			authorityList = new ArrayList<>();
		}

		adminDto.setAuthorities(authorityList);
	}

}