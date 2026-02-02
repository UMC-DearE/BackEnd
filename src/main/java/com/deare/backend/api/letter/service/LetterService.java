package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.LetterDetailResponseDTO;
import com.deare.backend.api.letter.dto.LetterListResponseDTO;
import org.springframework.data.domain.Pageable;

public interface LetterService {
    LetterListResponseDTO getLetterList(
            Long userId,
            Pageable pageable,
            Long folderId,
            Long fromId,
            String keyword
    );

    LetterDetailResponseDTO getLetterDetail(Long userId, Long letterId);
}
