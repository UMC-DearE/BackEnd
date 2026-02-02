package com.deare.backend.api.auth.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BaseErrorCode {

    // 400 Bad Request
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_40001", "지원하지 않는 OAuth Provider입니다."),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "AUTH_40002", "유효하지 않거나 만료된 State입니다."),
    MISSING_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_40003", "Signup Token이 없습니다."),
    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_40004", "Refresh Token이 없습니다."),

    // 401 Unauthorized
    INVALID_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_40101", "유효하지 않은 Signup Token입니다."),
    EXPIRED_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_40102", "Signup Token이 만료되었거나 이미 사용되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_40103", "유효하지 않은 Refresh Token입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_40105", "Refresh Token이 일치하지 않습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_40401", "사용자를 찾을 수 없습니다."),

    // 409 Conflict
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_40901", "이미 가입된 사용자입니다."),

    // 502 Bad Gateway (외부 OAuth 서버 오류)
    OAUTH_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_50201", "OAuth 토큰 요청에 실패했습니다."),
    OAUTH_USERINFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AUTH_50202", "OAuth 사용자 정보 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus status, String code, String message) {
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
