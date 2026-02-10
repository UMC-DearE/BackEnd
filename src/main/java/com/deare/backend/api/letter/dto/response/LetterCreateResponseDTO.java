package com.deare.backend.api.letter.dto.response;

import java.time.LocalDateTime;

public record LetterCreateResponseDTO(
        Long letterId,
        LocalDateTime createdAt
) {}