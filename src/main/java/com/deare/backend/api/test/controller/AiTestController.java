package com.deare.backend.api.test.controller;

import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.external.gemini.adapter.test.GeminiTestClientAdapter;
import com.deare.backend.global.external.gemini.dto.request.test.GeminiTestRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

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

    @PostMapping("/ocr/resource")
    public OcrResponse ocrTestResource() throws IOException {
        String instruction = "이 편지에서 손글씨만 ocr로 추출해서 보여줘";
        String base64Image = loadTestImageAsBase64();

        String result = geminiTestClientAdapter.geminiOcrTest(instruction, List.of(base64Image));

        return new OcrResponse(result);
    }

    private String loadTestImageAsBase64() throws IOException {
        ClassPathResource imgResource = new ClassPathResource("image/testImage.png");
        try (InputStream is = imgResource.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(is);
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrResponse {
        private String text;
    }

}
