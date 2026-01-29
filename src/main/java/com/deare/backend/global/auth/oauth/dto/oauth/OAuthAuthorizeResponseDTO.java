package com.deare.backend.global.auth.oauth.dto.oauth;

import java.time.LocalDateTime;

public record OAuthAuthorizeResponseDTO(
        String authorizeUrl,
        LocalDateTime createdAt
) {}