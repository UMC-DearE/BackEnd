package com.deare.backend.domain.sticker.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StickerErrorCode implements BaseErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "STICKER_40001",
            "잘못된 요청입니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "STICKER_40101",
            "로그인이 필요한 요청입니다."
    ),
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "STICKER_40301",
            "스티커에 대한 권한이 없습니다."
    ),
    IMAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "STICKER_40401",
            "이미지를 찾을 수 없습니다."
    ),
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "STICKER_40402",
            "스티커를 찾을 수 없습니다."
    ),


    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "STICKER_50001",
            "스티커 생성 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
