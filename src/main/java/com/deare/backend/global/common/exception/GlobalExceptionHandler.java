package com.deare.backend.global.common.exception;

import com.deare.backend.global.common.response.ApiResponse;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(GeneralException e) {
        BaseErrorCode ec = e.getErrorCode();

        logByStatus("GeneralException", ec, ec.getMessage(), e);

        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

    @ExceptionHandler(GeneralMessageException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralMessage(GeneralMessageException e) {
        BaseErrorCode ec = e.getErrorCode();
        String message = e.getCustomMessage();

        logByStatus("GeneralMessageException", ec, message, e);

        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("[Validation Error] {}", message);

        return response(
                CommonErrorCode.VALIDATION_FAILED,
                message.isBlank() ? CommonErrorCode.VALIDATION_FAILED.getMessage() : message
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e
    ) {
        String message = String.format("'%s' 값이 올바르지 않습니다.", e.getName());
        log.warn("[Type Mismatch] Field: {}, Value: {}", e.getName(), e.getValue());

        return response(CommonErrorCode.TYPE_MISMATCH, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(
            HttpMessageNotReadableException e
    ) {
        log.warn("[JSON Parse Error] {}", e.getMessage());

        return response(CommonErrorCode.BODY_NOT_READABLE);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException e
    ) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("[Constraint Violation] {}", message);

        return response(
                CommonErrorCode.CONSTRAINT_VIOLATION,
                message.isBlank() ? CommonErrorCode.CONSTRAINT_VIOLATION.getMessage() : message
        );
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ApiResponse<Void>> handleRequestBinding(ServletRequestBindingException e) {
        return handleCommonError(CommonErrorCode.REQUEST_BINDING_FAILED, e);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return handleCommonError(CommonErrorCode.RESOURCE_NOT_FOUND, e);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return handleCommonError(CommonErrorCode.METHOD_NOT_ALLOWED, e);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return handleCommonError(CommonErrorCode.MEDIA_TYPE_NOT_SUPPORTED, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Unhandled Exception] ", e);

        return response(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String formatFieldError(FieldError fe) {
        return String.format("[%s] %s", fe.getField(),
                (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "입력값이 올바르지 않습니다."));
    }

    private ResponseEntity<ApiResponse<Void>> handleCommonError(CommonErrorCode errorCode, Exception e) {
        log.warn("[{}] Code: {}, Message: {}", e.getClass().getSimpleName(), errorCode.getCode(), e.getMessage());
        return response(errorCode);
    }

    private ResponseEntity<ApiResponse<Void>> response(BaseErrorCode errorCode) {
        return response(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> response(BaseErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getCode(), message));
    }

    private void logByStatus(String exceptionType, BaseErrorCode errorCode, String message, Exception e) {
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("[{}] Code: {}, Message: {}", exceptionType, errorCode.getCode(), message, e);
            return;
        }

        log.warn("[{}] Code: {}, Message: {}", exceptionType, errorCode.getCode(), message);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalApiException(
            ExternalApiException e
    ) {
        BaseErrorCode ec = e.getErrorCode();

        logByStatus("ExternalApiException", ec, ec.getMessage(), e);

        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), ec.getMessage()));
    }

}
