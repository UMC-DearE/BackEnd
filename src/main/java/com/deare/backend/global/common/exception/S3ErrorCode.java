package com.deare.backend.global.common.exception;

import org.springframework.http.HttpStatus;

public enum S3ErrorCode implements BaseErrorCode {

    // 400
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "S3_40001", "파일이 비어 있습니다."),
    EMPTY_KEY(HttpStatus.BAD_REQUEST, "S3_40002", "key가 비어 있습니다."),

    // 404
    NOT_FOUND(HttpStatus.NOT_FOUND, "S3_40401", "S3에 해당 파일이 존재하지 않습니다."),

    // 500
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_50001", "파일 처리 중 오류가 발생했습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_50002", "S3 업로드에 실패했습니다."),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_50003", "S3 삭제에 실패했습니다."),
    PRESIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_50004", "Presigned URL 생성에 실패했습니다."),
    DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_50005", "S3 파일 다운로드에 실패했습니다.");

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
