package com.deare.backend.global.auth.oauth.service;

import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.global.common.exception.GeneralException;
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
    public String generateState(String inviteCode) {
        String state = UUID.randomUUID().toString();
        String key = STATE_PREFIX + state;

        redisTemplate.opsForValue().set(key, inviteCode == null ? "" : inviteCode, STATE_TTL_MINUTES, TimeUnit.MINUTES);

        log.debug("OAuth State 생성 - State: {}", state);
        return state;
    }

    /**
     * State 검증 및 삭제 (1회성)
     * @param state 검증용
     * @return state에 연결된 초대 코드, 초대 코드가 없으면 null
     */
    public String validateAndDeleteState(String state) {
        if (state == null || state.isBlank()) {
            log.warn("OAuth State 검증 실패 - State가 null 또는 빈 문자열");
            throw new GeneralException(AuthErrorCode.INVALID_STATE);
        }

        String key = STATE_PREFIX + state;
        String inviteCode = redisTemplate.opsForValue().getAndDelete(key);
        if (inviteCode != null) {
            log.debug("OAuth State 검증 성공 - State: {}", state);
            return inviteCode.isBlank() ? null : inviteCode;
        }

        log.warn("OAuth State 검증 실패 - State가 Redis에 존재하지 않음: {}", state);
        throw new GeneralException(AuthErrorCode.INVALID_STATE);
    }
}
