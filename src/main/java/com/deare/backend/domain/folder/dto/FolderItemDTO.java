package com.deare.backend.domain.folder.dto;

public record FolderItemDTO (
        Long id,
        String name,
        String imageUrl,
        int folderOrder
){
}
