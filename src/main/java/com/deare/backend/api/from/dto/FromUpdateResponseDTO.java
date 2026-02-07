package com.deare.backend.api.from.dto;

import java.time.LocalDateTime;

public record FromUpdateResponseDTO(
        Long fromId,
        String name,
        String bgColor,
        String fontColor,
        LocalDateTime updatedAt
) {
}
