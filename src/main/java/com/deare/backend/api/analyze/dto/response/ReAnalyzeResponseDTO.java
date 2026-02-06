package com.deare.backend.api.analyze.dto.response;

import com.deare.backend.domain.emotion.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReAnalyzeResponseDTO {
    private final String summary;
    private final List<EmotionResponseDTO> emotions;

    public static ReAnalyzeResponseDTO of(String  summary, List<Emotion> emotions) {
        List<EmotionResponseDTO> emotionDTOs=emotions.stream()
                .map(EmotionResponseDTO::simpleFrom)
                .toList();

        return new ReAnalyzeResponseDTO(summary, emotionDTOs);
    }
}
