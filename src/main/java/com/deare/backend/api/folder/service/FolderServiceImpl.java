package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.FolderItemDTO;
import com.deare.backend.api.folder.dto.FolderListResponseDTO;
import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;

    @Override
    @Transactional(readOnly = true)
    public FolderListResponseDTO getFolderList(Long userId) {
        List<Folder> folders =
                folderRepository.findAllByUser_IdAndIsDeletedFalseOrderByFolderOrderAsc(userId);

        List<FolderItemDTO> items = folders.stream()
                .map(f -> new FolderItemDTO(
                        f.getId(),
                        f.getName(),
                        f.getImage() != null ? f.getImage().getImageUrl() : null,
                        f.getFolderOrder()
                ))
                .toList();

        return new FolderListResponseDTO(items);
    }
}