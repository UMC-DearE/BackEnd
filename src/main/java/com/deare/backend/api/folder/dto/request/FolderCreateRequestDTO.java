package com.deare.backend.api.folder.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderCreateRequestDTO (
        @JsonProperty("folder_name")
        @NotBlank
        @Size(min = 1, max = 6)
        String name,
        Long imageId
) {
}
