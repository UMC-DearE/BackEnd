package com.deare.backend.global.common.exception;

import org.springframework.http.HttpStatus;

public enum S3ErrorCode implements BaseErrorCode {

    // 400
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "S3_400_1", "파일이 비어 있습니다."),
    EMPTY_KEY(HttpStatus.BAD_REQUEST, "S3_400_2", "key가 비어 있습니다."),

    // 500
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_1", "파일 처리 중 오류가 발생했습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_2", "S3 업로드에 실패했습니다."),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_3", "S3 삭제에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    S3ErrorCode(HttpStatus status, String code, String message) {
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
