package com.deare.backend.global.external.gemini.client;

import com.deare.backend.global.external.feign.config.FeignConfig;
import com.deare.backend.global.external.feign.config.FeignDevConfig;
import com.deare.backend.global.external.feign.config.FeignProdConfig;
import com.deare.backend.global.external.gemini.dto.request.GeminiRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "geminiFeignClient",
        url = "${external.ai.base-url}",
        configuration = {
                FeignConfig.class,
                FeignDevConfig.class,
                FeignProdConfig.class
        }
)
public interface GeminiFeignClient {

    @PostMapping(
            value = "/chat/completions",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    GeminiResponseDTO chat(
            @RequestHeader("Authorization") String authorization,
            @RequestBody GeminiRequestDTO request
    );
}
