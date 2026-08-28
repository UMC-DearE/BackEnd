package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.LetterSearchToken;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexTokenGenerator;
import com.deare.backend.domain.letter.search.VersionedBlindIndexKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LetterSearchTokenSynchronizer {

    private final LetterSearchTokenRepository searchTokenRepository;
    private final Optional<BlindIndexKeyProvider> keyProvider;

    public LetterSearchTokenSynchronizer(
            LetterSearchTokenRepository searchTokenRepository,
            Optional<BlindIndexKeyProvider> keyProvider
    ) {
        this.searchTokenRepository = searchTokenRepository;
        this.keyProvider = keyProvider;
    }

    public void indexCreatedLetter(Letter letter, long userId, String content) {
        keyProvider.ifPresent(provider -> saveCurrentTokens(letter, userId, content, provider));
    }

    public void replaceTokens(Letter letter, long userId, String content) {
        searchTokenRepository.deleteAllByLetterId(letter.getId());
        indexCreatedLetter(letter, userId, content);
    }

    public void deleteTokens(Letter letter) {
        searchTokenRepository.deleteAllByLetterId(letter.getId());
    }

    private void saveCurrentTokens(
            Letter letter,
            long userId,
            String content,
            BlindIndexKeyProvider provider
    ) {
        VersionedBlindIndexKey currentKey = provider.currentKey(userId);
        BlindIndexTokenGenerator tokenGenerator = new BlindIndexTokenGenerator(currentKey.key());
        List<LetterSearchToken> tokens = tokenGenerator.generateForIndex(content).stream()
                .map(token -> LetterSearchToken.create(
                        letter,
                        currentKey.version().value(),
                        token
                ))
                .toList();
        searchTokenRepository.saveAll(tokens);
    }
}
