package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.FolderListResponseDTO;

public interface FolderService {

    FolderListResponseDTO getFolderList(Long userId);
}