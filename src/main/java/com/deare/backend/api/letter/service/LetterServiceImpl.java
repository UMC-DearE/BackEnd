package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.*;
import com.deare.backend.api.letter.util.ExcerptUtil;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.query.LetterEmotionQueryRepository;
import com.deare.backend.global.common.exception.GeneralException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LetterServiceImpl implements LetterService {

    private static final int EXCERPT_MAX_CHARS = 100;

    private final LetterRepository letterRepository;
    private final LetterEmotionQueryRepository letterEmotionQueryRepository;

    public LetterServiceImpl(
            LetterRepository letterRepository,
            LetterEmotionQueryRepository letterEmotionQueryRepository
    ) {
        this.letterRepository = letterRepository;
        this.letterEmotionQueryRepository = letterEmotionQueryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LetterListResponseDTO getLetterList(
            Long userId,
            Pageable pageable,
            Long folderId,
            Long fromId,
            String keyword
    ) {

        Page<Letter> page = letterRepository.findLettersForList(
                userId,
                folderId,
                fromId,
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
                        new GeneralException(LetterErrorCode.LETTER_NOT_FOUND)
                );

        List<EmotionTagDTO> emotionTags =
                letterEmotionQueryRepository.findEmotionTagsByLetterId(letterId);

        List<String> imageUrls = letter.getLetterImages().stream()
                .map(li -> li.getImage().getImageUrl())
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

    private LetterItemDTO toItemDTO(Letter letter) {
        return new LetterItemDTO(
                letter.getId(),
                ExcerptUtil.excerptByChars(letter.getContent(), EXCERPT_MAX_CHARS),
                letter.isLiked(),
                letter.getReceivedAt() != null ? letter.getReceivedAt() : null,
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
