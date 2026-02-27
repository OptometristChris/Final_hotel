package com.spring.app.jh.security.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.jh.security.domain.AdminDTO;

@Mapper
public interface AdminDAO {

	// ===== AdminUserDetailsService 에서 사용하는 메서드 ===== //
	AdminDTO findByAdminid(@Param("adminid") String adminid);
	// ===== AdminUserDetailsService 에서 사용하는 메서드 ===== //
	
	
	// ===== AdminController 에서 사용하는 메서드 ===== //
	List<String> authorityListByAdminNo(@Param("admin_no") Integer admin_no);
	
	AdminDTO findByAdminNo(@Param("admin_no") int admin_no);

	int updateAdminProfile(AdminDTO adminDto);

	List<AdminDTO> getBranchAdminList(Map<String, String> paraMap);

	int getBranchAdminTotalCount(Map<String, String> paraMap);

	int insertBranchAdmin(AdminDTO adminDto);

	int insertBranchAdminAuthority(@Param("admin_no") Integer adminNo,
								   @Param("string") String string);

	int updateAdminByHq(AdminDTO adminDto);

	int updateAdminEnabled(Map<String, Object> paraMap);
	// ===== AdminController 에서 사용하는 메서드 ===== //


	


	

}
