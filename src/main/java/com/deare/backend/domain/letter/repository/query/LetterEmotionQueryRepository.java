package com.deare.backend.domain.letter.repository.query;

import com.deare.backend.api.letter.dto.EmotionTagDTO;

import java.util.List;

public interface LetterEmotionQueryRepository {

    List<EmotionTagDTO> findEmotionTagsByLetterId(Long letterId);
}
