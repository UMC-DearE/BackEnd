package com.deare.backend.global.external.gemini.adapter.ocr;

import com.deare.backend.global.external.feign.config.AiCallCounter;
import com.deare.backend.global.external.feign.config.AiCallLogTag;
import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.ocr.GeminiOcrRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrAdapterImpl implements OcrAdapter {

    private final GeminiFeignClient feignClient;
    private final AiCallCounter counter;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public String ocr(String instruction, String base64Image) {
        AiCallCounter.CallCount count = counter.nextOcr();
        long attemptSeq = count.attemptSeq();
        long total = count.total();
        MDC.put("aiCallType", "OCR");
        MDC.put("aiAttemptSeq", String.valueOf(attemptSeq));
        MDC.put("aiTotal", String.valueOf(total));
        long start = System.currentTimeMillis();
        try {
            log.info("{} attemptSeq={} total={} START", AiCallLogTag.OCR, attemptSeq, total);

            GeminiOcrRequestDTO request = GeminiOcrRequestDTO.fromImages(
                    model,
                    instruction,
                    List.of(base64Image)
            );

            GeminiTextResponseDTO response = feignClient.chatOcr(
                    "Bearer " + apiKey,
                    request
            );

            if (response.getChoices() == null || response.getChoices().isEmpty()
                    || response.getChoices().get(0).getMessage() == null
                    || response.getChoices().get(0).getMessage().getContent() == null) {
                throw new ExternalApiException(ExternalApiErrorCode.AI_RESPONSE_FORMAT_INVALID);
            }

            log.info("{} attemptSeq={} total={} SUCCESS elapsed={}ms", AiCallLogTag.OCR, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start);
            return response.getChoices().get(0).getMessage().getContent();

        } catch (feign.RetryableException e) {
            log.error("{} attemptSeq={} total={} FAIL reason=TIMEOUT status={} elapsed={}ms", AiCallLogTag.OCR, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.status(), System.currentTimeMillis() - start);
            throw new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT);
        } catch (ExternalApiException e) {
            log.error("{} attemptSeq={} total={} FAIL reason={} elapsed={}ms", AiCallLogTag.OCR, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), e.getErrorCode().getCode(), System.currentTimeMillis() - start);
            throw e;
        } catch (Exception e) {
            log.error("{} attemptSeq={} total={} FAIL reason=ERROR elapsed={}ms", AiCallLogTag.OCR, MDC.get("aiAttemptSeq"), MDC.get("aiTotal"), System.currentTimeMillis() - start, e);
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);
        } finally {
            MDC.remove("aiCallType");
            MDC.remove("aiAttemptSeq");
            MDC.remove("aiTotal");
        }
    }
}
