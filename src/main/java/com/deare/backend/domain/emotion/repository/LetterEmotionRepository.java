package com.deare.backend.domain.emotion.repository;

import com.deare.backend.domain.emotion.entity.LetterEmotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LetterEmotionRepository
        extends JpaRepository<LetterEmotion, Long> {
}
