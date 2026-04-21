package com.goodee.beedan.config.security;

import com.goodee.beedan.dto.member.auth.SignInErrorMessageDto;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.service.auth.CustomOAuth2UserService;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final SecurityService securityService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomSuccessHandler customSuccessHandler, CustomFailureHandler customFailureHandler, CustomOAuth2UserService customOAuth2UserService, SessionRegistry sessionRegistry) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/webhook/**")
                        .ignoringRequestMatchers("/api/tracking/**")
                )   // Payment Success 후 외부 팀과 JSON 통신 용 webhook 통과 코드입니다. + 추적 API는 sendBeacon 사용 (커스텀 헤더 불가)
                .authorizeHttpRequests(authorize -> authorize
                        // 1) 공개 경로
                        .requestMatchers(
                                "/",
                                "/about",
                                "/error/**",
                                "/css/**",
                                "/js/**",
                                "/image/**",
                                "/stock/**",
                                "/notice/list/**",
                                "/notice/detail/**",
                                "/api/tracking/**",
                                "/api/stock/list",
                                "/files/**",
                                "/auth/passwd/change",
                                "/auth/kakao/**",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()

                        // 2) 로그인은 필요하지만 공개 API로 열면 안 되는 예외 경로
                        .requestMatchers(
                                "/api/auth/disconnectSns",
                                "/api/auth/check-password",
                                "/api/member/postpone-password"
                        ).authenticated()

                        // 3) 인증/로그인 관련
                        .requestMatchers(
                                "/api/auth/**",
                                "/auth/**"
                        ).permitAll()

                        // 4) ROOT 전용
                        .requestMatchers(
                                "/root/**",
                                "/api/root/**",
                                "/admin/member/edit"
                        ).hasRole("ROOT")

                        // 5) ADMIN / ROOT 전용
                        .requestMatchers(
                                "/api/admin/chat/**", "/admin/chat/**",
                                "/api/admin/inquiry/**", "/admin/inquiry/**", "/api/admin/inquiries/**",
                                "/admin/quote/**", "/admin/negotiation/**",
                                "/api/admin/member/**", "/admin/member/**",
                                "/api/newstock/**", "/admin/newstock/**", "/api/admin/stock/**", "/admin/stock/**",
                                "/api/admin/require/**", "/admin/require/**",
                                "/notice/write/**", "/notice/edit/**"
                        ).hasAnyRole("ADMIN", "ROOT")

                        // 6) USER + ADMIN + ROOT 공용
                        .requestMatchers(
                                "/api/cart/**", "/cart/**",
                                "/quote/request"
                        ).hasAnyRole("USER", "ADMIN", "ROOT")

                        // 7) USER 전용
                        .requestMatchers(
                                "/quote/**",
                                "/api/payment/**", "/payment/**",
                                "/api/orders/**", "/order/**",
                                "/api/shipments/**",
                                "/api/notification/**", "/notification/**",
                                "/api/receiver/**", "/receiver/**",
                                "/api/images/**",
                                "/api/wishlist/**", "/wishlist/**",
                                "/api/stock/myitem/**", "/myitem/**",
                                "/api/require/**", "/require/**",
                                "/api/inquiries/**", "/inquiry/**",
                                "/api/chat/**", "/api/chatbot/**"
                        ).hasRole("USER")

                        // 8) 로그인 사용자 공통
                        .requestMatchers(
                                "/mypage/**",
                                "/ws", "/ws/**",
                                "/api/quote/**"
                        ).authenticated()

                        // 9) 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/auth/signin")
                        .loginProcessingUrl("/auth/signin")
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/signout")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true) // 여기서 스프링 시큐리티가 세션을 만료시킴
                        .clearAuthentication(true)
                        // 💡 1. 로그아웃 과정 중에 실행되는 핸들러를 추가하여 로그 작성
                        .addLogoutHandler((request, response, authentication) -> {
                            log.info("==== [로그아웃 진행 중] SecurityContext 및 세션 정리 시작 ====");
                            if (request.getSession(false) != null) {
                                log.info("현재 세션 ID: {}", request.getSession(false).getId());
                            } else {
                                log.info("현재 세션: null (이미 만료되었거나 없음)");
                            }
                        })
                        // 💡 2. 로그아웃이 완전히 성공한 직후
                        .logoutSuccessHandler((request, response, authentication) -> {
                            log.info("==== [로그아웃 성공] /auth/signin 으로 리다이렉트 합니다 ====");
                            response.sendRedirect("/auth/signin");
                        })
                        .permitAll()
                )
                .sessionManagement(session -> session
                        // 💡 3. 유효하지 않은 세션(이미 만료된 세션)으로 접근했을 때
                        .invalidSessionStrategy((request, response) -> {
                            log.warn("==== [유효하지 않은 세션 감지] invalidSessionStrategy 실행 ====");
                            request.getSession(true).setAttribute("errorMessage",
                                    new SignInErrorMessageDto("세션만료", "세션이 만료되었습니다. 재로그인 해주시기 바랍니다."));
                            response.sendRedirect("/auth/signin");
                        })
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry)
                                // 💡 4. 중복 로그인으로 인해 기존 세션이 만료 처리되었을 때
                                .expiredSessionStrategy(event -> {
                                    log.warn("==== [중복 로그인 감지] expiredSessionStrategy 실행 ====");
                                    HttpServletRequest request = event.getRequest();
                                    request.getSession(true).setAttribute("errorMessage",
                                            new SignInErrorMessageDto("중복로그인", "다른 사용자가 로그인하여 로그아웃 처리됩니다."));
                                    event.getResponse().sendRedirect("/auth/signin");
                                })
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/signin")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOAuth2UserService)
                        )
                        .failureHandler((request, response, exception) -> {
                            System.out.println("Error Type: " + exception.getClass().getName());
                            request.getSession().setAttribute("errorMessage",
                                    new SignInErrorMessageDto("소셜로그인", "잠시 후 다시 시도해주세요."));
                            response.sendRedirect("/auth/signin");
                        })
                )
                .exceptionHandling(ex -> ex              // ← 여기 추가
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            request.setAttribute("message", "잘못된 접근입니다.");
                            response.sendRedirect("/error/denied");
                        })
                );
        return http.build();




    }

    // 패스워드 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/h2-console/**",
                "/api/webhook/**",
                "/.well-known/**",
                "/css/**",
                "/js/**",
                "/image/**"
        );
    }

    // 세션변경 이벤트를 감지하기 위한 리스너 등록
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
