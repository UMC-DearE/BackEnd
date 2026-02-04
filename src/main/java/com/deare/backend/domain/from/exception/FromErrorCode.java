package com.deare.backend.domain.from.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FromErrorCode implements BaseErrorCode {

    FROM_40401(
            HttpStatus.NOT_FOUND,
            "FROM_40401",
            "존재하지 않는 프롬입니다."
    ),

    FROM_40301(
            HttpStatus.FORBIDDEN,
            "FROM_40301",
            "프롬 접근 권한이 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
