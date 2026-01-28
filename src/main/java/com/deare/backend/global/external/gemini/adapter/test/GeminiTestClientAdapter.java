package com.deare.backend.global.external.gemini.adapter.test;

import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiTestClientAdapter implements GeminiTestClient {

    private final GeminiFeignClient geminiFeignClient;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Override
    public String geminiTest(String text) {
        String prompt = """
                다음 글을 한 문장으로 간단하게 요약해줘.
                요약문만 음/슴체로 보여줘.
                
                글: %s
                """.formatted(text);

        GeminiTextRequestDTO request = GeminiTextRequestDTO.fromLetterText(prompt);

        GeminiTextResponseDTO response = geminiFeignClient.chat(
                "Bearer " + apiKey,
                request);

        return response.getChoices().get(0).getMessage().getContent();
    }
}
