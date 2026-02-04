package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.*;
import com.deare.backend.api.letter.util.ExcerptUtil;
import com.deare.backend.domain.emotion.entity.Emotion;
import com.deare.backend.domain.emotion.entity.LetterEmotion;
import com.deare.backend.domain.emotion.repository.EmotionRepository;
import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.from.exception.FromErrorCode;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.LetterImage;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService {

    private static final int EXCERPT_MAX_CHARS = 100;

    private final LetterRepository letterRepository;
    private final LetterEmotionQueryRepository letterEmotionQueryRepository;
    private final FromRepository fromRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final LetterEmotionRepository letterEmotionRepository;
    private final ImageRepository imageRepository;

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

    @Override
    @Transactional
    public LetterCreateResponseDTO createLetter(Long userId, LetterCreateRequestDTO req) {
        if (userId == null) {
            throw new GeneralException(LetterErrorCode.UNAUTHORIZED);
        }
        if (req == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }
        if (req.fromId() == null) {
            throw new GeneralException(LetterErrorCode.FROM_REQUIRED);
        }
        if (!StringUtils.hasText(req.content())) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }
        if (!StringUtils.hasText(req.aiSummary())) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }
        if (req.emotionIds() == null || req.emotionIds().isEmpty()) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.UNAUTHORIZED));

        From from = fromRepository.findById(req.fromId())
                .orElseThrow(() -> new GeneralException(LetterErrorCode.FROM_REQUIRED));

        if (!from.isOwnedBy(userId)) {
            throw new GeneralException(FromErrorCode.FROM_40301);
        }

        String content = req.content().trim();
        String aiSummary = req.aiSummary().trim();
        String contentHash = DigestUtils.sha256Hex(content);
        int contentVersion = 1;

        LocalDate receivedAt = req.receivedAt();

        Letter letter = new Letter(
                content,
                receivedAt,
                aiSummary,
                contentVersion,
                contentHash,
                user,
                from,
                null
        );

        List<Long> imageIds = (req.imageIds() == null) ? List.of() : req.imageIds();

        if (!imageIds.isEmpty()) {
            if (imageIds.size() > 10) {
                throw new GeneralException(ImageErrorCode.IMAGE_41301);
            }

            List<Image> images = imageRepository.findAllById(imageIds);
            if (images.size() != imageIds.size()) {
                throw new GeneralException(ImageErrorCode.IMAGE_40401);
            }

            Map<Long, Image> imageMap = images.stream()
                    .collect(Collectors.toMap(Image::getId, i -> i));

            for (int i = 0; i < imageIds.size(); i++) {
                Image image = imageMap.get(imageIds.get(i));
                LetterImage li = LetterImage.create(image, i + 1);
                letter.addLetterImage(li);
            }
        }

        Letter saved = letterRepository.save(letter);

        List<Long> emotionIds = req.emotionIds();
        List<Long> distinctIds = emotionIds.stream().distinct().toList();
        List<Emotion> emotions = emotionRepository.findAllById(distinctIds);

        if (emotions.size() != distinctIds.size()) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
            //추후 emotionerrorcode로 변경예정
        }

        List<LetterEmotion> mappings = emotions.stream()
                .map(e -> new LetterEmotion(saved, e))
                .toList();

        letterEmotionRepository.saveAll(mappings);

        return new LetterCreateResponseDTO(saved.getId(), saved.getCreatedAt());
    }

    @Transactional
    public void updateLetter(Long userId, Long letterId, LetterUpdateRequestDTO req) {

        if (req == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        if (req.getContent() != null && !StringUtils.hasText(req.getContent())) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        if (!req.hasAnyField()) {
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
                // TODO(ai-summary): content 변경 시 AI 요약 재생성 연동 필요
                // TODO(emotion): content 변경 시 감정 분석/태그 재생성 연동 필요

                String newSummary = "요약 결과"; // 임시값 (TODO 이후 변경)

                String normalizedContent = req.getContent().trim();
                String newHash = DigestUtils.sha256Hex(normalizedContent);

                letter.updateContent(req.getContent(), newSummary, newHash);
            } catch (Exception e) {
                throw new GeneralException(LetterErrorCode.SUMMARY_INTERNAL_ERROR);
            }
        }
    }

    @Override
    @Transactional
    public void deleteLetter(Long userId, Long letterId) {
        Letter letter = getOwnedActiveLetter(userId, letterId);
        letter.softDelete();
    }

    @Override
    @Transactional
    public LetterLikeResponseDTO likeLetter(Long userId, Long letterId) {
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
