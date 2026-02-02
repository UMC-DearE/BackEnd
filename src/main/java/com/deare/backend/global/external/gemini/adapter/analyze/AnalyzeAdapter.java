package com.deare.backend.global.external.gemini.adapter.analyze;

import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;

public interface AnalyzeAdapter {
    AnalyzeResponseDTO analyze(String content);
}
