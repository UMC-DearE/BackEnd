package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.FolderListResponseDTO;

public interface FolderService {

    FolderListResponseDTO getFolderList(Long userId);

    FolderCreateResponseDTO createFolder(Long userId, FolderCreateRequestDTO req);

}