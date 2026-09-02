package com.deare.backend.global.external.gemini.adapter.report;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.request.report.ReportAnalyzePromptFactory;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deare.backend.global.external.feign.config.AiCallCounter;
import com.deare.backend.global.external.feign.config.AiCallLogTag;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportAnalyzedAdapterImpl implements ReportAnalyzedAdapter {

    private static final int DESCRIPTION_MIN_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 200;

    private final GeminiFeignClient feignClient;
    private final ObjectMapper om;
    private final AiCallCounter counter;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public ReportAnalyzeResponseDTO reportAnalyze(List<String> summaries) {
        AiCallCounter.CallCount count = counter.nextReport();
        long attemptSeq = count.attemptSeq();
        long total = count.total();
        MDC.put("aiCallType", "REPORT");
        MDC.put("aiAttemptSeq", String.valueOf(attemptSeq));
        MDC.put("aiTotal", String.valueOf(total));
        long start = System.currentTimeMillis();
        try {
            log.info("{} attemptSeq={} total={} START", AiCallLogTag.REPORT, attemptSeq, total);

            GeminiTextRequestDTO request =
                    ReportAnalyzePromptFactory.fromSummaries(model, summaries);

            GeminiTextResponseDTO response = feignClient.chatText(
                    "Bearer " + apiKey,
                    request
            );

            String rawJson = extractContent(response);

            ReportAnalyzeResponseDTO result = parse(rawJson);

            validateResult(result);

            log.info("{} attemptSeq={} total={} SUCCESS elapsed={}ms", AiCallLogTag.REPORT, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            return result;

        } catch (RetryableException e) {
            log.error("{} attemptSeq={} total={} FAIL reason=TIMEOUT status={} elapsed={}ms", AiCallLogTag.REPORT, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.status(), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT);

        } catch (FeignException e) {
            log.error("{} attemptSeq={} total={} FAIL reason=ERROR status={} elapsed={}ms", AiCallLogTag.REPORT, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.status(), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);

        } catch (ExternalApiException e) {
            log.error("{} attemptSeq={} total={} FAIL reason={} elapsed={}ms", AiCallLogTag.REPORT, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.getErrorCode().getCode(), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw e;

        } catch (Exception e) {
            log.error("{} attemptSeq={} total={} FAIL reason=ERROR elapsed={}ms", AiCallLogTag.REPORT, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start, e);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);
        } finally {
            MDC.remove("aiCallType");
            MDC.remove("aiAttemptSeq");
            MDC.remove("aiTotal");
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
                || description.length() < DESCRIPTION_MIN_LENGTH
                || description.length() > DESCRIPTION_MAX_LENGTH) {

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
