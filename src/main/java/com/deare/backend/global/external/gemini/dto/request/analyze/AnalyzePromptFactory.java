package com.deare.backend.global.external.gemini.dto.request.analyze;

public class AnalyzePromptFactory {
    private static final String TEMPLATE= """
            너는 편지 감정 분석 AI다.
                아래 편지를 분석하여 다음을 수행하라.
            
                1. 편지를 한 문장으로 요약한다. (20~40자, 존댓말)
                2. 주요 감정을 아래 목록에서 3개에서 5개를 선택한다.
                3. 반드시 JSON 형식으로만 응답한다.
            
                [감정 태그 목록]
                사랑, 애정, 다정함, 소중함, 헌신, 신뢰, 친밀감, 포근함, 정서적유대,
                기쁨, 즐거움, 유쾌함, 신남, 활력, 기대감, 희열, 만족, 성취감,
                위로, 공감, 격려, 지지, 이해, 위안, 안심, 든든함, 회복감,
                그리움, 아련함, 향수, 회상, 차분함, 고요, 담담함, 평온, 여운,
                고민, 혼란, 불안, 걱정, 망설임, 답답함, 후회, 두려움, 외로움
            
                [응답 형식]
                {
                  "summary": "요약 문장",
                  "emotions": ["감정1", "감정2", "감정3"]
                }
            
                [편지 내용]
                %s
            """;

    public static GeminiTextRequestDTO fromLetter(String model, String content){
        return GeminiTextRequestDTO.fromLetterText(
                model,
                TEMPLATE.formatted(content)
        );
    }
}
