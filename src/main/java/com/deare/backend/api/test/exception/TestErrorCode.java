package com.deare.backend.api.test.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TestErrorCode implements BaseErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "TEST_40101",
            "인증이 필요합니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,
            "TEST_40102",
            "비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    TestErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
