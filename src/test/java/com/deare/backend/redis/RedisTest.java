package com.deare.backend.redis;

import com.deare.backend.global.auth.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // 테스트용 키 정리
        Set<String> testKeys = redisTemplate.keys("test:*");
        if (testKeys != null && !testKeys.isEmpty()) {
            redisTemplate.delete(testKeys);
        }
        Set<String> rtKeys = redisTemplate.keys("RT:*");
        if (rtKeys != null && !rtKeys.isEmpty()) {
            redisTemplate.delete(rtKeys);
        }
    }

    // === 기본 Redis 연결 테스트 ===

    @Test
    @DisplayName("Redis 연결 테스트 - ping/pong")
    void redis_ping_test() {
        // given
        String key = "test:redis:ping";
        String value = "pong";

        // when
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(5));
        String result = stringRedisTemplate.opsForValue().get(key);

        // then
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("Redis 값 저장/조회 테스트")
    void redis_set_get_test() {
        // given
        String key = "test:key";
        String value = "test-value";
        long ttl = 60;

        // when
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        String result = redisTemplate.opsForValue().get(key);

        // then
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("Redis 값 삭제 테스트")
    void redis_delete_test() {
        // given
        String key = "test:delete";
        String value = "to-be-deleted";
        redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);

        // when
        Boolean deleted = redisTemplate.delete(key);

        // then
        assertThat(deleted).isTrue();
        assertThat(redisTemplate.opsForValue().get(key)).isNull();
    }

    @Test
    @DisplayName("Redis TTL 테스트")
    void redis_ttl_test() {
        // given
        String key = "test:ttl";
        String value = "ttl-value";
        long ttlSeconds = 30;

        // when
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        Long remainingTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        // then
        assertThat(remainingTtl).isNotNull();
        assertThat(remainingTtl).isGreaterThan(0);
        assertThat(remainingTtl).isLessThanOrEqualTo(ttlSeconds);
    }

    // === JwtService Refresh Token 테스트 ===

    @Test
    @DisplayName("Refresh Token 저장 테스트")
    void refreshToken_save_test() {
        // given
        Long userId = 1L;
        String token = "test-refresh-token";

        // when
        jwtService.saveRefreshToken(userId, token);
        String savedToken = jwtService.getRefreshToken(userId);

        // then
        assertThat(savedToken).isEqualTo(token);
    }

    @Test
    @DisplayName("Refresh Token 조회 - 없는 경우 null")
    void refreshToken_get_notFound() {
        // given
        Long userId = 999L;

        // when
        String token = jwtService.getRefreshToken(userId);

        // then
        assertThat(token).isNull();
    }

    @Test
    @DisplayName("Refresh Token 삭제 테스트")
    void refreshToken_delete_test() {
        // given
        Long userId = 2L;
        String token = "token-to-delete";
        jwtService.saveRefreshToken(userId, token);

        // when
        jwtService.deleteRefreshToken(userId);

        // then
        assertThat(jwtService.getRefreshToken(userId)).isNull();
    }

    @Test
    @DisplayName("Refresh Token 검증 - 성공")
    void refreshToken_validate_success() {
        // given
        Long userId = 3L;
        String token = "valid-token";
        jwtService.saveRefreshToken(userId, token);

        // when
        boolean isValid = jwtService.validateRefreshToken(userId, token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 검증 - 실패 (토큰 불일치)")
    void refreshToken_validate_fail_mismatch() {
        // given
        Long userId = 4L;
        String savedToken = "saved-token";
        String wrongToken = "wrong-token";
        jwtService.saveRefreshToken(userId, savedToken);

        // when
        boolean isValid = jwtService.validateRefreshToken(userId, wrongToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh Token 검증 - 실패 (토큰 없음)")
    void refreshToken_validate_fail_notFound() {
        // given
        Long userId = 5L;
        String token = "some-token";

        // when
        boolean isValid = jwtService.validateRefreshToken(userId, token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("여러 사용자 Refresh Token 저장/조회")
    void refreshToken_multipleUsers_test() {
        // given
        jwtService.saveRefreshToken(10L, "token-10");
        jwtService.saveRefreshToken(20L, "token-20");
        jwtService.saveRefreshToken(30L, "token-30");

        // when & then
        assertThat(jwtService.getRefreshToken(10L)).isEqualTo("token-10");
        assertThat(jwtService.getRefreshToken(20L)).isEqualTo("token-20");
        assertThat(jwtService.getRefreshToken(30L)).isEqualTo("token-30");
    }
}
