package com.deare.backend.domain.letter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LetterItemDTO(
        long id,
        String content,
        boolean isLiked,
        LocalDate receivedAt,
        LocalDateTime createdAt,
        long fromId,
        String fromName,
        String fromBgColor,
        String fromFontColor,
        Long folderId
) {
}
