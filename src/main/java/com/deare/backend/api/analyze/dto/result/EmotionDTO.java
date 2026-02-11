package com.deare.backend.api.analyze.dto.result;

import com.deare.backend.domain.emotion.entity.Emotion;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionDTO {

    private Long emotionId;
    private String emotionName;
    private CategoryDTO category;

    public static EmotionDTO from(Emotion emotion){
        return EmotionDTO.builder()
                .emotionId(emotion.getId())
                .emotionName(emotion.getName())
                .category(CategoryDTO.from(emotion.getEmotionCategory()))
                .build();
    }

    public static EmotionDTO simpleFrom(Emotion emotion){
        return EmotionDTO.builder()
                .emotionId(emotion.getId())
                .emotionName(emotion.getName())
                .build();
    }
}
