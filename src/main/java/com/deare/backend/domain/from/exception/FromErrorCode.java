package com.deare.backend.domain.from.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FromErrorCode implements BaseErrorCode {

    // 400 - 잘못된 요청 (수정 시 모든 필드 null / 색상 세트 불일치 등)
    FROM_40001(
            HttpStatus.BAD_REQUEST,
            "FROM_40001",
            "잘못된 요청입니다."
    ),

    // 403 - 본인 소유가 아닌 From 접근
    FROM_40301(
            HttpStatus.FORBIDDEN,
            "FROM_40301",
            "프롬 접근 권한이 없습니다."
    ),

    // 404 - From 자체가 없음
    FROM_40401(
            HttpStatus.NOT_FOUND,
            "FROM_40401",
            "존재하지 않는 프롬입니다."
    ),

    // 409 - 사용 중이라 삭제 불가
    FROM_40901(
            HttpStatus.CONFLICT,
            "FROM_40901",
            "삭제할 수 없는 프롬입니다."
    ),

    // 500 - From 처리 중 서버 오류
    FROM_50001(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FROM_50001",
            "프롬 처리 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
