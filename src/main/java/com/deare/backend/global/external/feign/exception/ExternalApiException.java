package com.deare.backend.global.external.feign.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;

public class ExternalApiException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public ExternalApiException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BaseErrorCode getErrorCode() {
        return errorCode;
    }
}
