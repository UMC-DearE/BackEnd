package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.response.RandomLetterResponseDTO;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RandomLetterServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final Map<String, String> store = new ConcurrentHashMap<>();

    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private LetterRepository letterRepository;
    private LetterContentReader contentReader;
    private RandomLetterService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        store.clear();

        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        letterRepository = mock(LetterRepository.class);
        contentReader = mock(LetterContentReader.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString()))
                .thenAnswer(inv -> store.get((String) inv.getArgument(0)));

        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString());

        doAnswer(inv -> {
            store.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenAnswer(inv -> {
                    String key = inv.getArgument(0);
                    String value = inv.getArgument(1);
                    if (store.containsKey(key)) return false;
                    store.put(key, value);
                    return true;
                });

        when(redisTemplate.delete(anyString()))
                .thenAnswer(inv -> store.remove((String) inv.getArgument(0)) != null);

        service = new RandomLetterService(
                redisTemplate,
                new ObjectMapper(),
                letterRepository,
                contentReader
        );
    }

    private Letter mockLetter(long id, String content) {
        Letter letter = mock(Letter.class);
        when(letter.getId()).thenReturn(id);
        when(letter.getContent()).thenReturn(content);
        when(contentReader.read(letter)).thenReturn(content);
        return letter;
    }

    private String pinnedKeyOf(long userId) {
        return "letters:pinned:" + userId;
    }

    private String dateKeyOf(long userId) {
        return "letters:random:" + userId + ":" + LocalDate.now(SEOUL);
    }

    @Test
    @DisplayName("정상 추첨: 하루 안에서는 같은 편지를 반복 반환한다")
    void normalDraw_isCachedForTheDay() {
        long userId = 100L;
        Letter letter = mockLetter(10L, "테스트용 편지 본문입니다. 오늘 하루도 힘내세요!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.countVisibleLettersByUser(eq(userId), any(LocalDateTime.class))).thenReturn(2L);
        when(letterRepository.findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(letter));
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 10L)).thenReturn(Optional.of(false));

        RandomLetterResponseDTO first = service.getTodayRandomLetter(userId);
        assertThat(first.hasLetter()).isTrue();
        assertThat(first.letterId()).isEqualTo(10L);
        assertThat(first.randomPhrase()).isNotBlank();

        RandomLetterResponseDTO second = service.getTodayRandomLetter(userId);
        assertThat(second.letterId()).isEqualTo(10L);

        verify(letterRepository, times(1)).findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("A. 오늘 추첨된(비고정) 편지가 삭제되면 재추첨하지 않고 하루 종일 잠긴다")
    void deletedDrawnLetter_locksForRestOfDay() {
        long userId = 101L;
        Letter letter = mockLetter(11L, "테스트용 편지 본문입니다. 오늘 하루도 힘내세요!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.countVisibleLettersByUser(eq(userId), any(LocalDateTime.class))).thenReturn(1L);
        when(letterRepository.findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(letter));
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 11L)).thenReturn(Optional.of(false));

        RandomLetterResponseDTO drawn = service.getTodayRandomLetter(userId);
        assertThat(drawn.letterId()).isEqualTo(11L);

        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 11L)).thenReturn(Optional.empty());

        RandomLetterResponseDTO afterDelete = service.getTodayRandomLetter(userId);
        assertThat(afterDelete.hasLetter()).isFalse();
        assertThat(afterDelete.letterId()).isNull();

        RandomLetterResponseDTO stillLocked = service.getTodayRandomLetter(userId);
        assertThat(stillLocked.hasLetter()).isFalse();

        verify(letterRepository, times(1)).findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("B. 고정한 편지가 삭제되면 재추첨하지 않고 하루 종일 잠긴다")
    void deletedPinnedLetter_locksForRestOfDay() {
        long userId = 102L;
        Letter pinnedLetter = mockLetter(20L, "고정된 편지 본문입니다. 오늘 하루도 힘내세요!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.of(pinnedLetter));

        RandomLetterResponseDTO pinned = service.getTodayRandomLetter(userId);
        assertThat(pinned.hasLetter()).isTrue();
        assertThat(pinned.letterId()).isEqualTo(20L);
        assertThat(pinned.isPinned()).isTrue();

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 20L)).thenReturn(Optional.empty());

        RandomLetterResponseDTO afterDelete = service.getTodayRandomLetter(userId);
        assertThat(afterDelete.hasLetter()).isFalse();

        RandomLetterResponseDTO stillLocked = service.getTodayRandomLetter(userId);
        assertThat(stillLocked.hasLetter()).isFalse();

        verify(letterRepository, never()).countVisibleLettersByUser(anyLong(), any(LocalDateTime.class));
        verify(letterRepository, never()).findRandomLetterByUser(anyLong(), anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("C. 고정 해제 후 유예 중이던 편지가 삭제되면 재추첨하지 않고 하루 종일 잠긴다")
    void deletedGracedLetter_locksForRestOfDay() {
        long userId = 103L;
        Letter letter = mockLetter(30L, "유예 중인 편지 본문입니다. 오늘 하루도 힘내세요!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.of(letter));
        RandomLetterResponseDTO pinnedResult = service.getTodayRandomLetter(userId);
        assertThat(pinnedResult.isPinned()).isTrue();

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 30L)).thenReturn(Optional.of(false));

        RandomLetterResponseDTO gracedResult = service.getTodayRandomLetter(userId);
        assertThat(gracedResult.hasLetter()).isTrue();
        assertThat(gracedResult.letterId()).isEqualTo(30L);
        assertThat(gracedResult.isPinned()).isFalse();

        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 30L)).thenReturn(Optional.empty());

        RandomLetterResponseDTO afterDelete = service.getTodayRandomLetter(userId);
        assertThat(afterDelete.hasLetter()).isFalse();

        RandomLetterResponseDTO stillLocked = service.getTodayRandomLetter(userId);
        assertThat(stillLocked.hasLetter()).isFalse();

        verify(letterRepository, never()).countVisibleLettersByUser(anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("고정 캐시가 손상된 경우에는 잠그지 않고 정상적으로 새로 추첨한다")
    void corruptedPinnedCache_doesNotLockOut() {
        long userId = 104L;
        store.put(pinnedKeyOf(userId), "이것은-유효한-JSON이-아님");

        Letter letter = mockLetter(40L, "정상적으로 새로 뽑힌 편지 본문입니다. 오늘도 힘내세요!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.countVisibleLettersByUser(eq(userId), any(LocalDateTime.class))).thenReturn(1L);
        when(letterRepository.findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(letter));
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 40L)).thenReturn(Optional.of(false));

        RandomLetterResponseDTO result = service.getTodayRandomLetter(userId);

        assertThat(result.hasLetter()).isTrue();
        assertThat(result.letterId()).isEqualTo(40L);
        assertThat(store.containsKey(pinnedKeyOf(userId))).isFalse();
    }

    @Test
    @DisplayName("동시 추첨 경쟁에서 진 요청은 락아웃 센티널을 무시하지 않고 존중한다")
    void concurrentDraw_respectsLockoutWrittenByOtherRequest() {
        long userId = 105L;
        Letter letter = mockLetter(50L, "동시성 테스트용 편지 본문입니다. 오늘도 힘내세요!");
        String key = dateKeyOf(userId);

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.countVisibleLettersByUser(eq(userId), any(LocalDateTime.class))).thenReturn(1L);
        when(letterRepository.findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(letter));

        when(valueOperations.setIfAbsent(eq(key), anyString(), any(Duration.class)))
                .thenAnswer(inv -> {
                    store.put(key, "{\"letterId\":null,\"randomPhrase\":null}");
                    return false;
                });

        RandomLetterResponseDTO result = service.getTodayRandomLetter(userId);

        assertThat(result.hasLetter()).isFalse();
        assertThat(result.letterId()).isNull();
    }

    @Test
    @DisplayName("고정 우선순위: 이미 뽑힌 일반 편지가 있어도 새로 고정하면 즉시 그 편지를 보여준다")
    void pinningNewLetter_overridesExistingDraw() {
        long userId = 106L;
        Letter drawn = mockLetter(60L, "일반 추첨 편지 본문입니다. 오늘도 힘내세요!");
        Letter pinned = mockLetter(61L, "고정된 편지 본문입니다. 늘 응원합니다!");

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.empty());
        when(letterRepository.countVisibleLettersByUser(eq(userId), any(LocalDateTime.class))).thenReturn(2L);
        when(letterRepository.findRandomLetterByUser(eq(userId), anyLong(), any(LocalDateTime.class))).thenReturn(Optional.of(drawn));
        when(letterRepository.findIsPinnedByUserIdAndLetterId(userId, 60L)).thenReturn(Optional.of(false));

        RandomLetterResponseDTO before = service.getTodayRandomLetter(userId);
        assertThat(before.letterId()).isEqualTo(60L);

        when(letterRepository.findPinnedLetterByUser(userId)).thenReturn(Optional.of(pinned));

        RandomLetterResponseDTO afterPin = service.getTodayRandomLetter(userId);
        assertThat(afterPin.letterId()).isEqualTo(61L);
        assertThat(afterPin.isPinned()).isTrue();
    }
}
