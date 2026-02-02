package com.deare.backend.api.letter.dto;

public record EmotionTagDTO(
        Long emotionId,
        String emotionName,
        EmotionCategoryDTO category
) {}
