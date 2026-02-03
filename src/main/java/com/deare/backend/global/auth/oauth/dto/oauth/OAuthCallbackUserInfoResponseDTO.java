package com.deare.backend.global.auth.oauth.dto.oauth;

import java.time.LocalDateTime;

public record OAuthCallbackUserInfoResponseDTO(
        String provider,
        String providerUserId,
        String email,
        LocalDateTime createdAt
) {}
