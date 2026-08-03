package com.deare.backend.global.external.gemini.adapter.ocr;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.ocr.GeminiOcrRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrAdapterImpl implements OcrAdapter {

    private final GeminiFeignClient feignClient;

    @Value("${external.ai.api-key}")
    private String apiKey;

    @Value("${external.ai.model}")
    private String model;

    @Override
    public String ocr(String instruction, String base64Image) {
        try {
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

            return response.getChoices().get(0).getMessage().getContent();

        } catch (feign.RetryableException e) {
            // 네트워크 / 타임아웃 / DNS, 또는 5xx 재시도 소진
            log.warn("[OCR] AI 호출 실패 (재시도 소진 또는 네트워크 장애) - status={}, message={}",
                    e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.AI_TIMEOUT);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalApiException(ExternalApiErrorCode.AI_REQUEST_FAILED);
        }

    }
}
