package com.deare.backend.global.config;

import com.deare.backend.global.auth.jwt.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /// 생성자로 명시적으로 주입
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 설정 - 프론트 도메인 허용
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // 테스트/REST면 보통 꺼둠 + jwt 헤더 사용 시 꺼둠
        http
                .csrf(csrf -> csrf.disable());

        // 인증/인가
        http.
                authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(

                                // 헬스체크
                                "/actuator/health",
                                "/actuator/health/**",

                                // 테스트 API 열기 + 스웨거 세팅
                                "/test/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/images/**",

                                // 인증 관련 엔드포인트 오픈
                                "/auth/**",

                                // OAuth2 사용 시 - 콜백/리다이렉트 경로도 permitAll 필요
                                "/login/**",
                                "/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        http
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에 추가)
        http
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        // 인증 관련 예외 리턴
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN)
                )
        );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 2/5 프론트 주소로 추가 완료
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://deare.kr",
                "https://www.deare.kr"
        ));

        // refresh / signup-token -> cookie 전송
        config.setAllowCredentials(true);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")); // 브라우저 preflight 자주 요구하는 헤더 추가
        config.setExposedHeaders(List.of("Authorization")); // 필요하면 access 토큰 내려줄 때 노출

        // preflight 캐시
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
