package com.deare.backend.api.auth.service;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.api.auth.dto.response.LoginResponseDTO;
import com.deare.backend.api.auth.dto.response.RefreshResponseDTO;
import com.deare.backend.api.auth.dto.response.SignupResponseDTO;
import com.deare.backend.api.term.service.UserTermService;
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

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final SignupTokenProvider signupTokenProvider;
    private final UserTermService userTermService;
    
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
    
    /**
     * 회원가입
     * 
     * @param signupToken Signup Token
     * @param request 닉네임, 약관 동의 정보
     * @return LoginResponseDTO
     */
    @Transactional
    public LoginResponseDTO signup(String signupToken, SignupRequestDTO request) {
        // 1. Signup Token 검증
        if (!signupTokenProvider.validateToken(signupToken)) {
            throw new RuntimeException("유효하지 않은 Signup Token입니다.");
        }
        
        // 2. Signup Token 파싱
        Map<String, String> tokenInfo = signupTokenProvider.parseToken(signupToken);
        String provider = tokenInfo.get("provider");
        String providerId = tokenInfo.get("providerId");
        String email = tokenInfo.get("email");
        
        // 3. 중복 체크 (이미 가입된 사용자인지)
        Provider providerEnum = Provider.valueOf(provider.toUpperCase());
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(providerEnum, providerId);
        if (existingUser.isPresent()) {
            throw new RuntimeException("이미 가입된 사용자입니다.");
        }
        
        // 4. User 생성 (정적 팩토리 메서드 사용)
        User newUser = User.signUpUser(providerEnum, providerId, email, request.nickname());
        
        userRepository.save(newUser);
        
        log.info("회원가입 완료 - User ID: {}, Provider: {}, Email: {}", 
                newUser.getId(), provider, email);
        
        // 5. User-Terms 매핑 (약관 동의 처리)
        userTermService.createUserTerms(newUser, request.agreedTermIds());
        
        // 6. JWT 발급
        String accessToken = jwtProvider.generateAccessToken(newUser);
        String refreshToken = jwtProvider.generateRefreshToken(newUser);
        
        // 7. Refresh Token을 Redis에 저장
        jwtService.saveRefreshToken(newUser.getId(), refreshToken);
        
        // 8. 응답
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(newUser.getId())
                .email(newUser.getEmail())
                .nickname(newUser.getNickname())
                .role(newUser.getRole().name())
                .build();
    }
}
