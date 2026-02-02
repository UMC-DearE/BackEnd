package com.deare.backend.api.auth.dto.response;

import java.time.LocalDateTime;

/**
 * OAuth 콜백 응답 DTO
 * - REGISTERED: 기존 가입자
 * - SIGNUP_REQUIRED: 미가입자 (회원가입 필요)
 */
public record OAuthCallbackResponseDTO(
        String resultType,
        LocalDateTime createdAt
) {
    public static OAuthCallbackResponseDTO registered() {
        return new OAuthCallbackResponseDTO("REGISTERED", LocalDateTime.now());
    }

    public static OAuthCallbackResponseDTO signupRequired() {
        return new OAuthCallbackResponseDTO("SIGNUP_REQUIRED", LocalDateTime.now());
    }
}
