package com.deare.backend.api.auth.service;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.api.auth.dto.response.OAuthCallbackResponseDTO;
import com.deare.backend.api.auth.dto.response.SignupResponseDTO;
import com.deare.backend.api.auth.dto.response.TermResponseDTO;
import com.deare.backend.api.auth.dto.result.OAuthCallbackResult;
import com.deare.backend.api.auth.dto.result.SignupResult;
import com.deare.backend.api.auth.dto.result.TokenPair;
import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.api.term.service.UserTermService;
import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.repository.TermRepository;
import com.deare.backend.domain.user.entity.Provider;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtProvider;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthCallbackUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.service.OAuthService;
import com.deare.backend.global.auth.signupToken.SignupTokenProvider;
import com.deare.backend.global.auth.signupToken.SignupTokenService;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final SignupTokenProvider signupTokenProvider;
    private final SignupTokenService signupTokenService;
    private final UserTermService userTermService;
    private final OAuthService oAuthService;

    // === Public Methods ===

    /**
     * OAuth 콜백 처리
     * - 기존 회원: JWT 발급 + REGISTERED 반환
     * - 신규 회원: Signup Token 발급 + Redis 저장 + SIGNUP_REQUIRED 반환
     */
    @Transactional
    public OAuthCallbackResult handleOAuthCallback(String provider, String code) {

        // Oauth 처리 (code -> token -> userInfo)
        OAuthCallbackUserInfoResponseDTO oauthInfo = oAuthService.handleCallback(provider, code);

        // provider와 providerId로 기존 사용자 조회
        Provider providerEnum = Provider.valueOf(oauthInfo.provider().toUpperCase());

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(
                providerEnum,
                oauthInfo.providerUserId()
        );

        // 분기

        // 3-1 기존 회원 -> JWT 발급
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String accessToken = jwtProvider.generateAccessToken(user);
            String refreshToken = jwtProvider.generateRefreshToken(user);

            jwtService.saveRefreshToken(user.getId(), refreshToken);

            log.info("기존 회원 로그인 성공 - User ID: {}, Email: {}", user.getId(), user.getEmail());

            return new OAuthCallbackResult(
                    true,
                    accessToken,
                    refreshToken,
                    null,
                    OAuthCallbackResponseDTO.registered()
            );
        }

        // 3-2 신규 회원 -> signup-token 발급 + Redis 저장
        String signupToken = signupTokenProvider.generateSignupToken(
                oauthInfo.provider(),
                oauthInfo.providerUserId(),
                oauthInfo.email()
        );

        // Redis 저장 (1회용)
        signupTokenService.saveSignupToken(
                oauthInfo.provider(),
                oauthInfo.providerUserId(),
                oauthInfo.email(),
                signupToken
        );

        log.info("신규 회원 - Signup Token 발급 및 Redis 저장 - Provider: {}, ProviderId: {}, Email: {}",
                provider, oauthInfo.providerUserId(), oauthInfo.email());

        return new OAuthCallbackResult(
                false,
                null,
                null,
                signupToken,
                OAuthCallbackResponseDTO.signupRequired()
        );
    }

    /**
     * JWT 토큰 재발급
     */
    @Transactional
    public TokenPair refresh(String refreshToken) {

        // refresh-token 존재 여부 확인
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(AuthErrorCode.MISSING_REFRESH_TOKEN);
        }

        // refresh-token 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // refresh-token에서 userId 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // Redis에 저장된 refresh-token 과 비교
        if (!jwtService.validateRefreshToken(userId, refreshToken)) {
            throw new GeneralException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.USER_NOT_FOUND));

        // 새로운 토큰 발급
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        // 새로운 refresh-token -> Redis 저장
        jwtService.saveRefreshToken(userId, newRefreshToken);

        log.info("토큰 재발급 - User ID: {}", userId);

        return new TokenPair(newAccessToken, newRefreshToken);
    }

    /**
     * Signup Token 검증 (JWT 서명 + Redis 존재 여부)
     */
    public void validateSignupToken(String signupToken) {

        // JWT 검증
        if (!signupTokenProvider.validateToken(signupToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }

        // 토큰에서 정보 추출
        Map<String, String> tokenInfo = signupTokenProvider.parseToken(signupToken);
        String provider = tokenInfo.get("provider");
        String providerId = tokenInfo.get("providerId");

        // Redis 에 저장된 토큰과 비교
        if (!signupTokenService.validateSignupToken(provider, providerId, signupToken)) {
            throw new GeneralException(AuthErrorCode.EXPIRED_SIGNUP_TOKEN);
        }
    }

    /**
     * 회원가입용 약관 조회 (is_active 약관)
     */
    @Transactional(readOnly = true)
    public TermResponseDTO getSignupTerms() {
        List<Term> terms = termRepository.findByIsActiveTrue();
        return TermResponseDTO.from(terms);
    }

    /**
     * 회원가입
     */
    @Transactional
    public SignupResult signup(String signupToken, SignupRequestDTO request) {

        // signup-token JWT 검증
        if (!signupTokenProvider.validateToken(signupToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }

        // signup-token 파싱
        Map<String, String> tokenInfo = signupTokenProvider.parseToken(signupToken);
        String provider = tokenInfo.get("provider");
        String providerId = tokenInfo.get("providerId");
        String email = tokenInfo.get("email");

        // Redis에서 signup-token 검증 (1회성)
        if (!signupTokenService.validateSignupToken(provider, providerId, signupToken)) {
            throw new GeneralException(AuthErrorCode.EXPIRED_SIGNUP_TOKEN);
        }

        // 중복 체크
        Provider providerEnum = Provider.valueOf(provider.toUpperCase());
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(providerEnum, providerId);
        if (existingUser.isPresent()) {
            throw new GeneralException(AuthErrorCode.USER_ALREADY_EXISTS);
        }

        // user 생성
        User newUser = User.signUpUser(providerEnum, providerId, email, request.nickname());
        userRepository.save(newUser);

        log.info("회원가입 완료 - User ID: {}, Provider: {}, Email: {}",
                newUser.getId(), provider, email);

        // 약관 동의 처리
        userTermService.createUserTerms(newUser, request.termIds());

        // Redis 에서 signup-token 삭제 (1회성)
        signupTokenService.deleteSignupToken(provider, providerId, email);

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(newUser);
        String refreshToken = jwtProvider.generateRefreshToken(newUser);

        // refresh-token 을 Redis에 저장
        jwtService.saveRefreshToken(newUser.getId(), refreshToken);

        return new SignupResult(new TokenPair(accessToken, refreshToken), SignupResponseDTO.of());
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId) {
        jwtService.deleteRefreshToken(userId);
        log.info("로그아웃 - User ID: {}", userId);
    }
}
