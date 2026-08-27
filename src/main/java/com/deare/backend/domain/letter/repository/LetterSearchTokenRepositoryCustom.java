package com.deare.backend.domain.letter.repository;

import java.util.List;
import java.util.Set;

public interface LetterSearchTokenRepositoryCustom {

    List<Long> findCandidateLetterIds(
            Long userId,
            int indexKeyVersion,
            Set<String> tokens,
            int limit
    );
}
