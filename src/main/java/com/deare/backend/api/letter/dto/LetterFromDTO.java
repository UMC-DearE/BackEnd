package com.deare.backend.api.letter.dto;

public record LetterFromDTO(
        Long fromId,
        String name,
        String bgColor,
        String fontColor
) {}
