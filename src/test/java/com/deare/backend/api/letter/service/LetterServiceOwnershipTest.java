package com.deare.backend.api.letter.service;

import com.deare.backend.api.analyze.service.LetterAnalyzeService;
import com.deare.backend.api.letter.dto.request.LetterCreateRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterUpdateRequestDTO;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.from.exception.FromErrorCode;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LetterServiceOwnershipTest {

    private static final Long USER_ID = 1L;
    private static final Long LETTER_ID = 10L;
    private static final Long FROM_ID = 20L;

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
    @InjectMocks private LetterServiceImpl letterService;

    @Test
    void forwardsBlindIndexCandidatesResolvedForAuthenticatedUser() {
        PageRequest pageable = PageRequest.of(0, 10);
        Set<Long> candidateIds = Set.of(11L, 12L);
        when(searchCandidateResolver.resolve(USER_ID, "keyword"))
                .thenReturn(Optional.of(candidateIds));
        when(letterRepository.findLettersForList(
                USER_ID, null, null, null, "keyword", candidateIds, pageable
        )).thenReturn(Page.empty(pageable));

        letterService.getLetterList(
                pageable, USER_ID, null, null, null, "keyword"
        );

        verify(searchCandidateResolver).resolve(USER_ID, "keyword");
        verify(letterRepository).findLettersForList(
                USER_ID, null, null, null, "keyword", candidateIds, pageable
        );
    }

    @Test
    void rejectsLetterNotOwnedByCurrentUser() {
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> letterService.deleteLetter(USER_ID, LETTER_ID))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(LetterErrorCode.LETTER_NOT_FOUND));
    }

    @Test
    void preservesDeletedLetterErrorForOwner() {
        Letter letter = mock(Letter.class);
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));
        when(letter.isDeleted()).thenReturn(true);

        assertThatThrownBy(() -> letterService.deleteLetter(USER_ID, LETTER_ID))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(LetterErrorCode.DELETED_LETTER));
        verify(letter, never()).softDelete();
    }

    @Test
    void allowsOwnerToDeleteActiveLetter() {
        Letter letter = mock(Letter.class);
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));

        letterService.deleteLetter(USER_ID, LETTER_ID);

        verify(letter).softDelete();
        verify(searchTokenSynchronizer).deleteTokens(letter);
    }

    @Test
    void rejectsForeignFromWhenCreatingLetter() {
        User user = mock(User.class);
        LetterCreateRequestDTO request = new LetterCreateRequestDTO(
                "content",
                "summary",
                List.of(1L),
                FROM_ID,
                null,
                List.of()
        );
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(fromRepository.findByIdAndUser_IdAndIsDeletedFalse(FROM_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> letterService.createLetter(USER_ID, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(LetterErrorCode.FROM_NOT_FOUND));
        verify(letterRepository, never()).save(any());
    }

    @Test
    void rejectsForeignFromWhenUpdatingLetter() {
        Letter letter = mock(Letter.class);
        LetterUpdateRequestDTO request = new LetterUpdateRequestDTO();
        ReflectionTestUtils.setField(request, "fromId", FROM_ID);
        when(letterRepository.findByIdAndUser_Id(LETTER_ID, USER_ID)).thenReturn(Optional.of(letter));
        when(fromRepository.findByIdAndUser_IdAndIsDeletedFalse(FROM_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> letterService.updateLetter(USER_ID, LETTER_ID, request))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(FromErrorCode.FROM_40401));
    }
}
