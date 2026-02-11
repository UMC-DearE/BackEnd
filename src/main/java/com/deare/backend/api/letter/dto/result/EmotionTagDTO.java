package com.deare.backend.api.letter.dto.result;

public record EmotionTagDTO(
        Long emotionId,
        String emotionName,
        EmotionCategoryDTO category
) {}
