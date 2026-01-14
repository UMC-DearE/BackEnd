package com.deare.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // 테스트/REST면 보통 꺼둠
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/test/**","/swagger-ui/**", "/v3/api-docs/**").permitAll() // 테스트 API 열기
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()) // 기본 인증
                .formLogin(Customizer.withDefaults()) // 기본 로그인 페이지
                .build();
    }
}
