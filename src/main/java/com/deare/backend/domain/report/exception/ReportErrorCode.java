package com.deare.backend.domain.report.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ReportErrorCode implements BaseErrorCode {

    REPORT_NOT_FOUND_USER(HttpStatus.NOT_FOUND, "REPORT_40401", "존재하지 않는 사용자입니다."),
    REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "REPORT_40301", "해당 계정은 비활성화된 계정입니다."),
    REPORT_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"REPORT_50001","리포트 통계 처리 중 오류가 발생했습니다."),
    REPORT_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_50002", "AI 분석 결과를 조회할 수 없습니다."),

    REPORT_ANALYSIS_NOT_ENOUGH_LETTERS(HttpStatus.BAD_REQUEST, "REPORT_40001", "분석하려면 편지 3통이 필요해요."),
    REPORT_ANALYSIS_NOT_ENOUGH_NEW_LETTERS(HttpStatus.BAD_REQUEST, "REPORT_40002", "새로운 편지가 3통 이상 쌓여야 다시 분석할 수 있어요."),
    REPORT_ANALYSIS_WEEKLY_LIMIT(HttpStatus.BAD_REQUEST, "REPORT_40003", "AI 분석은 일주일에 한 번만 할 수 있어요."),
    REPORT_ANALYSIS_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_50003", "AI 분석 중 오류가 발생했습니다."),
    REPORT_ANALYSIS_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT_50004", "AI 분석 결과 저장 중 오류가 발생했습니다.");

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
