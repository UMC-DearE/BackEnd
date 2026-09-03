package com.deare.backend.global.external.gemini.limit;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiUsageErrorCode implements BaseErrorCode {

    DAILY_LIMIT_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "ANALYZE_40002",
            "일일 편지 분석 한도를 초과했습니다. 내일 다시 시도해주세요."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
