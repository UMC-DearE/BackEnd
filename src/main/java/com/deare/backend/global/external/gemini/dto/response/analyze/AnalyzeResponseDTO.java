package com.deare.backend.global.external.gemini.dto.response.analyze;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AnalyzeResponseDTO {
    private String summary;
    private List<String> emotions;
}
