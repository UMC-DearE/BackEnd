package com.deare.backend.api.folder.dto;

import java.util.List;

public record FolderOrderResponseDTO(
        List<Long> folderIds
) {}
