package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.*;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface LetterService {
    @Transactional(readOnly = true)
    LetterListResponseDTO getLetterList(
            Pageable pageable,
            Long userId,
            Long folderId,
            Long fromId,
            Boolean isLiked,
            String keyword
    );

    LetterDetailResponseDTO getLetterDetail(Long userId, Long letterId);

    void updateLetter(Long userId, Long letterId, LetterUpdateRequestDTO req);
    void deleteLetter(Long userId, Long letterId);

    LetterLikeResponseDTO likeLetter(Long userId, Long letterId);
    LetterLikeResponseDTO unlikeLetter(Long userId, Long letterId);

    LetterPinResponseDTO updatePinned(Long userId, Long letterId, LetterPinRequestDTO request);
}
