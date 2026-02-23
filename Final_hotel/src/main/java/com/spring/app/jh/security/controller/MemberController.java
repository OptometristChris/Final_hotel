package com.spring.app.jh.security.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.jh.security.domain.MemberDTO;
import com.spring.app.jh.security.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/* ===== (#스프링시큐리티07) ===== */
@Controller
@RequiredArgsConstructor  // @RequiredArgsConstructor는 Lombok 라이브러리에서 제공하는 애너테이션으로, final 필드 또는 @NonNull이 붙은 필드에 대해 생성자를 자동으로 생성해준다. 
@RequestMapping(value="/security/")
public class MemberController {
	
	private final MemberService memberService;	
	private final PasswordEncoder passwordEncoder; 	
	
	// 회원가입 form 페이지
	@GetMapping("memberRegister")
	public String memberRegister() {
		
		return "security/login/memberRegisterForm";
		// src/main/resources/templates/security/login/memberRegisterForm.html 파일 생성해줘야 함
	}
	
	// 약관 iframe
    @GetMapping("agree")
    public String memberAgree(){
      
       return "security/login/memberAgree"; 
       // src/main/resources/templates/security/login/memberAgree.html 파일 생성해줘야 함
    }
    
    // 아이디 중복검사
    @PostMapping("member_id_check")
    @ResponseBody
    public Map<String, Boolean> member_id_check(@RequestParam(name="memberid") String memberid){
    	// ajax이므로 map으로
    	int n = memberService.member_id_check(memberid);
    	
    	boolean isExists = false;
    	if (n==1) {
    		isExists = true;
		}
    	
    	Map<String, Boolean> map = new HashMap<>();
    	map.put("isExists", isExists);
    	
    	return map;
    }
    
    
    // email 중복검사
    @PostMapping("emailDuplicateCheck")
    @ResponseBody
    public Map<String, Boolean> emailDuplicateCheck(@RequestParam(name="email") String email){
    	// ajax이므로 map으로
    	int n = memberService.emailDuplicateCheck(email);
    	
    	boolean isExists = false;
    	if (n==1) {
    		isExists = true;
		}
    	
    	Map<String, Boolean> map = new HashMap<>();
    	map.put("isExists", isExists);
    	
    	return map;
    }
    
    
    // 회원가입, DB에 insert 하는 것
    @PostMapping("memberRegisterEnd")
    public String memberRegisterEnd(MemberDTO memberdto, Model model) {
    	
    	String hashedUserPwd = passwordEncoder.encode( memberdto.getPasswd());
    	
    	memberdto.setPasswd(hashedUserPwd);
    	
    	try {
			memberService.insert_member(memberdto);
			
			StringBuilder sb = new StringBuilder();
            sb.append("<span style='font-weight: bold;'>"+ memberdto.getName() + "</span>님의 회원 가입이 정상적으로 처리되었습니다.<br/>");
            sb.append("메인메뉴에서 로그인 하시기 바랍니다.<br/>");
            
            model.addAttribute("message", sb.toString());
		} catch (Exception e) {
			model.addAttribute("message", "장애가 발생되어 회원가입이 실패했습니다.");
			e.printStackTrace();
		}
    	
    	return "security/login/memberRegisterComplete";
    	// src/main/resources/templates/security/login/memberRegisterComplete.html 파일 생성해줘야 함
    }
    
    
    
    // 로그인 인증 form 페이지 보여주기
    @GetMapping(value="login")
    public String login(HttpServletRequest request){
       
       String referer = request.getHeader("referer"); 
       
       // referer가 null이다? -> 직접 url 타이핑 쳐 온것.
       if(referer == null || 
          referer.endsWith("/security/login") || 
          referer.endsWith("/security/login?loginFail=true") ||
          referer.endsWith("/security/noAuthenticated")) {
           referer = "http://localhost:9081/final_hotel/index";
       }
       
//       if(referer.endsWith("/board/view")) {
//           referer = "http://localhost:9081/final_hotel/list";
//       }
       
       HttpSession session = request.getSession();
       session.setAttribute("prevURLPage", referer);
       
        // login 실패여부 체크하기
       String loginFail = request.getParameter("loginFail");
       
       String msg = "";
       
       if("true".equals(loginFail)) {
          msg = "로그인 실패!! 아이디 또는 암호를 잘못 입력하셨습니다.";
       }
       
       request.setAttribute("msg", msg);
       
       return "security/login/loginform";
    // src/main/resources/templates/security/login/loginform.html 파일 생성해줘야 함
    }
    
    
    
    // 인증 실패시 URL  /* ===== (#스프링시큐리티16) ===== */
    @GetMapping(value="noAuthenticated")
    public String noAuthenticated(){
       
       return "security/noAuthenticated";
    // src/main/resources/templates/security/noAuthenticated.html 파일 생성해줘야 함
    }
    
    
    // 권한 실패시 URL  /* ===== (#스프링시큐리티17) ===== */
    @GetMapping(value="noAuthorized")
    public String noAuthorized(){
       
       return "security/noAuthorized";
    // src/main/resources/templates/security/noAuthorized.html 파일 생성해줘야 함
    }
    
    
  
    
    
    // 비밀번호 변경 form 페이지 보여주기
    @PreAuthorize("isAuthenticated()")  // 로그인 된 사용자만 허용
    @GetMapping(value="passwdChange")
    public String passwdChange(){
       
       return "security/member/passwdChangeForm";
    // src/main/resources/templates/security/member/passwdChangeForm.html 파일 생성해줘야 함
       
    }
    
    // 비밀번호 변경 하기
    @PreAuthorize("isAuthenticated()")  // 로그인 된 사용자만 허용
    @PostMapping(value="passwdChange")
    public String passwdChange(@RequestParam Map<String, String> paraMap, Model model){
       
       String hashedUserPwd = passwordEncoder.encode(paraMap.get("passwd"));
       paraMap.put("passwd", hashedUserPwd);
       
       int result = memberService.passwdChange(paraMap);
       model.addAttribute("result", result);
       
       return "security/member/passwdChangeResult";
    // src/main/resources/templates/security/member/passwdChangeResult.html 파일 생성해줘야 함
    }
    
    // 회원정보보기 URL
    @PreAuthorize("hasRole('ROLE_USER')") // 해당 권한이 있는 사람이 있는 사람만 허용
    @GetMapping(value="memberOnly")
    public String memberOnly(){
       
       return "security/member/memberOwnInfo";
    // src/main/resources/templates/security/member/memberOwnInfo.html 파일 생성해줘야 함
    }
    
    // 관리자 권한을 가진 사용자만 접근 가능한 URL
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value="admin/adminOnly")
    public String adminOnly(Model model){
       
       List<MemberDTO> memberDtoList = memberService.getAllMember();
       
       model.addAttribute("memberDtoList", memberDtoList);
       
       return "security/admin/memberAllInfo";
    // src/main/resources/templates/security/admin/memberAllInfo.html 파일 생성해줘야 함
    }
    
    
    
    
    
    
    
    // 누구나 접근 가능한 URL
    // 조건이 없으므로, 로그인을 하지 않은 상태이거나 또는 로그인을 성공한 상태 모두 메뉴가 보여지는 것이다 
    @GetMapping(value="everybody")
    public String everybody(){
       
       return "security/everybody";
       // src/main/resources/templates/security/everybody.html 파일 생성해줘야 함
    }
    
    
    // 회원만 접근 가능한 URL
    // exclude uri에 없기 때문에 자동으로 customAuthenticationEntryPoint() 로 흘러감
    @PreAuthorize("isAuthenticated()")  // 로그인 된 사용자만 허용
    @GetMapping(value="authenticatedUserOnly")
    public String authenticatedUserOnly(){
       
       return "security/authenticatedUserOnly";
    // src/main/resources/templates/security/authenticatedUserOnly.html 파일 생성해줘야 함
    }
    
    
    
    
    
    // 특별회원 권한이 있는 URL 
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER_SPECIAL')") // config 의 .requestMatchers("/security/special/**").hasAnyRole("ADMIN", "USER_SPECIAL") 와 겹치는 부분이지만 그냥 썼다.
    @GetMapping(value="special/special_memberOnly")
    public String special_memberOnly(){
       
       return "security/special_memberOnly";
    // src/main/resources/templates/security/special_memberOnly.html 파일 생성해줘야 함
    }
    
    
    
    
    
    
    
}
