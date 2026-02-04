package com.deare.backend.domain.setting.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ThemeErrorCode implements BaseErrorCode {

    THEME_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "THEME_40101",
            "로그인이 필요한 요청입니다."
    ),
    THEME_FORBIDDEN(HttpStatus.FORBIDDEN,
            "THEME_40301",
            "PLUS 회원만 사용할 수 있습니다."
    ),
    THEME_UNPROCESSABLE_ENTITY(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "THEME_42201",
            "지원하지 않는 폰트입니다."
    ),
    THEME_INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "THEME_50001",
            "처리 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

}
