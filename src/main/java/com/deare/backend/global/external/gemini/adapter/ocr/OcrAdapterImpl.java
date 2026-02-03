package com.deare.backend.global.external.gemini.adapter.ocr;

import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.request.ocr.GeminiOcrRequestDTO;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

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
            // 네트워크 / 타임아웃 / DNS
            throw new ExternalApiException(ExternalApiErrorCode.AI_CONNECTION_FAILED);
        } catch (feign.FeignException e) {
            int status = e.status();

            if (status == 401 || status == 403) {
                // API 키 / 인증 문제 → 배포/설정 이슈
                throw new ExternalApiException(ExternalApiErrorCode.AI_UNAUTHORIZED);
            }
            if (status == 429) {
                // rate limit → 비용/쿼터 이슈
                throw new ExternalApiException(ExternalApiErrorCode.AI_RATE_LIMITED);
            }
            if (status >= 500) {
                // Gemini 서버 장애
                throw new ExternalApiException(ExternalApiErrorCode.AI_UPSTREAM_ERROR);
            }
            // 그 외 4xx 반환 → 요청 포맷/모델 문제
            throw new ExternalApiException(ExternalApiErrorCode.AI_BAD_REQUEST);
        }

    }
}
