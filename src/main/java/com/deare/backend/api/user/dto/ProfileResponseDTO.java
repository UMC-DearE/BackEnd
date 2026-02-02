package com.deare.backend.api.user.dto;

public record ProfileResponseDTO(
        Long userId,
        String nickname,
        String intro,
        String profileImageUrl
) {
}
