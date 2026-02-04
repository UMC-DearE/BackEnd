package com.deare.backend.domain.setting.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MembershipErrorCode implements BaseErrorCode {

    MEMBERSHIP_UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "MEMBERSHIP_40101",
            "로그인이 필요한 요청입니다."
    ),
    MEMBERSHIP_CONFLICT(
            HttpStatus.CONFLICT,
            "MEMBERSHIP_40901",
            "이미 PLUS 회원입니다."
    ),
    MEMBERSHIP_INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "MEMBERSHIP_50001",
            "처리 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
