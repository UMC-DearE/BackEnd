package com.deare.backend.global.external.gemini.dto.request.ocr;

public class OcrPromptFactory {

    private static final String TEMPLATE = """
            당신은 한국어 손글씨 편지를 텍스트로 변환하는 OCR 시스템입니다.

            이미지 속 손글씨를 분석하여 다음 기준으로 변환하세요:
            - 줄 단위로 텍스트를 분리
            - 문단 간 공백은 빈 줄로 유지
            - 목록 형태(번호, 점 등)는 그대로 유지

            문자 인식 규칙:
            - 한국어 손글씨에서 자주 혼동되는 글자(예: ㄷ/ㄹ, ㅁ/ㅇ 등)는 문맥상 명백한 경우에 한해 올바른 글자로 보정할 수 있습니다.
            - 문맥으로도 판단이 불가능한 경우에는 원본 형태를 유지하거나 [판독불가]로 표시합니다.
            - 단어·문장의 의미를 새로 만들어내거나 추측하여 수정하지 마세요.
            - 의미 보정, 문장 개선, 맞춤법 교정은 하지 마세요.

            결과는 원본 구조를 최대한 유지한 텍스트로만 출력하세요.
            """;

    public static String instruction() {
        return TEMPLATE;
    }
}
