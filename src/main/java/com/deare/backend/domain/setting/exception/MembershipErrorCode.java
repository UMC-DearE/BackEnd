package com.deare.backend.domain.setting.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MembershipErrorCode implements BaseErrorCode {

    PLUS_REQUIRED(
            HttpStatus.FORBIDDEN,
            "MEMBERSHIP_40301",
            "PLUS 회원만 사용할 수 있습니다."
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
