package com.deare.backend.api.letter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record LetterCreateRequestDTO(

        String content,
        String aiSummary,
        List<Long> emotionIds,
        Long fromId,
        LocalDate receivedAt,
        List<Long> imageIds
) {}