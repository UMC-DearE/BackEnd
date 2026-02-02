package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.LetterDetailResponseDTO;
import com.deare.backend.api.letter.dto.LetterLikeResponseDTO;
import com.deare.backend.api.letter.dto.LetterListResponseDTO;
import com.deare.backend.api.letter.dto.LetterUpdateRequestDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface LetterService {
    LetterListResponseDTO getLetterList(
            Long userId,
            Pageable pageable,
            Long folderId,
            Long fromId,
            String keyword
    );
    LetterDetailResponseDTO getLetterDetail(Long userId, Long letterId);

    void updateLetter(Long userId, Long letterId, LetterUpdateRequestDTO req);
    void deleteLetter(Long userId, Long letterId);

    LetterLikeResponseDTO likeLetter(Long userId, Long letterId);
    LetterLikeResponseDTO unlikeLetter(Long userId, Long letterId);
}
