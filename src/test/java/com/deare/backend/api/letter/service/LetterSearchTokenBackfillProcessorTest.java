package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.letter.repository.LetterSearchTokenRepository;
import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.BlindIndexKeyVersion;
import com.deare.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterSearchTokenBackfillProcessorTest {

    private final LetterRepository letterRepository = mock(LetterRepository.class);
    private final LetterSearchTokenRepository searchTokenRepository =
            mock(LetterSearchTokenRepository.class);
    private final BlindIndexKeyProvider keyProvider = mock(BlindIndexKeyProvider.class);
    private final LetterSearchTokenSynchronizer synchronizer =
            mock(LetterSearchTokenSynchronizer.class);
    private final LetterContentReader contentReader = mock(LetterContentReader.class);
    private final LetterSearchTokenBackfillProcessor processor =
            new LetterSearchTokenBackfillProcessor(
                    letterRepository,
                    searchTokenRepository,
                    keyProvider,
                    synchronizer,
                    contentReader
            );

    @Test
    void indexesActiveLetterWhenCurrentVersionIsMissing() {
        Letter letter = mock(Letter.class);
        User user = mock(User.class);
        when(letterRepository.findActiveByIdForSearchTokenBackfill(10L))
                .thenReturn(Optional.of(letter));
        when(letter.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(7L);
        when(contentReader.read(letter)).thenReturn("content");
        when(keyProvider.currentVersion()).thenReturn(new BlindIndexKeyVersion(2));

        assertThat(processor.indexIfMissing(10L, 2)).isTrue();

        verify(synchronizer).indexCreatedLetter(letter, 7L, "content");
    }

    @Test
    void skipsLetterAlreadyIndexedForExpectedVersion() {
        Letter letter = mock(Letter.class);
        when(letterRepository.findActiveByIdForSearchTokenBackfill(10L))
                .thenReturn(Optional.of(letter));
        when(keyProvider.currentVersion()).thenReturn(new BlindIndexKeyVersion(2));
        when(searchTokenRepository.existsByLetter_IdAndIndexKeyVersion(10L, 2))
                .thenReturn(true);

        assertThat(processor.indexIfMissing(10L, 2)).isFalse();

        verify(synchronizer, never()).indexCreatedLetter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void skipsWhenKeyVersionChangedAfterTargetSelection() {
        Letter letter = mock(Letter.class);
        when(letterRepository.findActiveByIdForSearchTokenBackfill(10L))
                .thenReturn(Optional.of(letter));
        when(keyProvider.currentVersion()).thenReturn(new BlindIndexKeyVersion(3));

        assertThat(processor.indexIfMissing(10L, 2)).isFalse();

        verify(searchTokenRepository, never())
                .existsByLetter_IdAndIndexKeyVersion(10L, 2);
        verify(synchronizer, never()).indexCreatedLetter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
