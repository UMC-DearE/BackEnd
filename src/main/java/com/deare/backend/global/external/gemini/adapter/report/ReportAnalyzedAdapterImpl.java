package com.deare.backend.global.external.gemini.adapter.report;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.request.report.ReportAnalyzePromptFactory;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RetryableException;
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
            GeminiTextRequestDTO request =
                    ReportAnalyzePromptFactory.fromSummaries(model, summaries);

            GeminiTextResponseDTO response = feignClient.chatText(
                    "Bearer " + apiKey,
                    request
            );

            String rawJson = extractContent(response);

            ReportAnalyzeResponseDTO result = parse(rawJson);

            validateResult(result);

            return result;

        } catch (RetryableException e) {
            log.warn(
                    "[ReportAnalyze] AI 호출 타임아웃/네트워크 오류 - status={}, message={}",
                    e.status(),
                    e.getMessage()
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_TIMEOUT
            );

        } catch (FeignException e) {
            log.error(
                    "[ReportAnalyze] AI API 오류 - status={}, message={}",
                    e.status(),
                    e.getMessage()
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_REQUEST_FAILED
            );

        } catch (ExternalApiException e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "[ReportAnalyze] 예상하지 못한 오류",
                    e
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_REQUEST_FAILED
            );
        }
    }

    private String extractContent(GeminiTextResponseDTO response) {
        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()
                || response.getChoices().get(0) == null
                || response.getChoices().get(0).getMessage() == null) {

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        String content = response.getChoices()
                .get(0)
                .getMessage()
                .getContent();

        if (content == null || content.isBlank()) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        return content;
    }

    private ReportAnalyzeResponseDTO parse(String raw) {
        try {
            String json = extractJson(raw);

            return om.readValue(
                    json,
                    ReportAnalyzeResponseDTO.class
            );

        } catch (ExternalApiException e) {
            throw e;

        } catch (Exception e) {
            log.warn(
                    "[ReportAnalyze] AI 응답 JSON 파싱 실패 - raw={}",
                    raw
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_PARSE_ERROR
            );
        }
    }

    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start == -1 || end == -1 || start > end) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        return raw.substring(start, end + 1);
    }

    private void validateResult(ReportAnalyzeResponseDTO result) {
        if (result == null) {
            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID
            );
        }

        String description = result.getDescription();

        if (description == null
                || description.length() < 130
                || description.length() > 170) {

            log.warn(
                    "[ReportAnalyze] description 길이 검증 실패 - length={}, description={}",
                    description == null ? null : description.length(),
                    description
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_PERSONA_LENGTH_INVALID
            );
        }

        if (!isValidHashtag(result.getHashtag1())
                || !isValidHashtag(result.getHashtag2())) {

            log.warn(
                    "[ReportAnalyze] 해시태그 검증 실패 - hashtag1={}, hashtag2={}",
                    result.getHashtag1(),
                    result.getHashtag2()
            );

            throw new ExternalApiException(
                    ExternalApiErrorCode.AI_PERSONA_HASHTAG_INVALID
            );
        }
    }

    private boolean isValidHashtag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }

        String[] words = tag.split("_");

        return words.length == 2
                && !words[0].isBlank()
                && !words[1].isBlank();
    }
}
