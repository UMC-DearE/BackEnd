package com.deare.backend.api.folder.dto.response;

import java.util.List;

public record FolderOrderResponseDTO(
        List<Long> folderIds
) {}
