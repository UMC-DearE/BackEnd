package com.deare.backend.api.auth.dto.result;

import com.deare.backend.api.auth.dto.response.OAuthCallbackResponseDTO;

/**
 * OAuth 콜백 결과
 */
public record OAuthCallbackResult(
        boolean isRegistered,
        String accessToken,
        String refreshToken,
        String signupToken,
        OAuthCallbackResponseDTO response
) {}
