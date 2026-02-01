package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LetterRepositoryCustom {
    Page<Letter> findLettersForList(
            Long userId,
            Long folderId,
            Long fromId,
            String keyword,
            Pageable pageable
    );
}
