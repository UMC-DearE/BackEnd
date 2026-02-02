package com.deare.backend.domain.letter.repository.query.dto;

public record EmotionCategoryProjection(
        Long categoryId,
        String type,
        String bgColor,
        String fontColor
) {}
