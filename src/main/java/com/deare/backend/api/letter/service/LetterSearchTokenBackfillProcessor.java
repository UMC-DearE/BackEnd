package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "blind-index.keys",
        name = "enabled",
        havingValue = "true"
)
public class LetterSearchTokenBackfillProcessor {

    private final LetterRepository letterRepository;
    private final LetterSearchTokenRepository searchTokenRepository;
    private final BlindIndexKeyProvider keyProvider;
    private final LetterSearchTokenSynchronizer searchTokenSynchronizer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean indexIfMissing(Long letterId, int expectedKeyVersion) {
        Letter letter = letterRepository.findActiveByIdForSearchTokenBackfill(letterId)
                .orElse(null);
        if (letter == null
                || keyProvider.currentVersion().value() != expectedKeyVersion
                || searchTokenRepository.existsByLetter_IdAndIndexKeyVersion(
                letterId,
                expectedKeyVersion
        )) {
            return false;
        }

        searchTokenSynchronizer.indexCreatedLetter(
                letter,
                letter.getUser().getId(),
                letter.getContent()
        );
        return true;
    }
}
