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

    // 랜덤 문구 선택 시 최소 문장 길이 (너무 짧은 문장 제외)
    private static final int MIN_SENTENCE_LEN = 10;

    // 랜덤 문구 최대 길이 (UI에서 최대 2줄 표시)
    private static final int MAX_PHRASE_CHARS = 62;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final LetterRepository letterRepository;

    public RandomLetterResponseDTO getTodayRandomLetter(long userId) {
        LocalDate today = LocalDate.now(ZONE);

        // userId + 날짜 키
        String key = cacheKey(userId, today);

        // 1) Redis HIT: 이미 오늘의 랜덤 결과가 있으면 그대로 반환
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            RandomLetterCacheValue v = fromJson(cached);

            // 캐시 불일치(삭제된 letterId)면 캐시 삭제 후 재생성
            if (v.hasLetter() && v.letterId() != null && !letterRepository.existsById(v.letterId())) {
                redisTemplate.delete(key);

                RandomLetterCacheValue recreated = createValue(userId, today);
                Duration ttl = ttlUntilNextMidnight();
                redisTemplate.opsForValue().set(key, toJson(recreated), ttl);

                return toResponseDTO(recreated, today);
            }

            return toResponseDTO(v, today);
        }

        // 2) Redis MISS: 오늘의 랜덤 편지 새로 생성
        RandomLetterCacheValue created = createValue(userId, today);

        // TTL은 다음 자정까지로 설정 → 자정 지나면 자동 만료
        Duration ttl = ttlUntilNextMidnight();
        String json = toJson(created);

        // 동시성 처리
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, json, ttl);

        // 다른 요청이 먼저 캐시를 생성한 경우, 해당 값 반환
        if (Boolean.FALSE.equals(ok)) {
            String latest = redisTemplate.opsForValue().get(key);
            if (latest != null) {
                return toResponseDTO(fromJson(latest), today);
            }
        }

        // setIfAbsent 성공 또는 재시도
        return toResponseDTO(created, today);
    }

    private RandomLetterCacheValue createValue(long userId, LocalDate today) {

        long count = letterRepository.countVisibleLettersByUser(userId);

        // 편지가 없으면 hasLetter=false
        if (count == 0) {
            return new RandomLetterCacheValue(
                    userId,
                    false,
                    today.toString(),
                    null,
                    null,
                    false
            );
        }

        // 0 ~ count-1 사이 랜덤 offset
        long offset = ThreadLocalRandom.current().nextLong(count);

        // offset 기반으로 1개 조회
        Letter letter = letterRepository
                .findRandomLetterByUser(userId, offset)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.NOT_FOUND));

        // 편지 본문에서 랜덤 문구 추출
        String phrase = extractRandomPhrase(letter.getContent());

        return new RandomLetterCacheValue(
                userId,
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

        // 기본 문장 분리:
        // 1) 문장부호(.!?)+공백 기준
        // 2) 줄바꿈 기준
        String[] raw = normalized.split("(?<=[.!?])\\s+|\\n+");

        // 너무 짧은 후보 제거
        List<String> candidates = new ArrayList<>();
        for (String s : raw) {
            String t = s.trim();
            if (t.length() >= MIN_SENTENCE_LEN) candidates.add(t);
        }

        // 후보가 하나도 없으면 전체 본문 일부를 잘라서 반환
        if (candidates.isEmpty()) return ellipsis(normalized, MAX_PHRASE_CHARS);

        // 후보 중 랜덤 선택
        String picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return ellipsis(picked, MAX_PHRASE_CHARS);
    }

    private String ellipsis(String s, int maxChars) {
        if (s == null) return null;
        if (s.length() <= maxChars) return s;
        return s.substring(0, Math.max(0, maxChars - 1)) + "…";
    }

    private RandomLetterResponseDTO toResponseDTO(RandomLetterCacheValue v, LocalDate today) {
        // 화면 표시용 month(Jan, Feb...) - Locale.ENGLISH로 고정
        String month = today.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        // 요일 한글 표시
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
        // +1초는 경계값(정각)에서 TTL 0 되는 케이스 방지용
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
