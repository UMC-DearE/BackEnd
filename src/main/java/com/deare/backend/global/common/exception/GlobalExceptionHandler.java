package com.deare.backend.global.common.exception;

import com.deare.backend.global.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(GeneralException e) {
        BaseErrorCode ec = e.getErrorCode();

        log.warn("[GeneralException] Code: {}, Message: {}", ec.getCode(), ec.getMessage());

        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("[Validation Error] {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "COMMON_400_VALIDATION",
                        message.isBlank() ? "요청 값이 올바르지 않습니다." : message
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        String message = String.format("'%s' 값이 올바르지 않습니다.", e.getName());
        log.warn("[Type Mismatch] Field: {}, Value: {}", e.getName(), e.getValue());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("COMMON_400_TYPE_MISMATCH", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(
            HttpMessageNotReadableException e
    ) {
        log.warn("[JSON Parse Error] {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "COMMON_400_BODY_NOT_READABLE",
                        "요청 본문(JSON)을 올바르게 작성해 주세요."
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("[Constraint Violation] {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "COMMON_400_CONSTRAINT",
                        message.isBlank() ? "요청 값이 올바르지 않습니다." : message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Unhandled Exception] ", e);

        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail("COMMON_500", "서버 오류가 발생했습니다."));
    }

    private String formatFieldError(FieldError fe) {
        return String.format("[%s] %s", fe.getField(),
                (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "입력값이 올바르지 않습니다."));
    }
}