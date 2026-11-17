package com.deare.backend.global.external.gemini.adapter.report;

import com.deare.backend.global.external.feign.config.AiCallCounter;
import com.deare.backend.global.external.feign.exception.ExternalApiErrorCode;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import com.deare.backend.global.external.gemini.client.GeminiFeignClient;
import com.deare.backend.global.external.gemini.dto.response.GeminiTextResponseDTO;
import com.deare.backend.global.external.gemini.dto.response.report.ReportAnalyzeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportAnalyzedAdapterImplTest {

    private static final List<String> SUMMARIES = List.of("고마움을 전하는 편지", "응원을 보내는 편지");

    // 100자 이상 200자 이하를 만족하는 본문 (지칭어 뒤에 붙는다)
    private static final String BODY =
            " 주변 사람들에게 따뜻한 위로를 건네는 사람이에요. ".repeat(4);

    private GeminiFeignClient feignClient;
    private ReportAnalyzedAdapterImpl adapter;

    @BeforeEach
    void setUp() {
        feignClient = mock(GeminiFeignClient.class);
        adapter = new ReportAnalyzedAdapterImpl(feignClient, new ObjectMapper(), new AiCallCounter());
        ReflectionTestUtils.setField(adapter, "apiKey", "test-key");
        ReflectionTestUtils.setField(adapter, "model", "test-model");
    }

    @Test
    @DisplayName("정상 응답이면 1회 호출로 결과를 반환한다")
    void success_onFirstAttempt() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(validJson()));

        ReportAnalyzeResponseDTO result = adapter.reportAnalyze(SUMMARIES);

        assertThat(result.getDescription()).startsWith("당신은");
        assertThat(result.getHashtag1()).isEqualTo("다정한_마음");
        assertThat(result.getHashtag2()).isEqualTo("따뜻한_위로");
        verify(feignClient, times(1)).chatText(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"당신은", "당신이", "당신에게"})
    @DisplayName("description이 '당신'으로 시작하면 조사와 관계없이 검증을 통과한다")
    void prefix_allowsAnyParticleAfterDangsin(String prefix) {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json(prefix + BODY, "다정한_마음", "따뜻한_위로")));

        ReportAnalyzeResponseDTO result = adapter.reportAnalyze(SUMMARIES);

        assertThat(result.getDescription()).startsWith(prefix);
        verify(feignClient, times(1)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("지칭어 검증에 실패한 뒤 재시도에서 통과하면 결과를 반환한다")
    void retry_thenSuccess() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json("사용자는" + BODY, "다정한_마음", "따뜻한_위로")))
                .thenReturn(response(validJson()));

        ReportAnalyzeResponseDTO result = adapter.reportAnalyze(SUMMARIES);

        assertThat(result.getDescription()).startsWith("당신은");
        verify(feignClient, times(2)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("최초 시도 포함 3회 모두 지칭어 검증에 실패하면 AI_PERSONA_PREFIX_INVALID 예외가 발생한다")
    void retry_exhausted_prefixInvalid() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json("사용자는" + BODY, "다정한_마음", "따뜻한_위로")));

        assertThatThrownBy(() -> adapter.reportAnalyze(SUMMARIES))
                .isInstanceOf(ExternalApiException.class)
                .extracting(e -> ((ExternalApiException) e).getErrorCode())
                .isEqualTo(ExternalApiErrorCode.AI_PERSONA_PREFIX_INVALID);

        verify(feignClient, times(3)).chatText(anyString(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"수신자", "사용자"})
    @DisplayName("'당신'으로 시작해도 이후 문장에 다른 지칭어가 있으면 재시도한다")
    void retry_onForbiddenReferenceTermInBody(String term) {
        String description = "당신은 주변 사람들에게 따뜻한 위로를 건네는 사람이에요. "
                + term + "는 많은 사람에게 사랑받고 있어요." + BODY;
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json(description, "다정한_마음", "따뜻한_위로")))
                .thenReturn(response(validJson()));

        ReportAnalyzeResponseDTO result = adapter.reportAnalyze(SUMMARIES);

        assertThat(result.getDescription()).doesNotContain(term);
        verify(feignClient, times(2)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("3회 모두 다른 지칭어가 포함되면 AI_PERSONA_PREFIX_INVALID 예외가 발생한다")
    void retry_exhausted_forbiddenReferenceTerm() {
        String description = "당신은 따뜻한 사람이에요. 수신자는 사랑받고 있어요." + BODY;
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json(description, "다정한_마음", "따뜻한_위로")));

        assertThatThrownBy(() -> adapter.reportAnalyze(SUMMARIES))
                .isInstanceOf(ExternalApiException.class)
                .extracting(e -> ((ExternalApiException) e).getErrorCode())
                .isEqualTo(ExternalApiErrorCode.AI_PERSONA_PREFIX_INVALID);

        verify(feignClient, times(3)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("'너무', '그대로'처럼 일반 단어에 포함된 글자는 지칭어로 판단하지 않는다")
    void forbiddenReferenceTerm_noFalsePositive() {
        String description = "당신은 너무 따뜻한 사람이에요. 그 마음을 그대로 전해주세요." + BODY;
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json(description, "다정한_마음", "따뜻한_위로")));

        adapter.reportAnalyze(SUMMARIES);

        verify(feignClient, times(1)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("3회 모두 실패하면 마지막 시도의 실패 사유로 예외가 발생한다")
    void retry_exhausted_throwsLastError() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json("사용자는" + BODY, "다정한_마음", "따뜻한_위로")))   // 지칭어 실패
                .thenReturn(response(json("당신은 짧아요.", "다정한_마음", "따뜻한_위로")))      // 길이 실패
                .thenReturn(response(json("당신은" + BODY, "다정한마음", "따뜻한_위로")));      // 해시태그 실패

        assertThatThrownBy(() -> adapter.reportAnalyze(SUMMARIES))
                .isInstanceOf(ExternalApiException.class)
                .extracting(e -> ((ExternalApiException) e).getErrorCode())
                .isEqualTo(ExternalApiErrorCode.AI_PERSONA_HASHTAG_INVALID);

        verify(feignClient, times(3)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("길이 검증 실패도 재시도 대상이다")
    void retry_onLengthInvalid() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json("당신은 짧아요.", "다정한_마음", "따뜻한_위로")))
                .thenReturn(response(validJson()));

        adapter.reportAnalyze(SUMMARIES);

        verify(feignClient, times(2)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("해시태그 검증 실패도 재시도 대상이다")
    void retry_onHashtagInvalid() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response(json("당신은" + BODY, "다정한마음", "따뜻한_위로")))
                .thenReturn(response(validJson()));

        adapter.reportAnalyze(SUMMARIES);

        verify(feignClient, times(2)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("JSON 형식이 아닌 응답도 재시도 대상이다")
    void retry_onFormatInvalid() {
        when(feignClient.chatText(anyString(), any()))
                .thenReturn(response("JSON이 아닌 응답"))
                .thenReturn(response(validJson()));

        adapter.reportAnalyze(SUMMARIES);

        verify(feignClient, times(2)).chatText(anyString(), any());
    }

    @Test
    @DisplayName("응답 검증과 무관한 오류는 재시도하지 않고 즉시 실패한다")
    void noRetry_onNonResponseError() {
        when(feignClient.chatText(anyString(), any()))
                .thenThrow(new ExternalApiException(ExternalApiErrorCode.AI_RATE_LIMITED));

        assertThatThrownBy(() -> adapter.reportAnalyze(SUMMARIES))
                .isInstanceOf(ExternalApiException.class)
                .extracting(e -> ((ExternalApiException) e).getErrorCode())
                .isEqualTo(ExternalApiErrorCode.AI_RATE_LIMITED);

        verify(feignClient, times(1)).chatText(anyString(), any());
    }

    private static String validJson() {
        return json("당신은" + BODY, "다정한_마음", "따뜻한_위로");
    }

    private static String json(String description, String hashtag1, String hashtag2) {
        return """
                {"description": "%s", "hashtag1": "%s", "hashtag2": "%s"}
                """.formatted(description, hashtag1, hashtag2);
    }

    private static GeminiTextResponseDTO response(String content) {
        return new GeminiTextResponseDTO(List.of(
                new GeminiTextResponseDTO.Choice(
                        new GeminiTextResponseDTO.Message("assistant", content)
                )
        ));
    }
}
