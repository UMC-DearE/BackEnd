package com.deare.backend.api.letter.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LetterDetailResponseDTO(
        String content,
        LocalDate receivedAt,
        String aiSummary,
        boolean isLiked,
        String reply,
        String fromName,
        String fromBgColor,
        String fromFontColor,
        LocalDateTime createdAt,
        List<String> imageUrls
) {
}
