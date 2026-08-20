package com.deare.backend.global.external.gemini.adapter.report;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.request.report.ReportAnalyzePromptFactory;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportAnalyzedAdapterImpl implements ReportAnalyzedAdapter {
    private final GeminiFeignClient feignClient;
    private final ObjectMapper om;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public ReportAnalyzeResponseDTO reportAnalyze(List<String> summaries) {
        try {
            GeminiTextRequestDTO request = ReportAnalyzePromptFactory.fromSummaries(model, summaries);

            GeminiTextResponseDTO response = feignClient.chatText(
                    "Bearer " + apiKey,
                    request
            );

            String rawJson = response.getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            ReportAnalyzeResponseDTO result = parse(rawJson);
            validateResult(result);

            return result;
        } catch (feign.RetryableException e) {
            log.warn("[PersonaAnalyze] AI 호출 실패 (재시도 소진 또는 네트워크 장애) - status={}, message={}",
                    e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);
        }
    }

    private ReportAnalyzeResponseDTO parse(String raw) {
        try {
            String json = extractJson(raw);
            return om.readValue(json, ReportAnalyzeResponseDTO.class);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
    }

    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID);
        }

        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1 || start > end) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID);
        }

        return raw.substring(start, end + 1);
    }

    private void validateResult(ReportAnalyzeResponseDTO result) {
        String description = result.getDescription();
        if (description == null || description.length() < 97 || description.length() > 153) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_PERSONA_LENGTH_INVALID);
        }
        if (!isValidHashtag(result.getHashtag1()) || !isValidHashtag(result.getHashtag2())) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_PERSONA_HASHTAG_INVALID);
        }
    }

    private boolean isValidHashtag(String tag) {
        return tag != null && !tag.isBlank() && tag.contains("_");
    }
}
