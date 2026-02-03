package com.deare.backend.global.auth.oauth.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponseDTO(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn
) {}
