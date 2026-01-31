package com.deare.backend.api.letter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LetterItemDTO(
        long id,
        String content,
        boolean isLiked,
        LocalDate receivedAt,
        LocalDateTime createdAt,
        long fromId,
        LetterFromDTO from,
        Long folderId
) {
}
