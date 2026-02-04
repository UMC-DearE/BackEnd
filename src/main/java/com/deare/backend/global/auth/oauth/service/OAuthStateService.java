package com.deare.backend.global.auth.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth State 파라미터 관리 서비스
 * - CSRF 공격 방지를 위한 state 생성/검증/삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String STATE_PREFIX = "OAUTH_STATE:";
    private static final long STATE_TTL_MINUTES = 5; // 5분

    /**
     * State 생성 및 Redis 저장
     * @return 생성된 state 문자열
     */
    public String generateState() {
        String state = UUID.randomUUID().toString();
        String key = STATE_PREFIX + state;

        redisTemplate.opsForValue().set(key, "valid", STATE_TTL_MINUTES, TimeUnit.MINUTES);

        log.debug("OAuth State 생성 - State: {}", state);
        return state;
    }

    /**
     * State 검증 및 삭제 (1회성)
     * @param state 검증용
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateAndDeleteState(String state) {
        if (state == null || state.isBlank()) {
            log.warn("OAuth State 검증 실패 - State가 null 또는 빈 문자열");
            return false;
        }

        String key = STATE_PREFIX + state;
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.debug("OAuth State 검증 성공 - State: {}", state);
            return true;
        }

        log.warn("OAuth State 검증 실패 - State가 Redis에 존재하지 않음: {}", state);
        return false;
    }
}
