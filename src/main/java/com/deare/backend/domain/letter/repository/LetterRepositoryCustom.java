package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LetterRepositoryCustom {
    Page<Letter> findLettersForList(
            Long userId,
            Long folderId,
            Long fromId,
            Boolean isLiked,
            String keyword,
            Pageable pageable
    );

    Optional<Letter> findLetterDetailById(Long userId, Long letterId);

    Optional<Letter> findRandomLetterByUser(Long userId, long offset);

    long countVisibleLettersByUser(Long userId);

    Optional<Letter> findPinnedLetterByUser(Long userId);
}
