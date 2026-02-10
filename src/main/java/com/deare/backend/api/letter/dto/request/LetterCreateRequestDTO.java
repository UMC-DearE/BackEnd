package com.deare.backend.api.letter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record LetterCreateRequestDTO(
        @NotBlank
        String content,
        @NotBlank
        String aiSummary,
        @NotEmpty
        List<Long> emotionIds,
        @NotNull(message = "발신자는 필수입니다")
        Long fromId,
        LocalDate receivedAt,
        List<Long> imageIds
) {}