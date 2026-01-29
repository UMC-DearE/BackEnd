package com.deare.backend.global.auth.oauth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponseDTO(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn
) {}
