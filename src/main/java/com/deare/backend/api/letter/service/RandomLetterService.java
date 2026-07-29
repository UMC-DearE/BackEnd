package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.cache.RandomLetterCacheValue;
import com.deare.backend.api.letter.dto.response.RandomLetterResponseDTO;
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

        // 오늘 하루의 랜덤 편지 최종 결정 캐시 키
        String key = cacheKey(userId, today);

        // 오늘 날짜 기준 고정 해제 유예 캐시 키
        String graceKey = unpinGraceKey(userId, today);

        String graceCached = redisTemplate.opsForValue().get(graceKey);

        if (graceCached != null) {
            RandomLetterCacheValue v = fromJson(graceCached);

            // 캐시가 정상이고 letterId가 존재하는 경우
            if (v != null && v.letterId() != null) {

                // DB에서 현재 고정 상태 확인
                Optional<Boolean> pinnedOpt = letterRepository.findIsPinnedByUserIdAndLetterId(userId, v.letterId());

                // 유예 캐시가 가리키던 편지가 삭제된 경우
                if (pinnedOpt.isEmpty()) {
                    redisTemplate.delete(graceKey);
                    return lockOutForToday(key, today);

                } else if (Boolean.TRUE.equals(pinnedOpt.get())) {
                    // 유예 중이던 편지가 다시 고정된 경우
                    redisTemplate.delete(graceKey);

                } else {
                    // 오늘은 기존 고정 편지를 그대로 내려줌
                    return toResponseDTO(
                            true,
                            today,
                            v.letterId(),
                            v.randomPhrase(),
                            false
                    );
                }
            } else {
                // JSON 파싱 실패 또는 비정상 캐시는 삭제 후 정상 흐름 진행
                redisTemplate.delete(graceKey);
            }
        }

        // 현재 DB 기준으로 고정된 편지가 있는지 조회
        Optional<Letter> pinnedLetterOpt =
                letterRepository.findPinnedLetterByUser(userId);

        if (pinnedLetterOpt.isPresent()) {
            Letter pinnedLetter = pinnedLetterOpt.get();

            // 사용자 고정 편지 전용 캐시 키
            String pinnedKey = pinnedKey(userId);

            String pinnedCached = redisTemplate.opsForValue().get(pinnedKey);

            if (pinnedCached != null) {
                RandomLetterCacheValue v = fromJson(pinnedCached);

                if (v == null || v.letterId() == null) {
                    redisTemplate.delete(pinnedKey);

                } else if (!Objects.equals(v.letterId(), pinnedLetter.getId())) {
                    redisTemplate.delete(pinnedKey);

                } else {
                    // 캐시가 정상이고 DB와 일치하면 그대로 반환
                    return toResponseDTO(
                            true,
                            today,
                            v.letterId(),
                            v.randomPhrase(),
                            true
                    );
                }
            }

            // 캐시가 없거나 무효한 경우 -> 새로 문구 생성 후 저장
            String phrase = extractRandomPhrase(pinnedLetter.getContent());
            RandomLetterCacheValue createdPinned = new RandomLetterCacheValue(pinnedLetter.getId(), phrase);

            redisTemplate.opsForValue().set(pinnedKey, toJson(createdPinned));

            return toResponseDTO(
                    true,
                    today,
                    createdPinned.letterId(),
                    createdPinned.randomPhrase(),
                    true
            );
        }

        // 현재 DB에는 고정 편지가 없는데 pinned 캐시는 남아 있는 경우 (고정 해제 직후)
        String pinnedKey = pinnedKey(userId);

        String pinnedCached = redisTemplate.opsForValue().get(pinnedKey);

        if (pinnedCached != null) {
            RandomLetterCacheValue v = fromJson(pinnedCached);

            if (v != null && v.letterId() != null) {

                Optional<Boolean> pinnedOpt =
                        letterRepository.findIsPinnedByUserIdAndLetterId(userId, v.letterId());

                // DB에 편지가 여전히 존재하면 오늘 자정까지 유예로 유지
                if (pinnedOpt.isPresent()) {

                    Duration ttl = ttlUntilNextMidnight();

                    // 오늘 날짜 유예 캐시에 저장 (자정까지 유지)
                    redisTemplate.opsForValue().set(graceKey, toJson(v), ttl);

                    // pinned 캐시는 제거
                    redisTemplate.delete(pinnedKey);

                    return toResponseDTO(
                            true,
                            today,
                            v.letterId(),
                            v.randomPhrase(),
                            false
                    );
                }
            }

            // pinned 캐시가 가리키던 편지가 삭제된 경우
            redisTemplate.delete(pinnedKey);
            return lockOutForToday(key, today);
        }

        // 1) Redis HIT: 오늘 이미 랜덤 결과가 있으면 그걸 사용
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            RandomLetterCacheValue v = fromJson(cached);

            // JSON 자체를 읽지 못한 경우 캐시 삭제 후 재생성
            if (v == null) {
                redisTemplate.delete(key);
                return createAndCache(userId, today, key);
            }

            // 오늘은 더 이상 편지를 보여주지 않는 경우
            if (v.letterId() == null) {
                return toResponseDTO(
                        false,
                        today,
                        null,
                        null,
                        false
                );
            }

            Optional<Boolean> pinnedOpt = letterRepository.findIsPinnedByUserIdAndLetterId(userId, v.letterId());

            // 오늘 추첨된 편지가 삭제된 경우 재추첨하지 않음
            if (pinnedOpt.isEmpty()) {
                return lockOutForToday(key, today);
            }

            // pinned 상태는 DB의 최신값 사용
            boolean pinnedNow = pinnedOpt.orElse(false);

            return toResponseDTO(
                    true,
                    today,
                    v.letterId(),
                    v.randomPhrase(),
                    pinnedNow
            );
        }

        // 2) Redis MISS: 오늘의 랜덤 편지 새로 생성
        return createAndCache(userId, today, key);
    }

    private RandomLetterResponseDTO lockOutForToday(
            String key,
            LocalDate today
    ) {
        RandomLetterCacheValue empty =
                new RandomLetterCacheValue(null, null);

        redisTemplate.opsForValue().set(
                key,
                toJson(empty),
                ttlUntilNextMidnight()
        );

        return toResponseDTO(
                false,
                today,
                null,
                null,
                false
        );
    }

    private RandomLetterResponseDTO createAndCache(long userId, LocalDate today, String key) {
        long count = letterRepository.countVisibleLettersByUser(userId);

        // 편지가 없으면 캐시 저장 없이 바로 응답
        if (count == 0) {
            return toResponseDTO(
                    false,
                    today,
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
                .orElseThrow(() -> new GeneralException(LetterErrorCode.LETTER_NOT_FOUND));

        // 편지 본문에서 랜덤 문구 추출
        String phrase = extractRandomPhrase(letter.getContent());
        RandomLetterCacheValue created = new RandomLetterCacheValue(letter.getId(), phrase);

        Duration ttl = ttlUntilNextMidnight();
        String json = toJson(created);

        // 동시성 처리: 먼저 저장한 요청이 있으면 그 값을 사용
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, json, ttl);

        RandomLetterCacheValue finalValue = created;
        if (Boolean.FALSE.equals(ok)) {
            String latest = redisTemplate.opsForValue().get(key);
            if (latest != null) {
                RandomLetterCacheValue parsed = fromJson(latest);
                if (parsed != null && parsed.letterId() != null) {
                    finalValue = parsed;
                }
            }
        }

        // pinned 상태는 DB의 최신값 사용
        boolean pinnedNow = letterRepository.findIsPinnedByUserIdAndLetterId(userId, finalValue.letterId())
                .orElse(false);

        return toResponseDTO(
                true,
                today,
                finalValue.letterId(),
                finalValue.randomPhrase(),
                pinnedNow
        );
    }

    private String extractRandomPhrase(String content) {
        if (content == null) return null;

        String normalized = content.trim();
        if (normalized.isEmpty()) return null;

        /*
         * 기본 문장 분리:
         * 1. 문장부호(.!?)+공백 기준
         * 2. 줄바꿈 기준
         */
        String[] raw = normalized.split("(?<=[.!?])\\s+|\\n+");

        // 너무 짧은 후보 제거
        List<String> candidates = new ArrayList<>();
        for (String s : raw) {
            String t = s.trim();
            if (t.length() >= MIN_SENTENCE_LEN) candidates.add(t);
        }

        // 후보가 하나도 없으면 전체 본문 일부를 잘라서 반환
        if (candidates.isEmpty()) return ellipsis(normalized);

        // 후보 중 랜덤 선택
        String picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        return ellipsis(picked);
    }

    private String ellipsis(String s) {
        if (s == null) return null;
        if (s.length() <= RandomLetterService.MAX_PHRASE_CHARS) return s;
        return s.substring(0, Math.max(0, RandomLetterService.MAX_PHRASE_CHARS - 1)) + "…";
    }

    private RandomLetterResponseDTO toResponseDTO(
            boolean hasLetter,
            LocalDate today,
            Long letterId,
            String randomPhrase,
            boolean isPinned
    ) {
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
                hasLetter,
                new RandomLetterResponseDTO.DateDTO(
                        today.toString(),
                        month,
                        today.getDayOfMonth(),
                        dowKo
                ),
                letterId,
                randomPhrase,
                isPinned
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

    private String pinnedKey(long userId) {
        return "letters:pinned:" + userId;
    }

    private String unpinGraceKey(long userId, LocalDate date) {
        return "letters:unpin-grace:" + userId + ":" + date;
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
            return null;
        }
    }
}
