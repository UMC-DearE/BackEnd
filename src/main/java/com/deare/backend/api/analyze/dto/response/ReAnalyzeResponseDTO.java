package com.deare.backend.api.analyze.dto.response;

import com.deare.backend.api.analyze.dto.result.EmotionDTO;
import com.deare.backend.domain.emotion.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReAnalyzeResponseDTO {
    private final String summary;
    private final List<EmotionDTO> emotions;

    public static ReAnalyzeResponseDTO of(String  summary, List<Emotion> emotions) {
        List<EmotionDTO> emotionDTOs=emotions.stream()
                .map(EmotionDTO::simpleFrom)
                .toList();

        return new ReAnalyzeResponseDTO(summary, emotionDTOs);
    }
}
