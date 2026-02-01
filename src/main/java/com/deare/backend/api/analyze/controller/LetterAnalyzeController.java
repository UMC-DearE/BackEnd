package com.deare.backend.api.analyze.controller;

import com.deare.backend.api.analyze.dto.request.AnalyzeLetterRequestDTO;
import com.deare.backend.api.analyze.dto.response.AnalyzeLetterResponseDTO;
import com.deare.backend.api.analyze.service.LetterAnalyzeService;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/letters")
public class LetterAnalyzeController {
    private final LetterAnalyzeService letterAnalyzeService;

    @PostMapping("/analyze")
    @Operation(
            summary = "편지 요약 및 감정 태그 분석",
            description = "편지글을 받아 ai를 통해 편지글 한줄 요약 및 감정 태그를 추출하여 반환하는 API"
    )
    public ApiResponse<AnalyzeLetterResponseDTO> analyze(
            @RequestBody @Valid AnalyzeLetterRequestDTO request
    ){
        return ApiResponse.success(letterAnalyzeService.analyze(request));
    }
}
