package com.deare.backend.global.external.gemini.adapter.test;

import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.request.ocr.GeminiOcrRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeminiTestClientAdapter implements GeminiTestClient {

    private final GeminiFeignClient geminiFeignClient;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public String geminiTest(String text) {
        String prompt = """
                다음 글을 한 문장으로 간단하게 요약해줘.
                요약문만 음/슴체로 보여줘.
                
                글: %s
                """.formatted(text);

        GeminiTextRequestDTO request = GeminiTextRequestDTO.fromLetterText(model,prompt);

        GeminiTextResponseDTO response = geminiFeignClient.chatText(
                "Bearer " + apiKey,
                request);

        return response.getChoices().get(0).getMessage().getContent();
    }

    @Override
    public String geminiOcrTest(String instruction, List<String> base64Images) {
        GeminiOcrRequestDTO request = GeminiOcrRequestDTO.fromImages(model, instruction, base64Images);

        GeminiTextResponseDTO response = geminiFeignClient.chatOcr(
                "Bearer " + apiKey,
                request
        );

        return response.getChoices().get(0).getMessage().getContent();
    }
}
