package com.deare.backend.global.external.gemini.limit;

import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class AiUsageLimiterTest {

    @Autowired
    private AiUsageLimiter aiUsageLimiter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final Long TEST_USER_ID = 999_001L;

    @AfterEach
    void cleanUp() {
        Set<String> keys = redisTemplate.keys("AI_USAGE:LETTER_ANALYZE:" + TEST_USER_ID + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        // 테스트마다 바뀌는 daily-limit 필드를 기본값으로 복원
        ReflectionTestUtils.setField(aiUsageLimiter, "dailyLimit", 20);
    }

    @Test
    @DisplayName("한도 이내면 예외 없이 계속 reserve 가능하고, 초과하면 예외를 던진다")
    void reserve_blocksAfterLimit() {
        ReflectionTestUtils.setField(aiUsageLimiter, "dailyLimit", 3);

        aiUsageLimiter.reserve(TEST_USER_ID);
        aiUsageLimiter.reserve(TEST_USER_ID);
        aiUsageLimiter.reserve(TEST_USER_ID);

        assertThatThrownBy(() -> aiUsageLimiter.reserve(TEST_USER_ID))
                .isInstanceOf(GeneralException.class)
                .hasMessage(AiUsageErrorCode.DAILY_LIMIT_EXCEEDED.getMessage());

        // 한도를 넘은 뒤에도 카운트가 계속 올라가지 않고 한도 값에서 멈춰있어야 한다
        assertThat(currentCount()).isEqualTo("3");
    }

    @Test
    @DisplayName("release는 선점했던 사용량을 반납해 다시 reserve할 수 있게 한다")
    void release_givesBackSlot() {
        ReflectionTestUtils.setField(aiUsageLimiter, "dailyLimit", 1);

        aiUsageLimiter.reserve(TEST_USER_ID);
        assertThatThrownBy(() -> aiUsageLimiter.reserve(TEST_USER_ID))
                .isInstanceOf(GeneralException.class);

        aiUsageLimiter.release(TEST_USER_ID);

        // 반납되었으니 다시 1건 예약 가능해야 한다
        aiUsageLimiter.reserve(TEST_USER_ID);
        assertThat(currentCount()).isEqualTo("1");
    }

    @Test
    @DisplayName("release는 0 밑으로 내려가지 않는다")
    void release_neverGoesBelowZero() {
        aiUsageLimiter.release(TEST_USER_ID);
        aiUsageLimiter.release(TEST_USER_ID);

        assertThat(redisTemplate.opsForValue().get(buildKey())).isIn(null, "0");
    }

    private String currentCount() {
        return redisTemplate.opsForValue().get(buildKey());
    }

    private String buildKey() {
        String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "AI_USAGE:LETTER_ANALYZE:" + TEST_USER_ID + ":" + today;
    }
}
