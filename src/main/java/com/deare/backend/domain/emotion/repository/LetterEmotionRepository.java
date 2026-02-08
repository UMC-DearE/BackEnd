package com.deare.backend.domain.emotion.repository;

import com.deare.backend.domain.emotion.entity.LetterEmotion;
import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LetterEmotionRepository
        extends JpaRepository<LetterEmotion, Long> {
    void deleteByLetter(Letter letter);
}
