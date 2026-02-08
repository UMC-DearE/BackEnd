package com.deare.backend.api.folder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record FolderOrderRequestDTO(
        @Size(min = 1, max = 3)
        List<@NotNull @Positive Long> foldersOrder
) {}
