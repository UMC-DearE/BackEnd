package com.deare.backend.api.test.controller;

import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.external.gemini.adapter.test.GeminiTestClientAdapter;
import com.deare.backend.global.external.gemini.dto.request.test.GeminiTestRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class AiTestController {
    private final GeminiTestClientAdapter  geminiTestClientAdapter;

    @PostMapping("/gemini")
    public ApiResponse<String> gemini(@RequestBody GeminiTestRequestDTO text) {
        return ApiResponse.success(geminiTestClientAdapter.geminiTest(text.getText()));
    }
}
