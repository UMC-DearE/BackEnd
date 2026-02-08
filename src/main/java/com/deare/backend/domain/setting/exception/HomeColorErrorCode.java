package com.deare.backend.domain.setting.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HomeColorErrorCode implements BaseErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "HOME_COLOR_40001",
            "잘못된 홈 배경색 요청입니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "HOME_COLOR_40101",
            "로그인이 필요한 요청입니다."
    ),

    SETTING_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "HOME_COLOR_40401",
            "사용자 설정 정보를 찾을 수 없습니다."
    ),

    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "HOME_COLOR_50001",
            "홈 배경색 변경 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
