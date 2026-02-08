package com.deare.backend.domain.folder.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FolderErrorCode implements BaseErrorCode {

    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "FOLDER_40001",
            "잘못된 요청입니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "FOLDER_40101",
            "로그인이 필요한 요청입니다."
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "FOLDER_40301",
            "폴더에 대한 접근 권한이 없습니다."
    ),

    FOLDER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FOLDER_40401",
            "존재하지 않는 폴더입니다."
    ),
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FOLDER_40402",
            "존재하지 않는 사용자입니다."
    ),

    INVALID_FOLDER_NAME_LENGTH(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "FOLDER_42201",
            "폴더 이름은 1자 이상 6자 이하로 입력해야 합니다."
    ),
    MAX_FOLDER_LIMIT_EXCEEDED(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "FOLDER_42202",
            "폴더는 최대 3개까지 생성할 수 있습니다."
    ),
    INVALID_FOLDER_ORDER(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "FOLDER_42203",
            "폴더 순서 배열이 유효하지 않습니다."
    ),

    // 추후 고도화 시 COMMON으로 빼는 것도 고려
    FOLDER_50001(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "FOLDER_50001",
            "폴더 처리 중 서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
