package com.deare.backend.global.external.gemini.limit;

import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiUsageLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "AI_USAGE:LETTER_ANALYZE:";
    private static final int DAILY_LIMIT = 20;
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void checkAndIncrement(Long userId) {
        String key = buildKey(userId);

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, ttlUntilMidnight());
        }

        if (count != null && count > DAILY_LIMIT) {
            log.info("[AI-USAGE-LIMIT] 일일 한도 초과 - userId: {}, count: {}", userId, count);
            throw new GeneralException(AiUsageErrorCode.DAILY_LIMIT_EXCEEDED);
        }
    }

    private String buildKey(Long userId) {
        String today = LocalDate.now(ZONE).format(DATE_FORMAT);
        return KEY_PREFIX + userId + ":" + today;
    }

    private Duration ttlUntilMidnight() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight);
    }
}
