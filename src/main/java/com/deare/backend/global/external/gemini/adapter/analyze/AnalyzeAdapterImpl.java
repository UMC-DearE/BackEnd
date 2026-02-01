package com.deare.backend.global.external.gemini.adapter.analyze;

import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.AnalyzePromptFactory;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyzeAdapterImpl implements AnalyzeAdapter {

    private final GeminiFeignClient feignClient;
    private final ObjectMapper om;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public AnalyzeResponseDTO analyze(String content) {
        GeminiTextRequestDTO request = AnalyzePromptFactory.fromLetter(model, content);

        GeminiTextResponseDTO response=feignClient.chatText(
                "Bearer "+apiKey,
                request
        );

        String rawJson=response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        return parse(rawJson);
    }

    private AnalyzeResponseDTO parse(String json) {
        try{
            return om.readValue(json, AnalyzeResponseDTO.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Gemini analyze parsing failed. "+ json, e
            );
        }
    }
}
