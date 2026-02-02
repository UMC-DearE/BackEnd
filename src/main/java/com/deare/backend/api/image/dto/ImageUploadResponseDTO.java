package com.deare.backend.api.image.dto;

public record ImageUploadResponseDTO(
        Long imageId,
        String key,
        String url
) {}
