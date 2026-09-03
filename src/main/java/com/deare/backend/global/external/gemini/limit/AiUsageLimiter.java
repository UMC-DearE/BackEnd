package com.deare.backend.global.external.gemini.limit;

import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiUsageLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${ai.usage.daily-limit:20}")
    private int dailyLimit;

    private static final String KEY_PREFIX = "AI_USAGE:LETTER_ANALYZE:";
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            """
            local current = tonumber(redis.call('GET', KEYS[1]) or '0')
            if current >= tonumber(ARGV[1]) then
                return -1
            end
            local newVal = redis.call('INCR', KEYS[1])
            if redis.call('TTL', KEYS[1]) == -1 then
                redis.call('EXPIRE', KEYS[1], ARGV[2])
            end
            return newVal
            """,
            Long.class
    );

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            """
            local current = tonumber(redis.call('GET', KEYS[1]) or '0')
            if current > 0 then
                return redis.call('DECR', KEYS[1])
            end
            return current
            """,
            Long.class
    );

    public String reserve(Long userId) {
        String key = buildKey(userId);

        Long result = redisTemplate.execute(
                RESERVE_SCRIPT,
                List.of(key),
                String.valueOf(dailyLimit),
                String.valueOf(ttlUntilMidnight().toSeconds())
        );

        if (result == null) {
            log.error("[AI-USAGE-LIMIT] 사용량 확인 실패(Redis 응답 없음) - userId: {}", userId);
            throw new GeneralException(AiUsageErrorCode.USAGE_CHECK_FAILED);
        }

        if (result < 0) {
            log.info("[AI-USAGE-LIMIT] 일일 한도 초과 - userId: {}, limit: {}", userId, dailyLimit);
            throw new GeneralException(AiUsageErrorCode.DAILY_LIMIT_EXCEEDED);
        }

        return key;
    }

    public void release(String usageKey) {
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(usageKey));
        } catch (Exception e) {
            log.warn("[AI-USAGE-LIMIT] 사용량 반납 실패 - key: {}", usageKey, e);
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
