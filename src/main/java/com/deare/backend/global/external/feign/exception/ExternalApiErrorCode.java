package com.deare.backend.global.external.feign.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalApiErrorCode implements BaseErrorCode {

    AI_RESPONSE_PARSE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_50001",
            "AI 응답 처리 중 오류가 발생했습니다."
    ),

    AI_SUMMARY_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_50002",
            "편지 요약 생성에 실패했습니다."
    ),

    AI_REQUEST_FAILED(
            HttpStatus.BAD_GATEWAY,
            "AI_50201",
            "AI 분석 요청에 실패했습니다."
    ),

    AI_CONNECTION_FAILED(
            HttpStatus.BAD_GATEWAY,
            "AI_50202",
            "AI 서버에 연결할 수 없습니다."
    ),

    AI_TIMEOUT(
            HttpStatus.GATEWAY_TIMEOUT,
            "AI_50401",
            "AI 응답 시간이 초과되었습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;
}
