package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "letter-content.encryption",
        name = "enabled",
        havingValue = "true"
)
public class LetterContentBackfillProcessor {

    private final LetterRepository letterRepository;
    private final LetterContentEncryptionSynchronizer encryptionSynchronizer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean encryptIfMissing(Long letterId) {
        Letter letter = letterRepository.findByIdForContentBackfill(letterId)
                .orElse(null);
        if (letter == null) {
            return false;
        }

        encryptionSynchronizer.synchronize(
                letter,
                letter.getUser().getId(),
                letter.getContent()
        );
        return true;
    }
}
