package com.deare.backend.global.common.exception;

import com.deare.backend.global.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(GeneralException e) {
        BaseErrorCode ec = e.getErrorCode();

        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 여기엔 COMMON_500 같은 공통 코드 하나 두고 쓰는 걸 추천
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail("COMMON_500", "서버 오류가 발생했습니다."));
    }
}