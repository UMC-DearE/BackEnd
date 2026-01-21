package com.deare.backend.global.external.ai.adapter;

import com.deare.backend.global.external.ai.client.AiTextFeignClient;
import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequest;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiTextClientAdapter implements AiTextClient {

    private final AiTextFeignClient feignClient;

    @Override
    public AiAnalyzeResponse analyze(String text) {
        AiAnalyzeRequest request = new AiAnalyzeRequest(text);
        return feignClient.analyze(request);
    }
}
