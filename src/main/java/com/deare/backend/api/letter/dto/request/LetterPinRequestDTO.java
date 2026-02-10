package com.deare.backend.api.letter.dto.request;

import jakarta.validation.constraints.NotNull;

public record LetterPinRequestDTO(
        @NotNull
        Boolean pinned
) {
}
