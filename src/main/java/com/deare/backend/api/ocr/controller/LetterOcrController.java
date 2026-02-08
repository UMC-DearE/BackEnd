package com.deare.backend.api.ocr.controller;

import com.deare.backend.api.ocr.dto.request.OcrLettersRequestDTO;
import com.deare.backend.api.ocr.dto.response.OcrLettersResponseDTO;
import com.deare.backend.api.ocr.service.LetterOcrService;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@ConditionalOnProperty(name = "ocr.enabled", havingValue = "true")
@RequiredArgsConstructor
@RestController
@RequestMapping("/letters")
public class LetterOcrController {

    private final LetterOcrService letterOcrService;

    @PostMapping("/ocr")
    @Operation(
            summary = "편지 OCR",
            description = "업로드된 편지 이미지(최대 10장)를 OCR 처리하여 텍스트로 변환합니다."
    )
    public ApiResponse<OcrLettersResponseDTO> ocrLetters(
            @RequestBody @Valid OcrLettersRequestDTO request
    ) {
        return ApiResponse.success(letterOcrService.ocrLetters(request));
    }
}
