package com.deare.backend.api.user.exception;

import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.deare.backend.api.user")
public class UserExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserValidation(
            MethodArgumentNotValidException e
    ) {
        // Validation 에러는 첫 번째 에러 기준으로 처리
        FieldError fe = e.getBindingResult().getFieldErrors().get(0);
        String code = fe.getDefaultMessage(); // ex) USER_42201

        UserErrorCode ec = UserErrorCode.fromCode(code);

        if (ec != null) {
            log.warn("[User Validation] field={}, code={}", fe.getField(), ec.getCode());
            return ResponseEntity.status(ec.getStatus())
                    .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
        }

        // fallback
        log.warn("[User Validation] unknown validation code: {}", code);
        return ResponseEntity.status(UserErrorCode.BAD_REQUEST.getStatus())
                .body(ApiResponse.fail(
                        UserErrorCode.BAD_REQUEST.getCode(),
                        UserErrorCode.BAD_REQUEST.getMessage()
                ));
    }
}
