package com.deare.backend.domain.letter.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum LetterErrorCode implements BaseErrorCode {

    LETTER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "LETTER_40401",
            "편지를 찾을 수 없습니다."
    ),

    LETTER_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "LETTER_40301",
            "해당 편지에 접근할 권한이 없습니다."
    ),

    LETTER_INVALID_EXCERPT_PARAM(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "LETTER_50002",
            "편지 요약 처리 중 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    LetterErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
