package com.deare.backend.api.report.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ReportErrorCode implements BaseErrorCode {

    REPORT_NOT_FOUND_USER(HttpStatus.NOT_FOUND, "REPORT_40401", "존재하지 않는 사용자입니다."),
    REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "REPORT_40301", "해당 계정은 비활성화된 계정입니다."),
    REPORT_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"REPORT_50001","리포트 통계 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ReportErrorCode(HttpStatus status, String code, String message) {
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
