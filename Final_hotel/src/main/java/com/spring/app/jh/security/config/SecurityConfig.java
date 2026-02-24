package com.spring.app.jh.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.spring.app.jh.security.loginsuccess.AdminAuthenticationSuccessHandler;
import com.spring.app.jh.security.loginsuccess.MemberAuthenticationSuccessHandler;
import com.spring.app.jh.security.service.AdminUserDetailsService;
import com.spring.app.jh.security.service.MemberUserDetailsService;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpSession;

/* ===== (#스프링시큐리티02) ===== */

@Configuration        // Spring 컨테이너가 처리해주는 설정 클래스(Configuration)이다.
@EnableWebSecurity    // Spring Security를 “내가 작성한 설정(SecurityConfig)” 기반으로 적용하겠다는 의미.
@EnableMethodSecurity // Controller/Service 메서드에 @PreAuthorize 같은 “메서드 단위 보안”을 쓰기 위한 전제조건.
                      /*
                          @PreAuthorize("hasRole('ADMIN')")
                          @GetMapping("/admin/dashboard")
                          public String adminPage() { ... }

                          - URL 매칭 규칙(authorizeHttpRequests)만으로도 보안은 가능하지만,
                            메서드 실행 직전에 한번 더 “서버 레벨에서” 권한검사를 걸고 싶을 때 유용하다.

                          ※ hasRole("ADMIN") 은 내부적으로 "ROLE_ADMIN"을 찾는다.
                          - DB 권한 문자열은 ROLE_ 로 시작하는 형태(ROLE_ADMIN 등)가 되어야 한다.
                       */
public class SecurityConfig {

    /*
        비밀번호 암호화를 위해 BCrypt(BCryptPasswordEncoder)를 사용한다.

        - 회원/관리자 모두 같은 암호화 방식을 쓴다면 공통으로 1개 Bean만 있으면 된다.
        - 로그인 시에는 “사용자가 입력한 비밀번호”를 BCrypt로 검증하여 DB의 해시값과 비교한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // =====================================================================
    // 0) 공통: 권한(인가) 실패 처리(403)
    // =====================================================================
    /*
        AccessDeniedHandler란?
        - 사용자가 “로그인은 되어 있는데(인증 OK)”, 해당 URL/메서드에 필요한 권한(Role)이 없을 때 호출된다.
        - 이 상황이 대표적으로 403(Forbidden)이다.

        예)
        - ROLE_USER 회원이 /admin/** 관리자 전용 페이지에 접근
        - 또는 ROLE_ADMIN_BRANCH가 HQ 전용 기능에 접근 (룰을 그렇게 나눴다면)
     */
    @Bean
    AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect(request.getContextPath() + "/security/noAuthorized");
            // 권한이 부족할 때 안내 페이지로 이동
        };
    }


    // =====================================================================
    // 1) 관리자 체인(adminChain): /admin/** 전용 보안 규칙
    // =====================================================================
    /*
        [왜 체인을 분리하나?]
        - 너 DB는 “회원 테이블”과 “관리자 테이블”이 물리적으로 분리되어 있다.
          tbl_member_security / tbl_member_authorities
          tbl_admin_security  / tbl_admin_authorities

        - 그래서 로그인 처리도 “어느 테이블로 조회할지”가 갈라진다.
        - SecurityFilterChain을 2개로 나누면,
          /admin/** 요청은 무조건 관리자 쪽 인증 로직(AdminUserDetailsService)만 탄다.
          (판별/분기 로직이 없어져서 실수가 줄어든다)

        [AuthenticationEntryPoint(401)도 체인별로 분리하는 이유]
        - 로그인하지 않은 사용자가 /admin/**에 접근하면,
          회원 로그인 화면이 아니라 관리자 로그인 화면(/admin/login)으로 보내는 것이 자연스럽다.
     */
    @Bean
    @Order(1) // 우선순위: /admin/**은 먼저 잡아야 한다 (안 그러면 아래 memberChain이 먼저 잡을 수 있음)
    public SecurityFilterChain adminChain(HttpSecurity httpSecurity,
                                         AdminUserDetailsService adminUserDetailsService,
                                         PasswordEncoder passwordEncoder) throws Exception {

        // == 인증(Authentication) 실패 처리: 로그인 안 한 상태(401 상황)에서 /admin/** 접근 시 ==
        // AuthenticationEntryPoint는 “로그인 필요한데 아직 로그인 안 된 사용자”가 접근했을 때,
        // 어디로 보낼지/어떤 응답을 줄지 정하는 진입점이다.
        AuthenticationEntryPoint adminEntryPoint = (request, response, authException) -> {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            // 관리자 보호 자원에 접근했는데 로그인 안 했으면 관리자 로그인으로 보냄
        };

        httpSecurity
            // 이 체인은 /admin/** 요청에만 적용된다.
            .securityMatcher("/admin/**")

            // 개발/학습 단계에서는 CSRF disable을 많이 하지만, 운영에서는 정책에 맞게 활성화 고려.
            .csrf(csrfConfig -> csrfConfig.disable())

            .authorizeHttpRequests(auth -> auth
                // 뷰 렌더링 과정의 내부 forward/error 요청은 허용(템플릿/에러페이지 이동 시 필요)
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                // 관리자 로그인 화면/처리 URL은 인증 없이 허용
                .requestMatchers("/admin/login", "/admin/loginEnd", "/admin/login?**").permitAll()

                /*
                    관리자 권한 룰(예시)
                    - 너 DB에는 admin_type(HQ/BRANCH)도 있고 관리자 권한 테이블도 별도다.
                    - 권한 문자열 예:
                      ROLE_ADMIN_HQ
                      ROLE_ADMIN_BRANCH
                    - 여기서는 둘 중 하나라도 있으면 /admin/** 접근 허용.
                 */
                .anyRequest().hasAnyRole("ADMIN_HQ", "ADMIN_BRANCH")
            )

            /*
                formLogin: 관리자 로그인 설정
                - /admin/login (GET) : 관리자 로그인 폼
                - /admin/loginEnd (POST) : Spring Security가 가로채서 인증 처리하는 URL
                - usernameParameter("adminid") : 로그인 폼 input name="adminid"
                - passwordParameter("passwd")  : 로그인 폼 input name="passwd"
             */
            .formLogin(login -> login
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/loginEnd")
                .usernameParameter("adminid")
                .passwordParameter("passwd")
                .failureUrl("/admin/login?loginFail=true")

                /*
                    로그인 성공 시 처리:
                    - AdminAuthenticationSuccessHandler에서
                      (1) Session_AdminDTO 세션 저장
                      (2) SavedRequest 기반 redirect (보호자원 접근이었다면 원래 페이지로 복귀)
                      (3) SavedRequest가 없다면 defaultTargetUrl(/admin/dashboard)로 이동
                 */
                .successHandler(adminAuthenticationSuccessHandler())
            )

            /*
                logout: 관리자 로그아웃
                - 관리자 전용 로그아웃 URL로 분리하는 게 관리가 쉽다.
             */
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) session.invalidate();
                })
                .logoutSuccessUrl("/admin/login?logout=true")
            )

            /*
                예외 처리:
                - 401(로그인 안 됨): adminEntryPoint가 관리자 로그인 페이지로 보냄
                - 403(권한 부족): 공통 customAccessDeniedHandler 사용
             */
            .exceptionHandling(exceptionConfig -> exceptionConfig
                .authenticationEntryPoint(adminEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )

            // iframe 관련 정책 (Clickjacking 방어)
            .headers(headerConfig -> headerConfig.frameOptions(frame -> frame.sameOrigin()))

            /*
                관리자 인증 소스 지정:
                - adminUserDetailsService 를 사용하도록 DaoAuthenticationProvider를 연결
                - passwordEncoder 는 공통 BCrypt 사용
             */
            .authenticationProvider(adminAuthProvider(adminUserDetailsService, passwordEncoder));

        return httpSecurity.build();
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider(AdminUserDetailsService adminUserDetailsService,
                                                      PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    // =====================================================================
    // 2) 회원 체인(memberChain): /admin/** 제외한 일반 사이트 보안 규칙
    // =====================================================================
    /*
        이 체인은 “일반 사이트(회원 기능)” 쪽을 담당한다.
        - 기존 수업 코드/현재 프로젝트의 /security/login 기반 로그인 흐름을 최대한 유지했다.
        - /admin/**는 위 adminChain이 먼저 처리하므로, 여기서 denyAll을 걸어두면 실수 방지에 도움이 된다.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain memberChain(HttpSecurity httpSecurity,
                                          MemberUserDetailsService memberUserDetailsService,
                                          PasswordEncoder passwordEncoder) throws Exception {

        // == 인증(Authentication) 실패 처리: 로그인 안 한 상태(401 상황)에서 보호 URL 접근 시 ==
        // 여기서는 "회원 안내 페이지"로 보내는 기존 방식과 "회원 로그인 페이지" 둘 중 선택 가능.
        // - 안내페이지: /security/noAuthenticated
        // - 로그인페이지: /security/login
        AuthenticationEntryPoint memberEntryPoint = (request, response, authException) -> {
            response.sendRedirect(request.getContextPath() + "/security/noAuthenticated");
        };

        // 로그인 없이도 들어갈 수 있는 URL 목록들(기존 내용 최대 유지)
        String[] excludeUri = {
            "/test/**",
            "/",
            "/index",
            "/security/everybody",
            "/security/noAuthenticated",
            "/security/noAuthorized",
            "/security/memberRegister",
            "/security/member_id_check",
            "/security/emailDuplicateCheck",
            "/security/agree",
            "/security/memberRegisterEnd",
            "/security/login",
            "/security/loginEnd",
            "/board/list",
            "/board/view",
            "/board/view_2",
            "/board/readComment",
            "/board/wordSearchShow",
            "/board/commentList",
            "/board/ddCommentList",
            "/opendata/**",
            "/upload/**",
            "/photoupload/**",
            "/emailattachfile/**",
            "/images/**",
            "/product/list",
            "/room/**",
            "/reservation/**"
        };

        httpSecurity
            /*
                adminChain이 /admin/**를 먼저 잡고 처리한다.
                memberChain은 나머지를 담당한다.
             */
            .csrf(csrfConfig -> csrfConfig.disable())

            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                // 혹시라도 여기로 /admin/**가 떨어지면 차단(안전장치)
                .requestMatchers("/admin/**").denyAll()

                .requestMatchers(excludeUri).permitAll()

                /*
                    기존 룰 유지:
                    - 과거 수업/기존 코드에서 /security/admin/**, /emp/** 등의 관리자 기능이 남아있을 수 있다.
                    - 하지만 지금 구조에서는 관리자 기능 URL을 /admin/**로 통일하는 것이 권장된다.
                    - 즉, 아래 규칙은 "기존 코드 호환" 목적의 잔존 룰이라고 보면 된다.
                 */
                .requestMatchers("/security/special/**").hasAnyRole("ADMIN", "USER_SPECIAL")
                .requestMatchers("/security/admin/**").hasRole("ADMIN")
                .requestMatchers("/emp/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .formLogin(login -> login
                /*
                    회원 로그인 설정(기존 유지)
                    - loginPage("/security/login") : GET 로그인 폼
                    - loginProcessingUrl("/security/loginEnd") : POST 인증 처리(시큐리티가 가로챔)
                    - usernameParameter("memberid"), passwordParameter("passwd") : 폼 파라미터 매핑
                 */
                .loginPage("/security/login")
                .usernameParameter("memberid")
                .passwordParameter("passwd")
                .loginProcessingUrl("/security/loginEnd")

                /*
                    ===== 로그인 처리 흐름(요약) =====
                    1) 폼 POST(/security/loginEnd) 요청을 UsernamePasswordAuthenticationFilter가 가로챈다.
                    2) memberid/passwd를 꺼내서 UserDetailsService.loadUserByUsername(memberid)를 호출한다.
                    3) DB에서 사용자(회원) 정보를 조회하고(비밀번호 해시/권한 포함),
                       PasswordEncoder로 입력 비밀번호와 DB 해시 비밀번호를 비교한다.
                    4) 일치하면 인증 성공(Authentication 생성) -> SecurityContext에 저장 -> 세션으로 유지
                 */
                .failureUrl("/security/login?loginFail=true")

                /*
                    로그인 성공 시 처리:
                    - MemberAuthenticationSuccessHandler에서
                      (1) Session_MemberDTO 세션 저장
                      (2) last_login 업데이트 + login_history insert
                      (3) 비밀번호 변경 6개월 경과 시 변경 권장(confirm)
                      (4) SavedRequest 기반 redirect (보호자원 접근이었다면 원래 페이지로 복귀)
                      (5) SavedRequest가 없다면 defaultTargetUrl(/index)로 이동
                 */
                .successHandler(memberAuthenticationSuccessHandler())
            )

            .logout(logout -> logout
                .logoutUrl("/security/logout")
                .addLogoutHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) session.invalidate();
                })
                .logoutSuccessUrl("/index")
            )

            .exceptionHandling(exceptionConfig -> exceptionConfig
                .authenticationEntryPoint(memberEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler())
            )

            .headers(headerConfig -> headerConfig.frameOptions(frame -> frame.sameOrigin()))

            /*
                회원 인증 소스 지정:
                - memberUserDetailsService 를 사용하도록 DaoAuthenticationProvider를 연결
             */
            .authenticationProvider(memberAuthProvider(memberUserDetailsService, passwordEncoder));

        return httpSecurity.build();
    }

    @Bean
    public DaoAuthenticationProvider memberAuthProvider(MemberUserDetailsService memberUserDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    // =====================================================================
    // 3) 정적 리소스 허용(보안 필터 체인 자체를 타지 않게 ignore)
    // =====================================================================
    /*
        permitAll() vs ignoring()
        - permitAll(): 필터는 타지만 인증 검사만 면제
        - ignoring(): 아예 보안 필터를 타지 않음(정적 리소스는 보통 ignoring이 효율적)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/bootstrap-4.6.2-dist/**",
                             "/css/**",
                             "/fullcalendar_5.10.1/**",
                             "/Highcharts-10.3.1/**",
                             "/images/**",
                             "/jquery-ui-1.13.1.custom/**",
                             "/js/**",
                             "/smarteditor/**",
                             "/resources/photo_upload/**");
    }


    // =====================================================================
    // 4) 로그인 성공 후 추가 처리를 위한 SuccessHandler Bean
    // =====================================================================
    /*
        SuccessHandler:
        - 로그인 성공 직후 “추가 작업(세션 저장, 로그인 기록 저장, 마지막 로그인 업데이트 등)”이 필요할 때 사용한다.
        - 체인을 분리했으므로, 회원용/관리자용 SuccessHandler도 각각 분리해서 붙이는 것이 안전하다.
     */
    @Bean
    public MemberAuthenticationSuccessHandler memberAuthenticationSuccessHandler() {
        // SavedRequest가 없을 때 기본 이동 페이지
        return new MemberAuthenticationSuccessHandler("/index");
    }

    @Bean
    public AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler() {
        // SavedRequest가 없을 때 기본 이동 페이지(관리자 대시보드)
        return new AdminAuthenticationSuccessHandler("/admin/dashboard");
    }

}