package com.deare.backend.api.image.dto.response;

public record ImageUploadResponseDTO(
        Long imageId,
        String key,
        String url
) {}
