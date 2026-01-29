package com.deare.backend.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redis_ping_test() {
        System.out.println("===== REDIS PING TEST START =====");

        // given
        String key = "test:redis:ping";
        String value = "pong";

        try {
            // when
            redisTemplate.opsForValue()
                    .set(key, value, Duration.ofSeconds(5));

            String result = redisTemplate.opsForValue().get(key);

            // then
            System.out.println("Redis SET 성공: key=" + key + ", value=" + value);
            System.out.println("Redis GET 결과: " + result);

            assertThat(result).isEqualTo(value);

            System.out.println("===== REDIS CONNECTION OK =====");
        } catch (Exception e) {
            System.out.println("===== REDIS CONNECTION FAILED =====");
            e.printStackTrace();
            throw e; // 테스트 실패로 명확히 처리
        }
    }
}
