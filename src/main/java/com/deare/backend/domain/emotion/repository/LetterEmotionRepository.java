package com.deare.backend.domain.emotion.repository;

import com.deare.backend.domain.emotion.entity.LetterEmotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LetterEmotionRepository
        extends JpaRepository<LetterEmotion, Long> {

    /**
     * 해당 유저의 모든 letterEmotion 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM LetterEmotion le WHERE le.letter.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
