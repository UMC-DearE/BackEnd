package com.deare.backend.api.test.controller;

import com.deare.backend.api.test.service.AiTestService;
import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.external.ai.dto.request.AiAnalyzeRequestDTO;
import com.deare.backend.global.external.ai.dto.response.AiAnalyzeResponseDTO;
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
    private final AiTestService aiTestService;

    @PostMapping("/ai/analyze")
    public ApiResponse<AiAnalyzeResponseDTO> analyze(@RequestBody AiAnalyzeRequestDTO request){
        log.info("AiTestController 진입");
        AiAnalyzeResponseDTO dto = aiTestService.analyze(request.getText());
        log.info("Controller 리턴 직전");

        return ApiResponse.success(dto);
    }
}
