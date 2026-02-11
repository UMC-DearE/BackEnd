package com.deare.backend.api.analyze.dto.response;

import com.deare.backend.api.analyze.dto.result.EmotionDTO;
import com.deare.backend.domain.emotion.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AnalyzeLetterResponseDTO {
    private String summary;
    private List<EmotionDTO> emotions;

    public static AnalyzeLetterResponseDTO of(
            String summary,
            List<Emotion> emotions
    ){
        return AnalyzeLetterResponseDTO.builder()
                .summary(summary)
                .emotions(
                        emotions.stream()
                                .map(EmotionDTO::from)
                                .toList()
                ).build();
    }
}
