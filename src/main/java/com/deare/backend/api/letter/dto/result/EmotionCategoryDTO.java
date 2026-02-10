package com.deare.backend.api.letter.dto.result;

public record EmotionCategoryDTO(
        Long categoryId,
        String type,
        String bgColor,
        String fontColor
) {}
