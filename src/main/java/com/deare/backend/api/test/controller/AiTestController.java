package com.deare.backend.api.test.controller;

import com.deare.backend.api.test.service.AiTestService;
import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequest;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class AiTestController {
    private final AiTestService aiTestService;

    @PostMapping("/ai/analyze")
    public ApiResponse<AiAnalyzeResponse> analyze(@RequestBody AiAnalyzeRequest request){
        log.info("AiTestController 진입");
        AiAnalyzeResponse dto = aiTestService.analyze(request.getText());
        log.info("Controller 리턴 직전");

        return ApiResponse.success(dto);
    }
}
