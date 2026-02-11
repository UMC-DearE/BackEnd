package com.deare.backend.domain.letter.repository.query;

import com.deare.backend.domain.letter.repository.query.dto.EmotionTagProjection;

import java.util.List;

public interface LetterEmotionQueryRepository {

    List<EmotionTagProjection> findEmotionTagsByLetterId(Long letterId);
}
