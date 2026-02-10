package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.request.LetterCreateRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterPinRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterReplyUpsertRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterUpdateRequestDTO;
import com.deare.backend.api.letter.dto.response.*;
import org.springframework.data.domain.Pageable;

public interface LetterService {
    LetterListResponseDTO getLetterList(
            Pageable pageable,
            Long userId,
            Long folderId,
            Long fromId,
            Boolean isLiked,
            String keyword
    );
    LetterDetailResponseDTO getLetterDetail(Long userId, Long letterId);

    LetterCreateResponseDTO createLetter(Long userId, LetterCreateRequestDTO req);
    void updateLetter(Long userId, Long letterId, LetterUpdateRequestDTO req);
    void deleteLetter(Long userId, Long letterId);

    LetterLikeResponseDTO likeLetter(Long userId, Long letterId);
    LetterLikeResponseDTO unlikeLetter(Long userId, Long letterId);

    void upsertReply(Long userId, Long letterId, LetterReplyUpsertRequestDTO req);
    void deleteReply(Long userId, Long letterId);

    LetterPinResponseDTO updatePinned(Long userId, Long letterId, LetterPinRequestDTO request);
}
