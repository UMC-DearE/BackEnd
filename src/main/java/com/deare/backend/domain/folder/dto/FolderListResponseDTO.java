package com.deare.backend.domain.folder.dto;

import java.util.List;

public record FolderListResponseDTO(
        List<FolderItemDTO> items
) {
}
