package com.deare.backend.api.auth.dto.response;

import lombok.Builder;

@Builder
public record RefreshResponseDTO(
        String accessToken
) {
}
