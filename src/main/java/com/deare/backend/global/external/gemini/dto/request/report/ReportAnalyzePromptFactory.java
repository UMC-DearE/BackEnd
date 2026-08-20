package com.deare.backend.global.external.gemini.dto.request.report;

import com.deare.backend.global.external.gemini.dto.request.analyze.GeminiTextRequestDTO;

import java.util.List;

public class ReportAnalyzePromptFactory {
    private static final String TEMPLATE = """
            너는 편지를 읽고 수신자의 긍정적인 성향을 분석해주는 AI다.
            아래는 사용자가 받은 편지들의 한줄 요약 목록이다.

            [작성 규칙]
            1. 반드시 따뜻한 '-해요'체를 사용한다. 부정적인 표현은 절대 포함하지 않는다.
            2. description은 공백 포함 100자 이상 150자 이내로 작성한다.
            3. 편지 내용과 어울리는 해시태그를 정확히 2개 추출한다.
               - 각 해시태그는 단어 사이에 반드시 언더바(_)를 포함한다. (예: 다정한_마음)
            4. 반드시 아래 JSON 형식으로만 응답한다.

            [응답 형식]
            {
              "description": "분석 문장",
              "hashtag1": "단어_단어",
              "hashtag2": "단어_단어"
            }

            [편지 요약 목록]
            %s
            """;

    public static GeminiTextRequestDTO fromSummaries(String model, List<String> summaries) {
        String joined = String.join("\n", summaries);
        return GeminiTextRequestDTO.fromLetterText(model, TEMPLATE.formatted(joined));
    }
}
