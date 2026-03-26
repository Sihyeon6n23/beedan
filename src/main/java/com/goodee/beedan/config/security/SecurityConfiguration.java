package com.goodee.beedan.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.*;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error/**").permitAll() // 디버깅용
                        .requestMatchers("/css/**", "/js/**", "/image/**").permitAll() // 정적 리소스
                        .requestMatchers("/", "/stock/list", "/mypage/detail").permitAll() // 게시판
                        .requestMatchers("/signup").permitAll() // 인증
                        .requestMatchers("/member/**").hasAuthority("ROLE_ADMIN") // 관리자
                        .anyRequest().authenticated() // 그 외
                )
                .formLogin(login -> login
                        .loginPage("/auth/signin")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/signout")
                        .logoutSuccessUrl("/")
                        .permitAll()
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
                "/h2-console/**"
        );
    }
}
