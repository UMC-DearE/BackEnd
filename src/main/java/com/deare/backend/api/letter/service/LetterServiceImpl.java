package com.deare.backend.api.letter.service;

import com.deare.backend.api.letter.dto.LetterFromDTO;
import com.deare.backend.api.letter.dto.LetterItemDTO;
import com.deare.backend.api.letter.dto.LetterListResponseDTO;
import com.deare.backend.api.letter.util.ExcerptUtil;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LetterServiceImpl implements LetterService {

    private static final int EXCERPT_MAX_CHARS = 100;

    private final LetterRepository letterRepository;

    public LetterServiceImpl(LetterRepository letterRepository) {
        this.letterRepository = letterRepository;
    }

    @Override
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
