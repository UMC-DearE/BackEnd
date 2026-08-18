package com.deare.backend.api.term.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermErrorCode implements BaseErrorCode {

    INVALID_TERM_TYPE(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "TERM_42201",
            "잘못된 type 값입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
