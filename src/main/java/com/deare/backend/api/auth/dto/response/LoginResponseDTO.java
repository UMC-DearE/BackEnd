package com.deare.backend.api.auth.dto.response;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String nickname,
        String role
) {
}
