package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.*;
import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.folder.exception.FolderErrorCode;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private static final int MAX_FOLDERS = 3;

    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;

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

    @Override
    @Transactional
    public FolderCreateResponseDTO createFolder(Long userId, FolderCreateRequestDTO req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_50001));

        long count = folderRepository.countByUser_IdAndIsDeletedFalse(userId);
        if (count >= MAX_FOLDERS) {
            throw new GeneralException(FolderErrorCode.MAX_FOLDER_LIMIT_EXCEEDED);
        }

        Image image = null;
        if (req.imageId() != null) {
            image = imageRepository.findById(req.imageId())
                    .orElseThrow(() -> new GeneralException(FolderErrorCode.INVALID_REQUEST));
        }

        int nextOrder = folderRepository.findMaxFolderOrder(userId) + 1;

        Folder folder = Folder.create(
                req.name(),
                nextOrder,
                image,
                user
        );

        Folder saved = folderRepository.save(folder);

        String createdAt = saved.getCreatedAt()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        return new FolderCreateResponseDTO(saved.getId(), createdAt);
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        letterRepository.clearFolder(userId, folderId);
        folder.softDelete();
        folderRepository.save(folder);
    }

    @Override
    @Transactional
    public void addLetterToFolder(Long userId, Long folderId, Long letterId) {
        Folder folder = folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        Letter letter = letterRepository.findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.DELETED_LETTER));

        if (letter.getFolder() != null && letter.getFolder().getId().equals(folderId)) {
            return;
        }

        letter.changeFolder(folder);
    }

    @Override
    @Transactional
    public void removeLetterFromFolder(Long userId, Long folderId, Long letterId) {
        folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        Letter letter = letterRepository.findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.INVALID_REQUEST)); // TODO: LetterErrorCode로 교체

        if (letter.getFolder() == null || !letter.getFolder().getId().equals(folderId)) {
            throw new GeneralException(FolderErrorCode.INVALID_REQUEST);
        }

        letter.changeFolder(null);
    }

    @Override
    @Transactional
    public FolderOrderResponseDTO updateOrders(Long userId, FolderOrderRequestDTO reqDTO) {
        if (reqDTO == null || reqDTO.foldersOrder() == null) {
            throw new GeneralException(FolderErrorCode.INVALID_FOLDER_ORDER);
        }

        List<Long> requestedIds = reqDTO.foldersOrder();

        if (requestedIds.size() != new java.util.HashSet<>(requestedIds).size()) {
            throw new GeneralException(FolderErrorCode.INVALID_FOLDER_ORDER);
        }

        List<Folder> folders =
                folderRepository.findAllByUser_IdAndIsDeletedFalseOrderByFolderOrderAsc(userId);

        List<Long> existingIds = folders.stream().map(Folder::getId).toList();

        if (existingIds.size() != requestedIds.size()
                || !new java.util.HashSet<>(existingIds).equals(new java.util.HashSet<>(requestedIds))) {
            throw new GeneralException(FolderErrorCode.INVALID_FOLDER_ORDER);
        }

        java.util.Map<Long, Folder> folderMap = folders.stream()
                .collect(java.util.stream.Collectors.toMap(Folder::getId, f -> f));

        for (int i = 0; i < requestedIds.size(); i++) {
            Folder folder = folderMap.get(requestedIds.get(i));
            folder.changeOrder(i + 1);
        }

        return new FolderOrderResponseDTO(requestedIds);
    }

    @Override
    @Transactional
    public void updateFolder(Long userId, Long folderId, FolderUpdateRequestDTO reqDTO) {
        if (reqDTO == null || !reqDTO.hasAnyField()) {
            throw new GeneralException(FolderErrorCode.INVALID_REQUEST);
        }

        Folder folder = folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        if (reqDTO.name() != null) {
            folder.rename(reqDTO.name().trim());
        }

        if (reqDTO.imageId() != null) {
            Image image = imageRepository.findById(reqDTO.imageId())
                    .orElseThrow(() -> new GeneralException(FolderErrorCode.INVALID_REQUEST));

            folder.changeImage(image);
        }else{
            folder.changeImage(null);
        }
    }

}