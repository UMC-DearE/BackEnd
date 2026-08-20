package com.deare.backend.domain.letter.repository;

import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
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

    Optional<Letter> findRandomLetterByUser(Long userId, long offset, LocalDateTime createdBefore);

    long countVisibleLettersByUser(Long userId, LocalDateTime createdBefore);

    Optional<Letter> findPinnedLetterByUser(Long userId);

    long countVisibleLettersByUserSince(Long userId, LocalDateTime since);

    List<String> findAiSummariesByUser(Long userId, int limit);
}
