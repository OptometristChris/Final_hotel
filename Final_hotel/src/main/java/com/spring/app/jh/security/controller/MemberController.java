package com.spring.app.jh.security.controller;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;  // ★ 제거
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.common.AES256;
import com.spring.app.jh.security.domain.MemberDTO;
import com.spring.app.jh.security.domain.Session_MemberDTO;
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
	// private final PasswordEncoder passwordEncoder; 	// ★ 제거
	
	// 회원가입 form 페이지
	@GetMapping("memberRegister")
	public String memberRegister(HttpServletRequest request) {
		
		// 이미 로그인 했다면 인덱스로
       HttpSession session = request.getSession(false);
	   if(session != null && session.getAttribute("sessionMemberDTO") != null) {
	       return "redirect:/index";
	   }
		
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


        try {
            int n = memberService.insert_member(memberdto); 

            if (n == 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("<span style='font-weight: bold;'>")
                  .append(memberdto.getName())
                  .append("</span>님의 회원 가입이 정상적으로 처리되었습니다.<br/>");
                sb.append("메인메뉴에서 로그인 하시기 바랍니다.<br/>");

                model.addAttribute("message", sb.toString());
            }
            else {
                // insert가 0건이면 보통 뭔가 문제(조건 불일치/트리거/제약/쿼리 문제 등)
                model.addAttribute("message", "장애가 발생되어 회원가입이 실패했습니다.");
            }
        }
        catch (Exception e) {
            model.addAttribute("message", "장애가 발생되어 회원가입이 실패했습니다.");
            e.printStackTrace();
        }

        return "security/login/memberRegisterComplete";
    }
    
    
    // 로그인 인증 form 페이지 보여주기
    @GetMapping(value="login")
    public String login(HttpServletRequest request){

       /*
          ★ SavedRequest 방식 사용 ★
          - 로그인 이전에 보호자원에 접근했다면 Spring Security 가 SavedRequest 로 "원래 가려던 URL" 을 저장한다.
          - 로그인 성공 시 MemberAuthenticationSuccessHandler(SavedRequestAwareAuthenticationSuccessHandler)가
            SavedRequest 를 읽어 원래 가려던 페이지로 자동 redirect 한다.
          - 따라서 여기서 referer 를 세션에 prevURLPage 로 저장하는 방식은 제거한다.
       */
    	
       
       // 이미 로그인 했다면 인덱스로
       HttpSession session = request.getSession(false);
	   if(session != null && session.getAttribute("sessionMemberDTO") != null) {
	       return "redirect:/index";
	   }
    	
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
    
    
    // id 찾기 form
    @GetMapping("member_id_find")
    public String memberIdFindForm(HttpServletRequest request) {

        // 필요 시 안내 메시지(선택)
        // request.setAttribute("msg", "");

        return "security/login/member_id_find";
        // src/main/resources/templates/security/login/member_id_find.html
    }
    
    
    // id 찾기 처리
    @PostMapping("member_id_find")
    public String memberIdFindEnd(@RequestParam Map<String, String> paraMap,
                                  Model model) {

        /*
          paraMap 예시(너가 폼에서 어떤 input name을 쓰는지에 따라):
          - name
          - email
          - mobile

          ※ 컨트롤러에서 암/복호화 하지 말고 "있는 그대로" 서비스로 전달.
             (DB가 암호화 컬럼이면 서비스/DAO가 통일해서 처리)
        */

        // TODO: service 구현 필요
        // 예) String memberid = memberService.findMemberId(paraMap);
        String memberid = memberService.findMemberId(paraMap);

        if (memberid == null || memberid.trim().isEmpty()) {
            model.addAttribute("isFound", false);
            model.addAttribute("message", "일치하는 회원 정보가 없습니다. 입력 정보를 다시 확인하세요.");
        }
        else {
            model.addAttribute("isFound", true);
            model.addAttribute("memberid", memberid);
        }

        return "security/login/member_id_find_result";
        // src/main/resources/templates/security/login/member_id_find_result.html
    }
    
    // 비밀번호 찾기 form
    @GetMapping("member_pw_find")
    public String memberPwFindForm(HttpServletRequest request) {

        return "security/login/member_pw_find";
        // src/main/resources/templates/security/login/member_pw_find.html
    }
    
    // 비밀번호 찾기 처리
    @PostMapping("member_pw_find")
    public String memberPwFindEnd(@RequestParam Map<String, String> paraMap,
                                  Model model) {

        /*
          paraMap 예시:
          - memberid
          - email (또는 mobile)
          - name (선택)

          구현 방향(둘 중 하나 고르면 됨):
          A) 임시비밀번호 발급 → DB에 BCrypt 해시로 저장 → 이메일/문자로 전달
          B) 재설정 토큰 발급 → 재설정 페이지에서 새 비번 입력받아 저장(더 권장)

          지금 단계에서는 우선 A로 가는 경우가 많음.
        */

        // 비밀번호 리셋을 위해 기입한 정보로 신원 특정
        boolean isVerified = memberService.verifyMemberForPwReset(paraMap);

        if (!isVerified) {
            model.addAttribute("isSuccess", false);
            model.addAttribute("message", "입력하신 정보와 일치하는 회원이 없습니다.");
            return "security/login/member_pw_find_result";
        }

        // 이메일 발송 없이 그냥 임시비밀번호 발급 + DB 업데이트
        // String tmp = memberService.issueTempPasswordAndUpdate(paraMap);
        
        // 해당 회원의 비밀번호 초기화 후 임시비밀번호 발급 + DB 업데이트 + 이메일로 발송
        boolean ok = memberService.issueTempPasswordAndSendEmail(paraMap);

        if (!ok) {
            model.addAttribute("isSuccess", false);
            model.addAttribute("message", "입력하신 정보와 일치하는 회원이 없거나 이메일 발송에 실패했습니다.");
        }
        else {
            model.addAttribute("isSuccess", true);
            model.addAttribute("message", "임시 비밀번호를 이메일로 발송했습니다. 메일 확인 후 로그인하세요.");
            // model.addAttribute("tempPw", ...)  <-- 제거
        }

        return "security/login/member_pw_find_result";
        // src/main/resources/templates/security/login/member_pw_find_result.html
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
       
       // String hashedUserPwd = passwordEncoder.encode(paraMap.get("passwd"));  // ★ 제거
       // paraMap.put("passwd", hashedUserPwd);                                  // ★ 제거
       
       int result = memberService.passwdChange(paraMap);
       model.addAttribute("result", result);
       
       return "security/member/passwdChangeResult";
    // src/main/resources/templates/security/member/passwdChangeResult.html 파일 생성해줘야 함
    }
    
    
    // 마이페이지로
    @PreAuthorize("hasRole('USER')")
    @GetMapping("member/mypage")
    public String mypage(HttpSession session, Model model) {

        // DB 조회값을 올릴 수 있으면 올리고(예: memberService.getMemberDetail(memberNo))
        // 아직 없으면 model에 안 담아도 템플릿에서 더미로 처리됨

        return "security/member/mypage";
        // templates/security/member/mypage.html
    }
    
    
    // 회원정보보기 URL
    @PreAuthorize("hasRole('USER')") // 해당 권한이 있는 사람이 있는 사람만 허용
    @GetMapping(value="memberOnly")
    public String memberOnly(){
       
       return "security/member/memberOwnInfo";
    // src/main/resources/templates/security/member/memberOwnInfo.html 파일 생성해줘야 함
    }
    
    
    // 회원정보 수정 폼 페이지
    @PreAuthorize("hasRole('USER')")
    @GetMapping("member/profileEdit")
    public String profileEditForm() {
        return "security/member/profileEditForm";
    }
    
    
    // 회원정보 수정 저장
    @PreAuthorize("hasRole('USER')")
    @PostMapping("member/profileEdit")
    public String profileEditEnd(MemberDTO memberdto,
                                 HttpSession session,
                                 Model model) {

        Session_MemberDTO sm = (Session_MemberDTO) session.getAttribute("sessionMemberDTO");
        memberdto.setMemberNo(sm.getMemberNo());   // ★ 핵심: 어디 회원인지 PK 세팅

        int result = memberService.update_member_profile(memberdto);
        model.addAttribute("result", result);

        return "security/member/profileEditResult";
    }
    
    
    
    
    
    
    
    
    
    
    // 관리자 권한을 가진 사용자만 접근 가능한 URL
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN','USER_SPECIAL')") // config 의 .requestMatchers("/security/special/**").hasAnyRole("ADMIN", "USER_SPECIAL") 와 겹치는 부분이지만 그냥 썼다.
    @GetMapping(value="special/special_memberOnly")
    public String special_memberOnly(){
       
       return "security/special_memberOnly";
    // src/main/resources/templates/security/special_memberOnly.html 파일 생성해줘야 함
    }
    
    
    
    
    
}