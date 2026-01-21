package com.deare.backend.api.test.service;

import com.deare.backend.global.external.ai.client.AiTextFeignClient;
import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequest;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTestService {
    private final AiTextFeignClient aiTextFeignClient;

    @PostConstruct
    public void init() {
        log.info("🔥 aiTextFeignClient = {}", aiTextFeignClient);
    }

    public AiAnalyzeResponse analyze(String text) {
        log.info("Feign 호출 전");
        AiAnalyzeRequest request=new AiAnalyzeRequest(text);
        log.info("Feign 호출 후");

        return aiTextFeignClient.analyze(request);
    }
}
