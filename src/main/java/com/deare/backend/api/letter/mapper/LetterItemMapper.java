package com.deare.backend.api.letter.mapper;

import com.deare.backend.api.letter.dto.result.LetterFromDTO;
import com.deare.backend.api.letter.dto.result.LetterItemDTO;
import com.deare.backend.api.letter.util.ExcerptUtil;
import com.deare.backend.domain.letter.entity.Letter;

public final class LetterItemMapper {

    private static final int EXCERPT_MAX_CHARS = 100;

    private LetterItemMapper() {
    }

    public static LetterItemDTO toItemDTO(Letter letter) {
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
