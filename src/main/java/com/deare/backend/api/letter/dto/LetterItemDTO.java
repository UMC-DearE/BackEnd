package com.deare.backend.api.letter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LetterItemDTO(
        long id,
        String excerpt,
        boolean isLiked,
        LocalDate receivedAt,
        LocalDateTime createdAt,
        LetterFromDTO from,
        Long folderId
) {
}
