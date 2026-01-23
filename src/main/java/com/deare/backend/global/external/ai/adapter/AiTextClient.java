package com.deare.backend.global.external.ai.adapter;

import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponseDTO;

public interface AiTextClient {
    AiAnalyzeResponseDTO analyze(String text);
}
