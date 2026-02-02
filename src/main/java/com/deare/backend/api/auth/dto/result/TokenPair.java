package com.deare.backend.api.auth.dto.result;

/**
 * 토큰 페어 (Access Token + Refresh Token)
 */
public record TokenPair(
        String accessToken,
        String refreshToken
) {}
