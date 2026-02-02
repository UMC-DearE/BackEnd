package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.FolderItemDTO;
import com.deare.backend.api.folder.dto.FolderListResponseDTO;
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

    private static final int MAX_FOLDERS = 5;

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
            throw new GeneralException(FolderErrorCode.FOLDER_40001);
        }

        if (folderRepository.existsByUser_IdAndNameAndIsDeletedFalse(userId, req.name())) {
            throw new GeneralException(FolderErrorCode.FOLDER_40901);
        }

        Image image = null;
        if (req.imageId() != null) {
            image = imageRepository.findById(req.imageId())
                    .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_40003));
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
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_40401));

        letterRepository.clearFolder(userId, folderId);
        folder.softDelete();
    }

    @Override
    @Transactional
    public void addLetterToFolder(Long userId, Long folderId, Long letterId) {
        Folder folder = folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_40401));

        Letter letter = letterRepository.findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.LETTER_40301));

        if (letter.getFolder() != null && letter.getFolder().getId().equals(folderId)) {
            return;
        }

        letter.changeFolder(folder);
    }

    @Override
    @Transactional
    public void removeLetterFromFolder(Long userId, Long folderId, Long letterId) {
        folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_40401));

        Letter letter = letterRepository.findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_40003)); // TODO: LetterErrorCode로 교체

        if (letter.getFolder() == null || !letter.getFolder().getId().equals(folderId)) {
            throw new GeneralException(FolderErrorCode.FOLDER_40003);
        }

        letter.changeFolder(null);
    }
}