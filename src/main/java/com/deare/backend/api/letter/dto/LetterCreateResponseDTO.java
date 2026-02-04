package com.deare.backend.api.letter.dto;

import java.time.LocalDateTime;

public record LetterCreateResponseDTO(
        Long letterId,
        LocalDateTime createdAt
) {}