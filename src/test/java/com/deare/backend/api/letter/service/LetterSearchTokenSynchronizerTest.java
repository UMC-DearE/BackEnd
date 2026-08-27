package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.entity.LetterSearchToken;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexKeyVersion;
import com.deare.backend.domain.letter.search.VersionedBlindIndexKey;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterSearchTokenSynchronizerTest {

    private final LetterSearchTokenRepository repository = mock(LetterSearchTokenRepository.class);
    private final BlindIndexKeyProvider keyProvider = mock(BlindIndexKeyProvider.class);
    private final Letter letter = mock(Letter.class);

    @Test
    void indexesCreatedLetterWithCurrentKeyVersion() {
        LetterSearchTokenSynchronizer synchronizer = enabledSynchronizer();
        when(keyProvider.currentKey(1L)).thenReturn(versionedKey(2));

        synchronizer.indexCreatedLetter(letter, 1L, "가나다");

        verify(repository).saveAll(argThat(tokens -> {
            List<LetterSearchToken> savedTokens = (List<LetterSearchToken>) tokens;
            return savedTokens.size() == 2
                    && savedTokens.stream().allMatch(token ->
                    token.getLetter() == letter
                            && token.getIndexKeyVersion() == 2
                            && token.getToken().length() == 43
            );
        }));
    }

    @Test
    void replacesTokensByDeletingOldRowsBeforeSavingCurrentVersion() {
        LetterSearchTokenSynchronizer synchronizer = enabledSynchronizer();
        when(letter.getId()).thenReturn(10L);
        when(keyProvider.currentKey(1L)).thenReturn(versionedKey(2));

        synchronizer.replaceTokens(letter, 1L, "가나다");

        InOrder order = inOrder(repository);
        order.verify(repository).deleteAllByLetterId(10L);
        order.verify(repository).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void removesExistingTokensEvenWhenKeyProviderIsDisabled() {
        LetterSearchTokenSynchronizer synchronizer = new LetterSearchTokenSynchronizer(
                repository,
                Optional.empty()
        );
        when(letter.getId()).thenReturn(10L);

        synchronizer.replaceTokens(letter, 1L, "가나다");

        verify(repository).deleteAllByLetterId(10L);
        verify(repository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void deletesTokensForSoftDeletedLetter() {
        LetterSearchTokenSynchronizer synchronizer = enabledSynchronizer();
        when(letter.getId()).thenReturn(10L);

        synchronizer.deleteTokens(letter);

        verify(repository).deleteAllByLetterId(10L);
    }

    @Test
    void storesCompletionMarkerForContentWithoutBigrams() {
        LetterSearchTokenSynchronizer synchronizer = enabledSynchronizer();
        when(keyProvider.currentKey(1L)).thenReturn(versionedKey(2));

        synchronizer.indexCreatedLetter(letter, 1L, "a");

        verify(repository).saveAll(org.mockito.ArgumentMatchers.argThat(tokens -> {
            List<LetterSearchToken> savedTokens = (List<LetterSearchToken>) tokens;
            return savedTokens.size() == 1
                    && savedTokens.get(0).getToken().length() == 43;
        }));
    }

    private LetterSearchTokenSynchronizer enabledSynchronizer() {
        return new LetterSearchTokenSynchronizer(repository, Optional.of(keyProvider));
    }

    private VersionedBlindIndexKey versionedKey(int version) {
        return new VersionedBlindIndexKey(
                new BlindIndexKeyVersion(version),
                new SecretKeySpec(
                        "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8),
                        "HmacSHA256"
                )
        );
    }
}
