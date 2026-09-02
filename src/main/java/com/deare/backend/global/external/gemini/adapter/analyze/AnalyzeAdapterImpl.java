package com.deare.backend.global.external.gemini.adapter.analyze;

import com.deare.backend.global.external.feign.config.AiCallCounter;
import com.deare.backend.global.external.feign.config.AiCallLogTag;
import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.analyze.AnalyzePromptFactory;
import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.analyze.AnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeAdapterImpl implements AnalyzeAdapter {

    private final GeminiFeignClient feignClient;
    private final ObjectMapper om;
    private final AiCallCounter counter;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public AnalyzeResponseDTO analyze(String content) {
        AiCallCounter.CallCount count = counter.nextAnalyze();
        long attemptSeq = count.attemptSeq();
        long total = count.total();
        MDC.put("aiCallType", "ANALYZE");
        MDC.put("aiAttemptSeq", String.valueOf(attemptSeq));
        MDC.put("aiTotal", String.valueOf(total));
        long start = System.currentTimeMillis();
        try {
            log.info("{} attemptSeq={} total={} START", AiCallLogTag.ANALYZE, attemptSeq, total);

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

            log.info("{} attemptSeq={} total={} SUCCESS elapsed={}ms", AiCallLogTag.ANALYZE, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            return result;
        } catch (feign.RetryableException e) {
            log.error("{} attemptSeq={} total={} FAIL reason=TIMEOUT status={} elapsed={}ms", AiCallLogTag.ANALYZE, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.status(), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT);
        } catch (ExternalApiException e) {
            log.error("{} attemptSeq={} total={} FAIL reason={} elapsed={}ms", AiCallLogTag.ANALYZE, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.getErrorCode().getCode(), System.currentTimeMillis() - start);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw e;
        } catch (Exception e) {
            log.error("{} attemptSeq={} total={} FAIL reason=ERROR elapsed={}ms", AiCallLogTag.ANALYZE, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start, e);
            log.info("[AI-CALL][SUMMARY] {}", counter.summary());
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);
        } finally {
            MDC.remove("aiCallType");
            MDC.remove("aiAttemptSeq");
            MDC.remove("aiTotal");
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
