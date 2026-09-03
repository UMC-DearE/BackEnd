package com.deare.backend.api.analyze.service;

import com.deare.backend.api.analyze.dto.request.AnalyzeLetterRequestDTO;
import com.deare.backend.domain.emotion.entity.Emotion;
import com.deare.backend.domain.emotion.entity.EmotionCategory;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.adapter.analyze.AnalyzeAdapter;
import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;
import com.deare.backend.global.external.gemini.limit.AiUsageLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LetterAnalyzeServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USAGE_KEY = "AI_USAGE:LETTER_ANALYZE:1:20260903";

    @Mock private AnalyzeAdapter analyzeAdapter;
    @Mock private EmotionRepository emotionRepository;
    @Mock private AiUsageLimiter aiUsageLimiter;
    @InjectMocks private LetterAnalyzeService letterAnalyzeService;

    @Test
    void releasesUsageWhenAnalyzeAdapterFails() {
        AnalyzeLetterRequestDTO request = AnalyzeLetterRequestDTO.of("편지 내용");
        when(aiUsageLimiter.reserve(USER_ID)).thenReturn(USAGE_KEY);
        when(analyzeAdapter.analyze("편지 내용"))
                .thenThrow(new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT));

        assertThatThrownBy(() -> letterAnalyzeService.analyze(request, USER_ID))
                .isInstanceOf(ExternalApiException.class);

        verify(aiUsageLimiter).reserve(USER_ID);
        verify(aiUsageLimiter).release(USAGE_KEY);
    }

    @Test
    void releasesUsageWhenEmotionCountIsInvalid() {
        AnalyzeLetterRequestDTO request = AnalyzeLetterRequestDTO.of("편지 내용");
        AnalyzeResponseDTO response = analyzeResponse("요약", List.of("기쁨")); // 2~3개여야 하는데 1개 -> 검증 실패
        when(aiUsageLimiter.reserve(USER_ID)).thenReturn(USAGE_KEY);
        when(analyzeAdapter.analyze("편지 내용")).thenReturn(response);

        assertThatThrownBy(() -> letterAnalyzeService.analyze(request, USER_ID))
                .isInstanceOf(GeneralException.class);

        verify(aiUsageLimiter).reserve(USER_ID);
        verify(aiUsageLimiter).release(USAGE_KEY);
    }

    @Test
    void releasesUsageWhenEmotionDoesNotExistInDb() {
        AnalyzeLetterRequestDTO request = AnalyzeLetterRequestDTO.of("편지 내용");
        AnalyzeResponseDTO response = analyzeResponse("요약", List.of("기쁨", "슬픔"));
        when(aiUsageLimiter.reserve(USER_ID)).thenReturn(USAGE_KEY);
        when(analyzeAdapter.analyze("편지 내용")).thenReturn(response);
        when(emotionRepository.findByNameIn(List.of("기쁨", "슬픔")))
                .thenReturn(List.of()); // DB에 하나도 존재하지 않음 -> 검증 실패

        assertThatThrownBy(() -> letterAnalyzeService.analyze(request, USER_ID))
                .isInstanceOf(GeneralException.class);

        verify(aiUsageLimiter).reserve(USER_ID);
        verify(aiUsageLimiter).release(USAGE_KEY);
    }

    @Test
    void doesNotReleaseUsageOnSuccess() {
        AnalyzeLetterRequestDTO request = AnalyzeLetterRequestDTO.of("편지 내용");
        AnalyzeResponseDTO response = analyzeResponse("요약", List.of("기쁨", "슬픔"));
        List<Emotion> emotions = List.of(emotionWithCategory(), emotionWithCategory());
        when(analyzeAdapter.analyze("편지 내용")).thenReturn(response);
        when(emotionRepository.findByNameIn(List.of("기쁨", "슬픔"))).thenReturn(emotions);

        letterAnalyzeService.analyze(request, USER_ID);

        verify(aiUsageLimiter).reserve(USER_ID);
        verify(aiUsageLimiter, never()).release(anyString());
    }

    private AnalyzeResponseDTO analyzeResponse(String summary, List<String> emotions) {
        AnalyzeResponseDTO dto = mock(AnalyzeResponseDTO.class);
        when(dto.getSummary()).thenReturn(summary);
        when(dto.getEmotions()).thenReturn(emotions);
        return dto;
    }

    private Emotion emotionWithCategory() {
        Emotion emotion = mock(Emotion.class);
        EmotionCategory category = mock(EmotionCategory.class);
        when(emotion.getEmotionCategory()).thenReturn(category);
        return emotion;
    }
}
