package com.deare.backend.global.external.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalyzeResponseDTO {
    /**
     * summary: ai 한 줄 요약
     * emotion: 감정 태그
     */
    private String summary;
    private List<String> emotion=List.of();
}
