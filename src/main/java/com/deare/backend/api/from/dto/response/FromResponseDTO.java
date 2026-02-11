package com.deare.backend.api.from.dto.response;

public record FromResponseDTO(
        Long fromId,
        String name,
        String bgColor,
        String fontColor,
        long letterCount
) {}
