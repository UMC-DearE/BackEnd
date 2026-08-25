package com.deare.backend.global.common.exception;

import lombok.Getter;

/**
 * 에러코드의 고정 메시지 대신, 상황에 따라 동적으로 생성된 메시지를 응답에 담아야 할 때 사용한다.
 * (예: "새 편지 2통이 필요해요." 처럼 남은 개수에 따라 메시지가 달라지는 경우)
 */
@Getter
public class GeneralMessageException extends GeneralException {

    private final String customMessage;

    public GeneralMessageException(BaseErrorCode errorCode, String customMessage) {
        super(errorCode);
        this.customMessage = customMessage;
    }
}
