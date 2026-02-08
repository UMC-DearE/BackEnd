package com.deare.backend.api.auth.dto.result;

/**
 * OAuth 콜백 결과
 */
public record OAuthCallbackResult(
        boolean isRegistered,
        String refreshToken,
        String signupToken
) {}
