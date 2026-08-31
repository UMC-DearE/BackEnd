package com.deare.backend.api.letter.service;

import com.deare.backend.api.analyze.dto.result.EmotionDTO;
import com.deare.backend.api.analyze.dto.response.ReAnalyzeResponseDTO;
import com.deare.backend.api.analyze.service.LetterAnalyzeService;
import com.deare.backend.api.letter.dto.request.LetterCreateRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterPinRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterReplyUpsertRequestDTO;
import com.deare.backend.api.letter.dto.request.LetterUpdateRequestDTO;
import com.deare.backend.api.letter.dto.response.*;
import com.deare.backend.api.letter.mapper.LetterItemMapper;
import com.deare.backend.api.letter.dto.result.*;
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
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.external.feign.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LetterServiceImpl implements LetterService {

    private final LetterRepository letterRepository;
    private final LetterEmotionQueryRepository letterEmotionQueryRepository;
    private final FromRepository fromRepository;
    private final UserRepository userRepository;
    private final EmotionRepository emotionRepository;
    private final LetterEmotionRepository letterEmotionRepository;
    private final ImageRepository imageRepository;
    private final LetterImageRepository letterImageRepository;
    private final LetterAnalyzeService letterAnalyzeService;
    private final LetterSearchTokenSynchronizer searchTokenSynchronizer;
    private final LetterSearchCandidateResolver searchCandidateResolver;
    private final LetterContentEncryptionSynchronizer contentEncryptionSynchronizer;
    private final LetterContentReader contentReader;
    private final LetterSearchResultPager searchResultPager;

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

        Set<Long> indexedCandidateIds = searchCandidateResolver.resolve(userId, keyword)
                .orElse(null);
        Pageable repositoryPageable = searchPageable(pageable, keyword);
        Page<Letter> candidates = letterRepository.findLettersForList(
                userId,
                folderId,
                fromId,
                isLiked,
                keyword,
                indexedCandidateIds,
                repositoryPageable
        );
        Page<Letter> page = searchResultPager.verifyAndPage(candidates, keyword, pageable);

        List<LetterItemDTO> items = page.getContent().stream()
                .map(letter -> LetterItemMapper.toItemDTO(letter, contentReader.read(letter)))
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
                        new GeneralException(LetterErrorCode.LETTER_NOT_FOUND)
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
                contentReader.read(letter),
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.UNAUTHORIZED));

        From from = fromRepository.findByIdAndUser_IdAndIsDeletedFalse(req.fromId(), userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.FROM_NOT_FOUND));

        String content = req.content().trim();
        String aiSummary = req.aiSummary().trim();
        int contentVersion = 1;

        LocalDate receivedAt = req.receivedAt();

        Letter letter = new Letter(
                receivedAt,
                aiSummary,
                contentVersion,
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
                throw new GeneralException(ImageErrorCode.IMAGE_NOT_FOUND);
            }

            Map<Long, Image> imageMap = images.stream()
                    .collect(Collectors.toMap(Image::getId, i -> i));

            for (int i = 0; i < imageIds.size(); i++) {
                Image image = imageMap.get(imageIds.get(i));
                LetterImage li = LetterImage.create(image, i + 1);
                letter.addLetterImage(li);
            }
        }

        List<Long> emotionIds = req.emotionIds();
        List<Long> distinctIds = emotionIds.stream().distinct().toList();
        List<Emotion> emotions = emotionRepository.findAllById(distinctIds);

        if (emotions.size() != distinctIds.size()) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter saved = letterRepository.save(letter);
        contentEncryptionSynchronizer.synchronize(saved, userId, content);

        List<LetterEmotion> mappings = emotions.stream()
                .map(e -> new LetterEmotion(saved, e))
                .toList();

        letterEmotionRepository.saveAll(mappings);
        searchTokenSynchronizer.indexCreatedLetter(saved, userId, content);

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
            From from = fromRepository.findByIdAndUser_IdAndIsDeletedFalse(req.getFromId(), userId)
                    .orElseThrow(() -> new GeneralException(FromErrorCode.FROM_40401));

            letter.changeFrom(from);
        }

        if (req.isReceivedAtSpecified()) {
            letter.updateReceivedAt(req.getReceivedAt());
        }

        if (StringUtils.hasText(req.getContent())) {
            String normalizedContent = req.getContent().trim();
            if (normalizedContent.equals(contentReader.read(letter))) {
                return;
            }
            try {

                ReAnalyzeResponseDTO result = letterAnalyzeService.analyzeForUpdate(normalizedContent);

                letterEmotionRepository.deleteByLetter(letter);
                letterEmotionRepository.flush();

                String AiSummary = result.getSummary();
                List<Long> emotionIds = result.getEmotions().stream()
                        .map(EmotionDTO::getEmotionId)
                        .toList();

                List<Emotion> emotions = emotionRepository.findAllById(emotionIds);

                List<LetterEmotion> updateEmotions = emotions.stream()
                        .map(emotion -> new LetterEmotion(letter, emotion))
                        .toList();

                letterEmotionRepository.saveAll(updateEmotions);
                letter.updateContent(AiSummary);

            } catch (ExternalApiException e) {
                throw e;
            } catch (Exception e) {
                throw new GeneralException(LetterErrorCode.SUMMARY_INTERNAL_ERROR);
            }
            contentEncryptionSynchronizer.synchronize(letter, userId, normalizedContent);
            searchTokenSynchronizer.replaceTokens(letter, userId, normalizedContent);
        }
    }

    @Override
    @Transactional
    public void deleteLetter(Long userId, Long letterId) {
        Letter letter = getOwnedActiveLetter(userId, letterId);
        letter.softDelete();
        searchTokenSynchronizer.deleteTokens(letter);
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

    @Override
    @Transactional
    public void upsertReply(Long userId, Long letterId, LetterReplyUpsertRequestDTO req) {
        if (req == null || req.getReply() == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter letter = letterRepository
                .findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.LETTER_NOT_FOUND));

        letter.updateReply(req.getReply());
    }

    @Override
    @Transactional
    public void deleteReply(Long userId, Long letterId) {
        Letter letter = letterRepository
                .findByIdAndUser_IdAndIsDeletedFalse(letterId, userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.LETTER_NOT_FOUND));

        letter.deleteReply();
    }

    private Pageable searchPageable(Pageable pageable, String keyword) {
        if (!StringUtils.hasText(keyword)) return pageable;
        return PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
    }

    private Letter getOwnedActiveLetter(Long userId, Long letterId) {
        if (userId == null) {
            throw new GeneralException(LetterErrorCode.UNAUTHORIZED);
        }
        if (letterId == null) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        Letter letter = letterRepository.findByIdAndUser_Id(letterId, userId)
                .orElseThrow(() -> new GeneralException(LetterErrorCode.LETTER_NOT_FOUND));

        if (letter.isDeleted()) {
            throw new GeneralException(LetterErrorCode.DELETED_LETTER);
        }

        return letter;
    }

    @Override
    @Transactional
    public LetterPinResponseDTO updatePinned(Long userId, Long letterId, LetterPinRequestDTO request) {

        Letter letter = getOwnedActiveLetter(userId, letterId);

        letter.updatePinned(request.pinned());

        return new LetterPinResponseDTO(letter.isPinned());
    }
}
