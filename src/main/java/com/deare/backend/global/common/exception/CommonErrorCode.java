package com.deare.backend.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "COMMON_40001", "요청 값이 올바르지 않습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_40002", "요청 값의 타입이 올바르지 않습니다."),
    BODY_NOT_READABLE(HttpStatus.BAD_REQUEST, "COMMON_40003", "요청 본문(JSON)을 올바르게 작성해 주세요."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "COMMON_40004", "요청 값이 올바르지 않습니다."),
    REQUEST_BINDING_FAILED(HttpStatus.BAD_REQUEST, "COMMON_40005", "필수 요청 값이 누락되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_40401", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_40501", "지원하지 않는 HTTP 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_41501", "지원하지 않는 Content-Type입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_50001", "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
