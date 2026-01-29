package com.deare.backend.api.auth.controller;

import com.deare.backend.api.auth.dto.request.RefreshRequestDTO;
import com.deare.backend.api.auth.dto.response.LoginResponseDTO;
import com.deare.backend.api.auth.dto.response.RefreshResponseDTO;
import com.deare.backend.api.auth.service.AuthService;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthCallbackUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final OAuthService oauthService;
    private final AuthService authService;
    
    /**
     * OAuth 로그인
     * 
     * - 기존 회원: LoginResponseDTO (JWT 토큰 반환)
     * - 신규 회원: SignupResponseDTO (Signup Token 반환)
     * 
     * @param provider "kakao" 또는 "google"
     * @param code OAuth Provider가 발급한 인가 코드
     * @return LoginResponseDTO 또는 SignupResponseDTO
     */
    @PostMapping("/login/oauth2/{provider}")
    public ResponseEntity<?> loginWithOAuth(
            @PathVariable String provider,
            @RequestParam("code") String code
    ) {
        // 1. OAuth 처리 (code → access_token → userInfo)
        OAuthCallbackUserInfoResponseDTO oauthInfo = oauthService.handleCallback(provider, code);
        
        // 2. 기존 회원 → JWT 발급 / 신규 회원 → Signup Token 발급
        Object response = authService.loginWithOAuth(oauthInfo);
        
        // 3. 응답 (LoginResponseDTO 또는 SignupResponseDTO)
        return ResponseEntity.ok(response);
    }
    
    /**
     * Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(
            @RequestBody RefreshRequestDTO request
    ) {
        RefreshResponseDTO response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 로그아웃
     * 
     * Authorization: Bearer {accessToken} 필수
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User user
    ) {
        authService.logout(user.getId());
        return ResponseEntity.ok().build();
    }
}
