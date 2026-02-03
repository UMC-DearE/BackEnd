package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.*;
import com.deare.backend.api.letter.util.ExcerptUtil;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.exception.FromErrorCode;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService {

    private static final int EXCERPT_MAX_CHARS = 100;

    private final LetterRepository letterRepository;
    private final LetterEmotionQueryRepository letterEmotionQueryRepository;
    private final FromRepository fromRepository;

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDTO getLetterList(
            Pageable pageable,
            Long userId,
            Long folderId,
            Long fromId,
            Boolean isLiked,
            String keyword
    ) {

        Page<Letter> page = letterRepository.findLettersForList(
                userId,
                folderId,
                fromId,
                isLiked,
                keyword,
                pageable
        );

        List<LetterItemDTO> items = page.getContent().stream()
                .map(this::toItemDTO)
                .toList();

        return new LetterListResponseDTO(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                items
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LetterDetailResponseDTO getLetterDetail(Long userId, Long letterId) {

        Letter letter = letterRepository
                .findLetterDetailById(userId, letterId)
                .orElseThrow(() ->
                        new GeneralException(LetterErrorCode.NOT_FOUND)
                );

        List<EmotionTagDTO> emotionTags =
                letterEmotionQueryRepository.findEmotionTagsByLetterId(letterId)
                        .stream()
                        .map(p -> new EmotionTagDTO(
                                p.emotionId(),
                                p.emotionName(),
                                new EmotionCategoryDTO(
                                        p.category().categoryId(),
                                        p.category().type(),
                                        p.category().bgColor(),
                                        p.category().fontColor()
                                )
                        ))
                        .toList();

        List<String> imageUrls = letter.getLetterImages().stream()
                .map(li -> li.getImage() != null ? li.getImage().getImageUrl() : null)
                .filter(java.util.Objects::nonNull)
                .toList();

        return new LetterDetailResponseDTO(
                letter.getContent(),
                letter.getReceivedAt(),
                letter.getAiSummary(),
                letter.isLiked(),
                letter.getReply(),
                new LetterFromDTO(
                        letter.getFrom().getId(),
                        letter.getFrom().getName(),
                        letter.getFrom().getBackgroundColor(),
                        letter.getFrom().getFontColor()
                ),
                letter.getCreatedAt(),
                letter.getFolder() != null
                        ? new LetterFolderDTO(
                        letter.getFolder().getId(),
                        letter.getFolder().getName()
                )
                        : null,
                emotionTags,
                imageUrls
        );
    }

    @Transactional
    public void updateLetter(Long userId, Long letterId, LetterUpdateRequestDTO req) {

        if (req == null || !req.hasAnyField()) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter letter = getOwnedActiveLetter(userId, letterId);

        if (req.getFromId() != null) {
            From from = fromRepository.findById(req.getFromId())
                    .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

            if (!from.isOwnedBy(userId)) {
                throw new GeneralException(FromErrorCode.FROM_40301);
            }
            letter.changeFrom(from);
        }

        if (req.isReceivedAtSpecified()) {
            letter.updateReceivedAt(req.getReceivedAt());
        }

        if (StringUtils.hasText(req.getContent())) {
            try {
                String newSummary = "요약 결과"; // TODO: 실제 요약 연동 필요
                String newHash = "hash";       // TODO: 해시 계산

                letter.updateContent(req.getContent(), newSummary, newHash);
            } catch (Exception e) {
                throw new GeneralException(LetterErrorCode.SUMMARY_INTERNAL_ERROR);
            }
        }
    }

    @Override
    @Transactional
    public void deleteLetter(Long userId, Long letterId) {

        if (userId == null) {
            throw new GeneralException(LetterErrorCode.UNAUTHORIZED);
        }
        if (letterId == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.NOT_FOUND));

        if (letter.isDeleted()) {
            throw new GeneralException(LetterErrorCode.DELETED_LETTER);
        }
        if (!letter.isOwnedBy(userId)) {
            throw new GeneralException(LetterErrorCode.FORBIDDEN);
        }

        letter.softDelete();
    }

    @Override
    @Transactional
    public LetterLikeResponseDTO  likeLetter(Long userId, Long letterId) {
        Letter letter = getOwnedActiveLetter(userId, letterId);
        letter.like();
        return new LetterLikeResponseDTO(true);
    }

    @Override
    @Transactional
    public LetterLikeResponseDTO unlikeLetter(Long userId, Long letterId) {
        Letter letter = getOwnedActiveLetter(userId, letterId);
        letter.unlike();
        return new LetterLikeResponseDTO(false);
    }

    private Letter getOwnedActiveLetter(Long userId, Long letterId) {
        if (userId == null) {
            throw new GeneralException(LetterErrorCode.UNAUTHORIZED);
        }
        if (letterId == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter letter = letterRepository.findById(letterId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.NOT_FOUND));

        if (letter.isDeleted()) {
            throw new GeneralException(LetterErrorCode.DELETED_LETTER);
        }
        if (!letter.isOwnedBy(userId)) {
            throw new GeneralException(LetterErrorCode.FORBIDDEN);
        }

        return letter;
    }

    private LetterItemDTO toItemDTO(Letter letter) {
        return new LetterItemDTO(
                letter.getId(),
                ExcerptUtil.excerptByChars(letter.getContent(), EXCERPT_MAX_CHARS),
                letter.isLiked(),
                letter.getReceivedAt(),
                letter.getCreatedAt(),
                new LetterFromDTO(
                        letter.getFrom().getId(),
                        letter.getFrom().getName(),
                        letter.getFrom().getBackgroundColor(),
                        letter.getFrom().getFontColor()
                ),
                letter.getFolder() != null ? letter.getFolder().getId() : null
        );
    }
}
