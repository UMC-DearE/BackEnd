package com.deare.backend.domain.image.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {

    IMAGE_40001(HttpStatus.BAD_REQUEST, "IMAGE_40001", "잘못된 요청입니다."),
    IMAGE_40101(HttpStatus.UNAUTHORIZED, "IMAGE_40101", "로그인이 필요한 요청입니다."),
    IMAGE_40401(HttpStatus.NOT_FOUND, "IMAGE_40401", "이미지를 찾을 수 없습니다."),
    IMAGE_41301(HttpStatus.PAYLOAD_TOO_LARGE, "IMAGE_41301", "업로드 가능한 용량을 초과했습니다."),
    IMAGE_50001(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_50001", "이미지 업로드 중 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

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
