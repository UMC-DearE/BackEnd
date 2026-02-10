package com.deare.backend.api.folder.dto.response;

import com.deare.backend.api.folder.dto.result.FolderItemDTO;

import java.util.List;

public record FolderListResponseDTO(
        List<FolderItemDTO> items
) {
}
