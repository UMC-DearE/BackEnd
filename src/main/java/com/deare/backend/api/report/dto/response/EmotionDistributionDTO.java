package com.deare.backend.api.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionDistributionDTO {
    private String emotion;
    private int percent;

    public static EmotionDistributionDTO of(String emotion, int percent) {
        return EmotionDistributionDTO.builder()
                .emotion(emotion)
                .percent(percent)
                .build();
    }
}
