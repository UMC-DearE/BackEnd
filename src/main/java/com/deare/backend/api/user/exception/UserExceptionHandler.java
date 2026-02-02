package com.deare.backend.api.user.exception;

import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.global.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.deare.backend.api.user")
public class UserExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserValidation(
            MethodArgumentNotValidException e
    ) {
        boolean isNicknameSizeViolation = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fe ->
                        "nickname".equals(fe.getField())
                                && "Size".equals(fe.getCode())
                );

        if (isNicknameSizeViolation) {
            log.warn("[User Validation] nickname size violation");
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.fail(
                            UserErrorCode.USER_42201.getCode(),
                            UserErrorCode.USER_42201.getMessage()
                    ));
        }

        boolean isIntroSizeViolation = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fe ->
                        "intro".equals(fe.getField())
                                && "Size".equals(fe.getCode())
                );

        if (isIntroSizeViolation) {
            log.warn("[User Validation] intro size violation");
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.fail(
                            UserErrorCode.USER_42202.getCode(),
                            UserErrorCode.USER_42202.getMessage()
                    ));
        }

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("[User Validation] {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        UserErrorCode.USER_40001.getCode(),
                        message.isBlank()
                                ? UserErrorCode.USER_40001.getMessage()
                                : message
                ));
    }

    private String formatFieldError(FieldError fe) {
        return String.format("[%s] %s",
                fe.getField(),
                (fe.getDefaultMessage() != null
                        ? fe.getDefaultMessage()
                        : "입력값이 올바르지 않습니다."));
    }
}
