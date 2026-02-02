package com.deare.backend.global.auth.oauth.dto.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        String authorizeUri,
        String tokenUri,
        String userInfoUri
) {}
