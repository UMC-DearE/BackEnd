package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.exception.LetterErrorCode;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexTokenGenerator;
import com.deare.backend.domain.letter.search.VersionedBlindIndexKey;
import com.deare.backend.global.common.exception.GeneralException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class LetterSearchCandidateResolver {

    private static final int MAX_SEARCH_TOKENS = 100;
    private static final int MAX_SEARCH_KEYWORD_CODE_POINTS = MAX_SEARCH_TOKENS + 1;
    private static final int MAX_CANDIDATE_LETTERS = 1_000;

    private final LetterSearchTokenRepository searchTokenRepository;
    private final Optional<BlindIndexKeyProvider> keyProvider;

    public LetterSearchCandidateResolver(
            LetterSearchTokenRepository searchTokenRepository,
            Optional<BlindIndexKeyProvider> keyProvider
    ) {
        this.searchTokenRepository = searchTokenRepository;
        this.keyProvider = keyProvider;
    }

    public Optional<Set<Long>> resolve(Long userId, String keyword) {
        if (keyword != null
                && keyword.codePointCount(0, keyword.length()) > MAX_SEARCH_KEYWORD_CODE_POINTS) {
            throw new GeneralException(LetterErrorCode.INVALID_REQUEST);
        }

        if (userId == null || keyProvider.isEmpty()) {
            return Optional.empty();
        }

        List<VersionedBlindIndexKey> readableKeys = keyProvider.get().readableKeys(userId);
        if (readableKeys.isEmpty()) {
            return Optional.empty();
        }

        Set<Long> candidateIds = new LinkedHashSet<>();
        for (VersionedBlindIndexKey readableKey : readableKeys) {
            Set<String> tokens = new LinkedHashSet<>(
                    new BlindIndexTokenGenerator(readableKey.key()).generateUnique(keyword)
            );
            if (tokens.isEmpty() || tokens.size() > MAX_SEARCH_TOKENS) {
                return Optional.empty();
            }

            candidateIds.addAll(searchTokenRepository.findCandidateLetterIds(
                    userId,
                    readableKey.version().value(),
                    tokens,
                    MAX_CANDIDATE_LETTERS + 1
            ));
            if (candidateIds.size() > MAX_CANDIDATE_LETTERS) {
                return Optional.empty();
            }
        }

        return Optional.of(Set.copyOf(candidateIds));
    }
}
