package com.deare.backend.api.auth.dto.response;

import java.time.LocalDateTime;

/**
 * 회원가입 완료 응답 DTO
 */
public record SignupResponseDTO(
        LocalDateTime createdAt
) {
    public static SignupResponseDTO of() {
        return new SignupResponseDTO(LocalDateTime.now());
    }
}
