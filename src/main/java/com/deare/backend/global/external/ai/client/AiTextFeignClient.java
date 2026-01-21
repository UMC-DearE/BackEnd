package com.deare.backend.global.external.ai.client;

import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequest;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponse;
import com.deare.backend.global.external.feign.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name="aiTextClient",
        url="${external.ai.base-url}",
        configuration = FeignConfig.class
)
public interface AiTextFeignClient {
    @PostMapping(
            value="/ai/analyze",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    AiAnalyzeResponse analyze(@RequestBody AiAnalyzeRequest request);
}
