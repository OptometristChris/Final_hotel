package com.spring.app.jh.security.model;

import com.spring.app.jh.security.domain.AdminDTO;


public interface AdminDAO {

	// ===== AdminUserDetailsService 에서 사용하는 메서드 ===== //
	AdminDTO findByUsername(String username);
	// ===== AdminUserDetailsService 에서 사용하는 메서드 ===== //

}
