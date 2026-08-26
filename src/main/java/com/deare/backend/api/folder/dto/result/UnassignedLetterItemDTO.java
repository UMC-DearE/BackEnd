package com.deare.backend.api.folder.dto.result;

import com.deare.backend.api.letter.dto.result.LetterFromDTO;
import com.deare.backend.api.letter.dto.result.LetterItemDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UnassignedLetterItemDTO(
        long id,
        String excerpt,
        boolean isLiked,
        LocalDate receivedAt,
        LocalDateTime createdAt,
        LetterFromDTO from
) {
    public static UnassignedLetterItemDTO from(LetterItemDTO item) {
        return new UnassignedLetterItemDTO(
                item.id(),
                item.excerpt(),
                item.isLiked(),
                item.receivedAt(),
                item.createdAt(),
                item.from()
        );
    }
}
