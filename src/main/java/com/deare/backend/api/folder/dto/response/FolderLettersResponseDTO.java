package com.deare.backend.api.folder.dto.response;

public record FolderLettersResponseDTO(
        Long folderId,
        int processedCount
) {
}
