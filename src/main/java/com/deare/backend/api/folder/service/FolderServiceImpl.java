package com.deare.backend.api.folder.service;

import com.deare.backend.api.folder.dto.request.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.request.FolderOrderRequestDTO;
import com.deare.backend.api.folder.dto.request.FolderUpdateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderListResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderOrderResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderLettersRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderLettersResponseDTO;
import com.deare.backend.api.folder.dto.response.UnassignedLetterListResponseDTO;
import com.deare.backend.api.folder.dto.result.FolderItemDTO;
import com.deare.backend.api.folder.dto.result.UnassignedLetterItemDTO;
import com.deare.backend.api.letter.mapper.LetterItemMapper;
import com.deare.backend.api.letter.service.LetterContentReader;
import com.deare.backend.api.letter.service.LetterSearchCandidateResolver;
import com.deare.backend.api.letter.service.LetterSearchResultPager;
import com.deare.backend.domain.folder.entity.Folder;
import com.deare.backend.domain.folder.exception.FolderErrorCode;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private static final int MAX_FOLDERS = 3;
    private final FolderRepository folderRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final LetterSearchCandidateResolver searchCandidateResolver;
    private final LetterContentReader contentReader;
    private final LetterSearchResultPager searchResultPager;

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
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_SERVER_ERROR));

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
    public FolderLettersResponseDTO addLettersToFolder(Long userId, Long folderId, FolderLettersRequestDTO reqDTO) {
        Folder folder = getOwnedActiveFolder(userId, folderId);
        Set<Long> distinctIds = new LinkedHashSet<>(reqDTO.letterIds());
        List<Letter> letters = letterRepository.findAllById(distinctIds);

        if (letters.size() != distinctIds.size() || letters.stream().anyMatch(Letter::isDeleted)) {
            throw new GeneralException(LetterErrorCode.LETTER_NOT_FOUND);
        }
        if (letters.stream().anyMatch(letter -> !letter.isOwnedBy(userId))) {
            throw new GeneralException(LetterErrorCode.FORBIDDEN);
        }

        letters.forEach(letter -> letter.changeFolder(folder));
        return new FolderLettersResponseDTO(folderId, distinctIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public UnassignedLetterListResponseDTO getUnassignedLetters(
            Pageable pageable,
            Long userId,
            Long fromId,
            Boolean isLiked,
        String keyword
    ) {
        Set<Long> indexedCandidateIds = searchCandidateResolver.resolve(userId, keyword)
                .orElse(null);
        Page<Letter> page;
        if (StringUtils.hasText(keyword)) {
            page = searchResultPager.verifyAndPage(
                    batch -> letterRepository.findAvailableLetters(
                            userId, fromId, isLiked, keyword, indexedCandidateIds, batch
                    ).getContent(),
                    keyword,
                    pageable
            );
        } else {
            page = letterRepository.findAvailableLetters(
                    userId, fromId, isLiked, keyword, indexedCandidateIds, pageable
            );
        }
        List<UnassignedLetterItemDTO> items = page.getContent().stream()
                .map(letter -> LetterItemMapper.toItemDTO(letter, contentReader.read(letter)))
                .map(UnassignedLetterItemDTO::from)
                .toList();

        return new UnassignedLetterListResponseDTO(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                items
        );
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
        if (reqDTO == null
                || !reqDTO.hasAnyField()
                || reqDTO.hasInvalidImageRequest()) {
            throw new GeneralException(FolderErrorCode.INVALID_REQUEST);
        }

        Folder folder = folderRepository.findByIdAndUser_IdAndIsDeletedFalse(folderId, userId)
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        if (reqDTO.name() != null) {
            folder.rename(reqDTO.name().trim());
        }

        if (reqDTO.imageAction() == null) {
            return;
        }

        switch (reqDTO.imageAction()) {
            case KEEP -> {
            }

            case CHANGE -> {
                Image image = imageRepository.findById(reqDTO.imageId())
                        .orElseThrow(() -> new GeneralException(ImageErrorCode.IMAGE_NOT_FOUND));

                folder.changeImage(image);
            }

            case DELETE -> folder.changeImage(null);
        }
    }

    private Folder getOwnedActiveFolder(Long userId, Long folderId) {
        Folder folder = folderRepository.findById(folderId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new GeneralException(FolderErrorCode.FOLDER_NOT_FOUND));

        if (!folder.getUser().getId().equals(userId)) {
            throw new GeneralException(FolderErrorCode.FORBIDDEN);
        }
        return folder;
    }

}
