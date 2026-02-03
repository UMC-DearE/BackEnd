package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.cache.RandomLetterCacheValue;
import com.deare.backend.api.letter.dto.RandomLetterResponseDTO;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RandomLetterService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private static final int MIN_SENTENCE_LEN = 6;
    private static final int MAX_PHRASE_CHARS = 60;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LetterRepository letterRepository;

    public RandomLetterResponseDTO getTodayRandomLetter(long userId) {
        LocalDate today = LocalDate.now(ZONE);
        String key = cacheKey(userId, today);

        // 1) Redis hit
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            RandomLetterCacheValue v = fromJson(cached);

            // 404: 캐시에 저장된 letterId가 삭제되어 불일치
            // 추후 다른 편지가 조회되도록 리팩토링
            if (v.hasLetter() && v.letterId() != null && !letterRepository.existsById(v.letterId())) {
                throw new GeneralException(LetterErrorCode.NOT_FOUND);
            }
            return toResponseDTO(v, today);
        }

        // 2) Redis miss -> 생성
        RandomLetterCacheValue created = createValue(userId, today);

        // 3) TTL = 다음 자정까지
        Duration ttl = ttlUntilNextMidnight();
        String json = toJson(created);

        // 동시성
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, json, ttl);
        if (Boolean.FALSE.equals(ok)) {
            String latest = redisTemplate.opsForValue().get(key);
            if (latest != null) {
                return toResponseDTO(fromJson(latest), today);
            }
        }

        // setIfAbsent 성공 or fallback 실패 -> created 반환
        if (Boolean.TRUE.equals(ok)) return toResponseDTO(created, today);

        redisTemplate.opsForValue().set(key, json, ttl);
        return toResponseDTO(created, today);
    }

    private RandomLetterCacheValue createValue(long userId, LocalDate today) {
        long count = letterRepository.count();

        if (count == 0) {
            return new RandomLetterCacheValue(
                    false,
                    today.toString(),
                    null,
                    null,
                    false
            );
        }

        long offset = ThreadLocalRandom.current().nextLong(count);
        Letter letter = letterRepository
                .findRandomLetterByUser(userId, offset)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.NOT_FOUND));

        String phrase = extractRandomPhrase(letter.getContent());

        return new RandomLetterCacheValue(
                true,
                today.toString(),
                letter.getId(),
                phrase,
                letter.isPinned()
        );
    }

    private String extractRandomPhrase(String content) {
        if (content == null) return null;

        String normalized = content.trim();
        if (normalized.isEmpty()) return null;

        // 기본 문장 분리: 줄바꿈 or 문장부호(.!?)
        String[] raw = normalized.split("(?<=[.!?])\\s+|\\n+");
        List<String> candidates = new ArrayList<>();
        for (String s : raw) {
            String t = s.trim();
            if (t.length() >= MIN_SENTENCE_LEN) candidates.add(t);
        }

        // 후보 없으면 fallback
        if (candidates.isEmpty()) return ellipsis(normalized, MAX_PHRASE_CHARS);

        String picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return ellipsis(picked, MAX_PHRASE_CHARS);
    }

    private String ellipsis(String s, int maxChars) {
        if (s == null) return null;
        if (s.length() <= maxChars) return s;
        return s.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private RandomLetterResponseDTO toResponseDTO(RandomLetterCacheValue v, LocalDate today) {
        String month = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // Jan
        String dowKo = switch (today.getDayOfWeek()) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };

        return new RandomLetterResponseDTO(
                v.hasLetter(),
                new RandomLetterResponseDTO.DateDTO(
                        today.toString(),
                        month,
                        today.getDayOfMonth(),
                        dowKo
                ),
                v.letterId(),
                v.randomPhrase(),
                v.isPinned()
        );
    }

    private Duration ttlUntilNextMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(ZONE);
        return Duration.between(now, nextMidnight).plusSeconds(1);
    }

    private String cacheKey(long userId, LocalDate date) {
        return "letters:random:" + userId + ":" + date;
    }

    private String toJson(RandomLetterCacheValue v) {
        try {
            return objectMapper.writeValueAsString(v);
        } catch (Exception e) {
            throw new GeneralException(LetterErrorCode.INTERNAL_ERROR);
        }
    }

    private RandomLetterCacheValue fromJson(String json) {
        try {
            return objectMapper.readValue(json, RandomLetterCacheValue.class);
        } catch (Exception e) {
            throw new GeneralException(LetterErrorCode.INTERNAL_ERROR);
        }
    }
}
