package com.deare.backend.domain.letter.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OcrErrorCode implements BaseErrorCode {

    OCR_BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "OCR_40001",
            "잘못된 요청입니다."
    ),
    OCR_INVALID_IMAGE_IDS(
            HttpStatus.BAD_REQUEST,
            "OCR_40002",
            "잘못된 요청입니다."
    ),
    OCR_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "OCR_40101",
            "인증이 필요합니다."
    ),
    OCR_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "OCR_40401",
            "이미지를 찾을 수 없습니다."
    ),
    OCR_TOO_MANY_IMAGES(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "OCR_42201",
            "요청 가능한 이미지 개수를 초과했습니다."
    ),
    OCR_INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "OCR_50001",
            "처리 중 서버 오류가 발생했습니다."
    );


    private final HttpStatus status;
    private final String code;
    private final String message;
}
