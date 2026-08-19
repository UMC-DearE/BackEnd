package com.deare.backend.api.invite.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InviteErrorCode implements BaseErrorCode {
    INVALID_INVITE_CODE(HttpStatus.NOT_FOUND, "INVITE_40401", "유효하지 않은 초대 링크입니다."),
    LINK_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "INVITE_50001", "초대 링크 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
