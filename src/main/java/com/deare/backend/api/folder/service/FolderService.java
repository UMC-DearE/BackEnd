package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.request.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderListResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderOrderResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderOrderRequestDTO;
import com.deare.backend.api.folder.dto.request.FolderUpdateRequestDTO;

public interface FolderService {

    FolderListResponseDTO getFolderList(Long userId);
    FolderCreateResponseDTO createFolder(
            Long userId, FolderCreateRequestDTO req);
    void deleteFolder(Long userId, Long folderId);
    void addLetterToFolder(Long userId, Long folderId, Long letterId);
    void removeLetterFromFolder(Long userId, Long folderId, Long letterId);
    FolderOrderResponseDTO updateOrders(Long userId, FolderOrderRequestDTO reqDTO);
    void updateFolder(Long userId, Long folderId, FolderUpdateRequestDTO reqDTO);
}