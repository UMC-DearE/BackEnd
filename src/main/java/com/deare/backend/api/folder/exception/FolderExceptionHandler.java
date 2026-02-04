package com.deare.backend.api.folder.exception;

import com.deare.backend.domain.folder.exception.FolderErrorCode;
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
@RestControllerAdvice(basePackages = "com.deare.backend.api.folder")
public class FolderExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleFolderValidation(MethodArgumentNotValidException e) {
        boolean isNameSizeViolation = e.getBindingResult().getFieldErrors().stream()
                .anyMatch(fe -> "name".equals(fe.getField()) && "Size".equals(fe.getCode()));

        if (isNameSizeViolation) {
            log.warn("[Folder Validation] name size violation");
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.fail(
                            FolderErrorCode.INVALID_FOLDER_NAME_LENGTH.getCode(),
                            FolderErrorCode.INVALID_FOLDER_NAME_LENGTH.getMessage()
                    ));
        }

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        log.warn("[Folder Validation] {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        FolderErrorCode.INVALID_REQUEST.getCode(),
                        message.isBlank() ? FolderErrorCode.INVALID_REQUEST.getMessage() : message
                ));
    }

    private String formatFieldError(FieldError fe) {
        return String.format("[%s] %s",
                fe.getField(),
                (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "입력값이 올바르지 않습니다."));
    }
}
