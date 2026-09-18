package com.deare.backend.api.letter.service;

import com.deare.backend.api.analyze.dto.response.ReAnalyzeResponseDTO;
import com.deare.backend.api.analyze.service.LetterAnalyzeService;
import com.deare.backend.api.letter.dto.request.LetterUpdateRequestDTO;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LetterServiceUpdateReanalyzeTest {

    private static final Long USER_ID = 1L;
    private static final Long LETTER_ID = 10L;
    private static final String ORIGINAL_CONTENT = "하나 둘 셋 넷 다섯 여섯 여덟 아홉 열 열하나";

    @Mock private LetterRepository letterRepository;
    @Mock private LetterEmotionQueryRepository letterEmotionQueryRepository;
    @Mock private FromRepository fromRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmotionRepository emotionRepository;
    @Mock private LetterEmotionRepository letterEmotionRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private LetterImageRepository letterImageRepository;
    @Mock private LetterAnalyzeService letterAnalyzeService;
    @Mock private LetterSearchTokenSynchronizer searchTokenSynchronizer;
    @Mock private LetterSearchCandidateResolver searchCandidateResolver;
    @Mock private LetterContentEncryptionSynchronizer contentEncryptionSynchronizer;
    @Mock private LetterContentReader contentReader;
    @Mock private LetterSearchResultPager searchResultPager;
    @InjectMocks private LetterServiceImpl letterService;

    @BeforeEach
    void setUp() {
        // @Value로 주입되는 필드는 @InjectMocks가 채워주지 않으므로 직접 세팅한다.
        ReflectionTestUtils.setField(letterService, "reanalyzeChangeRateThreshold", 0.15);
    }

    private LetterUpdateRequestDTO requestWithContent(String content) {
        LetterUpdateRequestDTO request = new LetterUpdateRequestDTO();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }

    @Test
    void skipsReanalyzeWhenChangeRateBelowThreshold() {
        // 원문 10단어 중 1단어만 교체 -> 10% (임계값 15% 미만)
        Letter letter = mock(Letter.class);
        String updatedContent = "하나 둘 셋 넷 다섯 여섯 여덟 아홉 열 열둘";
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));
        when(contentReader.read(letter)).thenReturn(ORIGINAL_CONTENT);

        letterService.updateLetter(USER_ID, LETTER_ID, requestWithContent(updatedContent));

        verify(letterAnalyzeService, never()).analyzeForUpdate(any(), any());
        verify(letter, never()).updateContent(any());
        verify(contentEncryptionSynchronizer).synchronize(letter, USER_ID, updatedContent);
        verify(searchTokenSynchronizer).replaceTokens(letter, USER_ID, updatedContent);
    }

    @Test
    void triggersReanalyzeWhenChangeRateReachesThreshold() {
        // 원문 10단어 중 2단어 교체 -> 20% (임계값 15% 이상)
        Letter letter = mock(Letter.class);
        String updatedContent = "하나 둘 셋 넷 다섯 여섯 일곱 여덟 열 열둘";
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));
        when(contentReader.read(letter)).thenReturn(ORIGINAL_CONTENT);
        when(letterAnalyzeService.analyzeForUpdate(updatedContent, USER_ID))
                .thenReturn(new ReAnalyzeResponseDTO("새 요약", List.of()));

        letterService.updateLetter(USER_ID, LETTER_ID, requestWithContent(updatedContent));

        verify(letterAnalyzeService).analyzeForUpdate(updatedContent, USER_ID);
        verify(letter).updateContent("새 요약");
        verify(contentEncryptionSynchronizer).synchronize(letter, USER_ID, updatedContent);
        verify(searchTokenSynchronizer).replaceTokens(letter, USER_ID, updatedContent);
    }

    @Test
    void skipsEverythingWhenContentIsUnchanged() {
        Letter letter = mock(Letter.class);
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));
        when(contentReader.read(letter)).thenReturn(ORIGINAL_CONTENT);

        letterService.updateLetter(USER_ID, LETTER_ID, requestWithContent(ORIGINAL_CONTENT));

        verify(letterAnalyzeService, never()).analyzeForUpdate(any(), any());
        verify(contentEncryptionSynchronizer, never()).synchronize(any(), anyLong(), any());
        verify(searchTokenSynchronizer, never()).replaceTokens(any(), anyLong(), any());
    }
}
