package com.deare.backend.api.test.service;

import com.deare.backend.global.external.ai.adapter.AiTextClientAdapter;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponseDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTestService {
    private final AiTextClientAdapter aiTextClientAdapter;

    @PostConstruct
    public void init() {
        log.info("🔥 aiTextClientAdapter = {}", aiTextClientAdapter);
    }

    public AiAnalyzeResponseDTO analyze(String text) {
        log.info("AI 분석 요청 시작");
        return aiTextClientAdapter.analyze(text);
    }
}
