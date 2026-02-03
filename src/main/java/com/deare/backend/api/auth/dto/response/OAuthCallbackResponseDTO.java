package com.deare.backend.api.auth.dto.response;

import java.time.LocalDateTime;

/**
 * OAuth 콜백 응답 DTO
 */
public record OAuthCallbackResponseDTO(
        OAuthResultType resultType,
        LocalDateTime createdAt
) {
    public static OAuthCallbackResponseDTO registered() {
        return new OAuthCallbackResponseDTO(OAuthResultType.REGISTERED, LocalDateTime.now());
    }

    public static OAuthCallbackResponseDTO signupRequired() {
        return new OAuthCallbackResponseDTO(OAuthResultType.SIGNUP_REQUIRED, LocalDateTime.now());
    }
}
