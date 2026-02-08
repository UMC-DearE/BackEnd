package com.deare.backend.domain.emotion.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum EmotionErrorCode implements BaseErrorCode {
    INVALID_AI_RESPONSE(
            HttpStatus.BAD_REQUEST,
            "ANALYZE_40001",
            "AI 응답이 서비스 정책을 벗어났습니다."
    ),
    EMOTION_NOT_EXIST(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "ANALYZE_50001",
            "AI가 반환한 시스템에 존재하지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
