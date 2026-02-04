package com.deare.backend.global.external.feign.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalApiErrorCode implements BaseErrorCode {

    AI_CLIENT_ERROR(
            HttpStatus.BAD_REQUEST,
            "AI_40001",
            "AI 요청이 올바르지 않습니다."
    ),

    AI_RESPONSE_PARSE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_50001",
            "AI 응답 처리 중 오류가 발생했습니다."
    ),

    AI_SUMMARY_CREATE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AI_50002",
            "편지 요약 생성에 실패했습니다."
    ),

    AI_EMOTION_CREATE_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "AI_50003",
      "감정 분석에 실패했습니다."
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

    AI_RESPONSE_FORMAT_INVALID(
      HttpStatus.BAD_GATEWAY,
      "AI_50203",
      "AI 응답 형식이 올바르지 않습니다."
    ),

    AI_UNAUTHORIZED(
            HttpStatus.BAD_GATEWAY,
            "AI_50204",
            "AI 인증에 실패했습니다."
    ),

    AI_RATE_LIMITED(
            HttpStatus.BAD_GATEWAY,
            "AI_50205",
            "AI 요청 한도를 초과했습니다."
    ),

    AI_UPSTREAM_ERROR(
            HttpStatus.BAD_GATEWAY,
            "AI_50206",
            "AI 서버 오류가 발생했습니다."
    ),

    AI_BAD_REQUEST(
            HttpStatus.BAD_GATEWAY,
            "AI_50207",
            "AI 요청 형식이 올바르지 않습니다."
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
