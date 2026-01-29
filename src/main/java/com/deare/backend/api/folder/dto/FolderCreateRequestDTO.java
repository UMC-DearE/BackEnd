package com.deare.backend.api.folder.dto;

import jakarta.validation.constraints.Size;

public record FolderCreateRequestDTO (
        @Size(min = 1, max = 6)
        String name,
        Long imageId
) {
}
