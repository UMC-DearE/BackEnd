package com.deare.backend.api.user.dto.response;

public record ProfileUpdateResponseDTO(
        Long userId,
        String nickname,
        String intro,
        String profileImageUrl
) {
}
