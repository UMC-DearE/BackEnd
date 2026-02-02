package com.deare.backend.global.auth.signupToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SignupTokenProperties signupTokenProperties;

    private static final String SIGNUP_TOKEN_PREFIX = "ST:";

    /**
     * Signup Token을 Redis에 저장
     * Key: ST:{provider}:{providerId}
     * Value: signupToken
     * TTL: 30분
     */
    public void saveSignupToken(String provider, String providerId, String email, String signupToken) {
        String key = buildKey(provider, providerId);
        redisTemplate.opsForValue().set(
                key,
                signupToken,
                signupTokenProperties.getExpiration(),
                TimeUnit.MILLISECONDS
        );
        log.info("Signup Token 저장 완료 - Provider: {}, ProviderId: {}, Email: {}", provider, providerId, email);
    }

    /**
     * Redis에서 Signup Token 조회
     */
    public String getSignupToken(String provider, String providerId) {
        String key = buildKey(provider, providerId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Signup Token 검증 (Redis에 저장된 토큰과 비교)
     */
    public boolean validateSignupToken(String provider, String providerId, String signupToken) {
        String savedToken = getSignupToken(provider, providerId);
        return savedToken != null && savedToken.equals(signupToken);
    }

    /**
     * Signup Token 삭제 (회원가입 완료 후 1회성 보장)
     */
    public void deleteSignupToken(String provider, String providerId, String email) {
        String key = buildKey(provider, providerId);
        redisTemplate.delete(key);
        log.info("Signup Token 삭제 완료 - Provider: {}, ProviderId: {}, Email: {}", provider, providerId, email);
    }

    private String buildKey(String provider, String providerId) {
        return SIGNUP_TOKEN_PREFIX + provider + ":" + providerId;
    }
}
