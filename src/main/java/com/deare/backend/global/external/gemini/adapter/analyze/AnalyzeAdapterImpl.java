package com.deare.backend.global.external.gemini.adapter.analyze;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
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
        try {
            GeminiTextRequestDTO request = AnalyzePromptFactory.fromLetter(model, content);

            GeminiTextResponseDTO response = feignClient.chatText(
                    "Bearer " + apiKey,
                    request
            );

            String rawJson = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            AnalyzeResponseDTO result = parse(rawJson);

            validateResult(result);

            return result;
        }catch(feign.RetryableException e){
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_CONNECTION_FAILED
            );
        }
    }

    private AnalyzeResponseDTO parse(String raw) {
        try{
            String json=extractJson(raw);
            return om.readValue(json, AnalyzeResponseDTO.class);
        } catch(ExternalApiException e){
            throw e;
        }
        catch (Exception e) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_PARSE_ERROR
            );
        }
    }

    private String extractJson(String raw) {

        if(raw==null||raw.isBlank()){
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        int start=raw.indexOf("{");
        int end=raw.lastIndexOf("}");

        if(start==-1 || end==-1||start>end){
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        return raw.substring(start, end+1);
    }

    private void validateResult(AnalyzeResponseDTO result) {

        if (result.getSummary() == null || result.getSummary().isBlank()) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_SUMMARY_CREATE_FAILED
            );
        }

        if (result.getEmotions() == null || result.getEmotions().isEmpty()) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_EMOTION_CREATE_FAILED
            );
        }
    }

}
