package com.deare.backend.api.auth.service;

import com.deare.backend.api.auth.dto.response.LoginResponseDTO;
import com.deare.backend.api.auth.dto.response.RefreshResponseDTO;
import com.deare.backend.api.auth.dto.response.SignupResponseDTO;
import com.deare.backend.domain.user.entity.Provider;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtProvider;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthCallbackUserInfoResponseDTO;
import com.deare.backend.global.auth.signupToken.SignupTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final SignupTokenProvider signupTokenProvider;
    
    /**
     * OAuth 로그인 처리
     * - 기존 회원: JWT 발급
     * - 신규 회원: Signup Token 발급 (회원가입 필요)
     * 
     * @return Object - LoginResponseDTO 또는 SignupResponseDTO
     */
    @Transactional
    public Object loginWithOAuth(OAuthCallbackUserInfoResponseDTO oauthInfo) {
        
        // 1. Provider와 ProviderId로 기존 사용자 조회
        Provider provider = Provider.valueOf(oauthInfo.provider().toUpperCase());
        
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                provider, 
                oauthInfo.providerUserId()
        );
        
        // 2-1. 기존 회원 → JWT 발급
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Access Token 생성
            String accessToken = jwtProvider.generateAccessToken(user);
            
            // Refresh Token 생성
            String refreshToken = jwtProvider.generateRefreshToken(user);
            
            // Refresh Token을 Redis에 저장
            jwtService.saveRefreshToken(user.getId(), refreshToken);
            
            log.info("기존 회원 로그인 성공 - User ID: {}, Email: {}", user.getId(), user.getEmail());
            
            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole().name())
                    .build();
        }
        
        // 2-2. 신규 회원 → Signup Token 발급
        else {
            // Signup Token 생성 (provider + providerId + email 포함)
            String signupToken = signupTokenProvider.generateSignupToken(
                    oauthInfo.provider(),
                    oauthInfo.providerUserId(),
                    oauthInfo.email()
            );
            
            log.info("신규 회원 - Signup Token 발급 - Provider: {}, ProviderId: {}", 
                    provider, oauthInfo.providerUserId());
            
            return SignupResponseDTO.builder()
                    .signupToken(signupToken)
                    .provider(oauthInfo.provider())
                    .email(oauthInfo.email())
                    .message("회원가입이 필요합니다. 추가 정보를 입력해주세요.")
                    .build();
        }
    }
    
    /**
     * Access Token 재발급
     */
    @Transactional(readOnly = true)
    public RefreshResponseDTO refresh(String refreshToken) {
        
        // 1. Refresh Token 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }
        
        // 2. Refresh Token에서 사용자 ID 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        
        // 3. Redis에 저장된 Refresh Token과 비교
        if (!jwtService.validateRefreshToken(userId, refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }
        
        // 4. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtProvider.generateAccessToken(user);
        
        log.info("Access Token 재발급 - User ID: {}", userId);
        
        return RefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .build();
    }
    
    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId) {
        // Redis에서 Refresh Token 삭제
        jwtService.deleteRefreshToken(userId);
        log.info("로그아웃 - User ID: {}", userId);
    }
}
