package com.deare.backend.domain.folder.exception;

import com.deare.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FolderErrorCode implements BaseErrorCode {

    FOLDER_40101(
            HttpStatus.UNAUTHORIZED,
            "FOLDER_40101",
            "로그인이 필요한 요청입니다."
    ),

    FOLDER_40301(
            HttpStatus.FORBIDDEN,
            "FOLDER_40301",
            "폴더에 대한 접근 권한이 없습니다."
    ),

    FOLDER_40401(
            HttpStatus.NOT_FOUND,
            "FOLDER_40401",
            "존재하지 않는 폴더입니다."
    ),

    FOLDER_40001(
            HttpStatus.BAD_REQUEST,
            "FOLDER_40001",
            "폴더는 최대 5개까지 생성할 수 있습니다."
    ),

    FOLDER_40002(
            HttpStatus.BAD_REQUEST,
            "FOLDER_40002",
            "폴더 이름은 1자 이상 6자 이하로 입력해야 합니다."
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
