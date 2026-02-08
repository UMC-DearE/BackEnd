package com.deare.backend.domain.letter.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LetterErrorCode implements BaseErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "LETTER_40001",
            "잘못된 요청입니다."
    ),
    FROM_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "LETTER_40002",
            "편지의 From은 필수입니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "LETTER_40101",
            "로그인이 필요한 요청입니다."
    ),

    DELETED_LETTER(
            HttpStatus.GONE,
            "LETTER_41001",
            "삭제된 편지입니다."
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "LETTER_40301",
            "해당 편지에 접근할 권한이 없습니다."
    ),

    LETTER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "LETTER_40401",
            "편지를 찾을 수 없습니다."
    ),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "LETTER_50001",
            "처리 중 서버 오류가 발생했습니다."
    ),

    SUMMARY_INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "LETTER_50002",
            "편지 요약 처리 중 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
