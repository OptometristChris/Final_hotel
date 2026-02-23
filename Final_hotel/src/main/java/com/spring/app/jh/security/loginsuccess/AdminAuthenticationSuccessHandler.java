package com.spring.app.jh.security.loginsuccess;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.spring.app.jh.security.domain.AdminDTO;
import com.spring.app.jh.security.domain.CustomAdminDetails;
import com.spring.app.jh.security.domain.Session_AdminDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;

/* ===== (#스프링시큐리티15-ADMIN) ===== */
// 관리자 로그인 성공후 세션 등 부가적인 처리를 할 수 있도록 해주는 클래스이다.

/*
   관리자도 회원과 동일하게 "로그인 성공 후 부가 처리"가 필요할 수 있다.
   (예: 마지막 로그인 업데이트, 로그인 히스토리 기록, 관리자 타입별 세션 저장 등)

   >>> redirect 처리 방식 <<<
   - Spring Security의 SavedRequest 기능을 그대로 사용한다.
   - SavedRequest가 있으면: 로그인 전 접근하려던 관리자 보호자원(/admin/**)으로 자동 이동
   - SavedRequest가 없으면: defaultTargetUrl로 이동
*/

@Getter
@Setter
public class AdminAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	public AdminAuthenticationSuccessHandler() { }

	public AdminAuthenticationSuccessHandler(String defaultTargetUrl) {
		super.setDefaultTargetUrl(defaultTargetUrl);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request
			                          , HttpServletResponse response
			                          , Authentication authentication) throws IOException, ServletException {

		// 관리자 principal 안전 체크
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof CustomAdminDetails)) {
			// 이 SuccessHandler는 "관리자 로그인 체인"에만 붙는 것이 정상이다.
			super.onAuthenticationSuccess(request, response, authentication);
			return;
		}

		CustomAdminDetails cad = (CustomAdminDetails) principal;
		AdminDTO adminDto = cad.getAdminDto(); // CustomAdminDetails가 AdminDTO를 감싸고 있다고 전제

		// >>> !!! 로그인한 관리자 정보(아이디/이름/타입/호텔FK)를 세션에 저장하도록 함. <<< !!!
		// Session_AdminDTO는 "세션에 넣을 최소 정보"만 담는다.
		Session_AdminDTO sessionAdminDTO = new Session_AdminDTO();
		sessionAdminDTO.setAdminid(adminDto.getAdminid());
		sessionAdminDTO.setName(adminDto.getName());
		sessionAdminDTO.setAdmin_type(adminDto.getAdmin_type());   // HQ / BRANCH
		sessionAdminDTO.setFk_hotel_id(adminDto.getFk_hotel_id()); // BRANCH면 NOT NULL, HQ면 null

		HttpSession session = request.getSession();
		session.setAttribute("sessionAdminDTO", sessionAdminDTO); // 키값이 sessionAdminDTO 임.

		/*
		   >>> redirect 처리 <<<
		   SavedRequestAwareAuthenticationSuccessHandler가 다음을 자동 수행한다.
		   1) SavedRequest가 있으면 원래 가려던 URL로 redirect
		   2) SavedRequest가 없으면 defaultTargetUrl로 redirect
		 */
		super.onAuthenticationSuccess(request, response, authentication);
	}

}