package com.deare.backend.api.analyze.dto.response;

import com.deare.backend.domain.emotion.entity.Emotion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionResponseDTO {

    private Long emotionId;
    private String emotionName;
    private CategoryResponseDTO category;

    public static EmotionResponseDTO from(Emotion emotion){
        return EmotionResponseDTO.builder()
                .emotionId(emotion.getId())
                .emotionName(emotion.getName())
                .category(CategoryResponseDTO.from(emotion.getEmotionCategory()))
                .build();
    }
}
