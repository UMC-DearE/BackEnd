package com.deare.backend.domain.letter.repository.query.dto;

public record EmotionTagProjection(
        Long emotionId,
        String emotionName,
        EmotionCategoryProjection category
) {}
