package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.FolderListResponseDTO;

public interface FolderService {

    FolderListResponseDTO getFolderList(Long userId);
    FolderCreateResponseDTO createFolder(
            Long userId, FolderCreateRequestDTO req);
    void deleteFolder(Long userId, Long folderId);
    void addLetterToFolder(Long userId, Long folderId, Long letterId);
    void removeLetterFromFolder(Long userId, Long folderId, Long letterId);

}