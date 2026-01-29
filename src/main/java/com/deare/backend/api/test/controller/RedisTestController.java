package com.deare.backend.api.test.controller;

import com.deare.backend.global.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test/redis")
@RequiredArgsConstructor
public class RedisTestController {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    
    /**
     * Redis 연결 테스트
     */
    @GetMapping("/ping")
    public String ping() {
        try {
            redisTemplate.opsForValue().set("test:ping", "pong", 10, TimeUnit.SECONDS);
            String result = redisTemplate.opsForValue().get("test:ping");
            return "Redis 연결 성공: " + result;
        } catch (Exception e) {
            return "Redis 연결 실패: " + e.getMessage();
        }
    }
    
    /**
     * Redis에 값 저장
     */
    @PostMapping("/set")
    public String setValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "60") long ttl
    ) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        return "저장 성공 - Key: " + key + ", Value: " + value + ", TTL: " + ttl + "초";
    }
    
    /**
     * Redis에서 값 조회
     */
    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return "Key not found: " + key;
        }
        return "Value: " + value;
    }
    
    /**
     * Redis에서 값 삭제
     */
    @DeleteMapping("/delete")
    public String deleteValue(@RequestParam String key) {
        Boolean deleted = redisTemplate.delete(key);
        return deleted ? "삭제 성공: " + key : "삭제 실패 또는 키 없음: " + key;
    }
    
    /**
     * Refresh Token 저장 테스트
     */
    @PostMapping("/token/save")
    public String saveRefreshToken(
            @RequestParam Long userId,
            @RequestParam String token
    ) {
        jwtService.saveRefreshToken(userId, token);
        return "Refresh Token 저장 완료 - User ID: " + userId;
    }
    
    /**
     * Refresh Token 조회 테스트
     */
    @GetMapping("/token/get")
    public String getRefreshToken(@RequestParam Long userId) {
        String token = jwtService.getRefreshToken(userId);
        if (token == null) {
            return "Refresh Token not found for User ID: " + userId;
        }
        return "Refresh Token: " + token;
    }
    
    /**
     * Refresh Token 삭제 테스트
     */
    @DeleteMapping("/token/delete")
    public String deleteRefreshToken(@RequestParam Long userId) {
        jwtService.deleteRefreshToken(userId);
        return "Refresh Token 삭제 완료 - User ID: " + userId;
    }
    
    /**
     * 모든 Refresh Token 조회 (디버깅용)
     */
    @GetMapping("/tokens")
    public String getAllTokens() {
        var keys = redisTemplate.keys("RT:*");
        if (keys == null || keys.isEmpty()) {
            return "저장된 Refresh Token이 없습니다.";
        }
        
        StringBuilder result = new StringBuilder("저장된 Refresh Token:\n");
        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            result.append(String.format("- %s: %s (TTL: %d초)\n", key, value, ttl));
        }
        
        return result.toString();
    }
    
    /**
     * Refresh Token 검증 테스트
     */
    @GetMapping("/token/validate")
    public String validateRefreshToken(
            @RequestParam Long userId,
            @RequestParam String token
    ) {
        boolean isValid = jwtService.validateRefreshToken(userId, token);
        return isValid ? "유효한 토큰입니다." : "유효하지 않은 토큰입니다.";
    }
}
