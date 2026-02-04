package com.deare.backend.api.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmotionDistributionDTO {
    private String emotion;
    private double percent;

    public static EmotionDistributionDTO of(String emotion, double percent) {
        return EmotionDistributionDTO.builder()
                .emotion(emotion)
                .percent(percent)
                .build();
    }
}
