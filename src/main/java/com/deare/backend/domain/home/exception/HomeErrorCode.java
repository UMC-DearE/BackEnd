package com.deare.backend.domain.home.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum HomeErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "HOME_40401",
            "사용자를 찾을 수 없습니다."
    ),

    HOME_INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "HOME_50001",
            "홈 화면 조회 중 서버 오류가 발생하였습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    HomeErrorCode(HttpStatus status, String code, String message) {
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
