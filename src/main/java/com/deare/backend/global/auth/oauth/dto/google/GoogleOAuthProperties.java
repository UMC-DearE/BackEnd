package com.deare.backend.global.auth.oauth.dto.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String authorizeUri,
        String tokenUri,
        String userInfoUri,
        String scope
) {}
