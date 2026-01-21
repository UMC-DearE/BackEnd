package com.deare.backend.global.external.ai.adapter;

import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponse;

public interface AiTextClient {
    AiAnalyzeResponse analyze(String text);
}
