package com.spring.app.jh.security.model;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.spring.app.common.AES256;
import com.spring.app.jh.security.domain.AdminDTO;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminDAO_imple implements AdminDAO {

	@Override
	public AdminDTO findByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

}
