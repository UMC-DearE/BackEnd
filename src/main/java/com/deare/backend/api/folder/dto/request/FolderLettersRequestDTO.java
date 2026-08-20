package com.deare.backend.api.folder.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record FolderLettersRequestDTO(
        @NotEmpty
        List<@NotNull @Positive Long> letterIds
) {
}
