package com.deare.backend.global.external.ai.adapter;

import com.deare.backend.global.external.ai.client.AiTextFeignClient;
import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequestDTO;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiTextClientAdapter implements AiTextClient {

    private final AiTextFeignClient feignClient;

    @Override
    public AiAnalyzeResponseDTO analyze(String text) {
        AiAnalyzeRequestDTO request = new AiAnalyzeRequestDTO(text);
        return feignClient.analyze(request);
    }
}
