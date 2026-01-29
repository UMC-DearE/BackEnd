package com.deare.backend.api.folder.dto;

public record FolderItemDTO (
        Long id,
        String name,
        String imageUrl,
        int folderOrder
){
}
