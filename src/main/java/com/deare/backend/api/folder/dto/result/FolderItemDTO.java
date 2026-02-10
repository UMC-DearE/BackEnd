package com.deare.backend.api.folder.dto.result;

public record FolderItemDTO (
        Long id,
        String name,
        String imageUrl,
        int folderOrder
){
}
