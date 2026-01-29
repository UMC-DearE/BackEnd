package com.deare.backend.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    
    private static final String REFRESH_TOKEN_PREFIX = "RT:";
    
    /**
     * Refresh Token을 Redis에 저장
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
        log.info("Refresh Token 저장 완료 - User ID: {}", userId);
    }
    
    /**
     * Redis에서 Refresh Token 조회
     */
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Redis에서 Refresh Token 삭제 (로그아웃)
     */
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Refresh Token 삭제 완료 - User ID: {}", userId);
    }
    
    /**
     * Refresh Token 검증 (Redis에 저장된 토큰과 비교)
     */
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String savedToken = getRefreshToken(userId);
        return savedToken != null && savedToken.equals(refreshToken);
    }
}
