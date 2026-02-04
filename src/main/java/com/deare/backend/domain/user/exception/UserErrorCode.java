package com.deare.backend.domain.user.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            "USER_40001",
            "잘못된 요청입니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "USER_40101",
            "로그인이 필요한 요청입니다."
    ),

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_40401",
            "사용자를 찾을 수 없습니다."
    ),

    USER_IMAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "USER_40402",
            "이미지를 찾을 수 없습니다."
    ),

    NICKNAME_INVALID_LENGTH(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "USER_42201",
            "닉네임은 2자 이상 10자 이하입니다."
    ),

    INTRO_TOO_LONG(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "USER_42202",
            "소개글은 최대 20자입니다."
    ),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "USER_50001",
            "처리 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    private static final Map<String, UserErrorCode> BY_CODE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(UserErrorCode::getCode, Function.identity()));

    public static UserErrorCode fromCode(String code) {
        return BY_CODE.get(code);
    }
}
