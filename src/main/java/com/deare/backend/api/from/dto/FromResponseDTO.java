package com.deare.backend.api.from.dto;

public record FromResponseDTO(
        Long fromId,
        String name,
        String bgColor,
        String fontColor,
        long letterCount
) {}
