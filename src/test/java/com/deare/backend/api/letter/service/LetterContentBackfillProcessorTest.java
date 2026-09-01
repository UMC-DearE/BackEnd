package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterContentBackfillProcessorTest {

    private final LetterRepository letterRepository = mock(LetterRepository.class);
    private final LetterContentEncryptionSynchronizer synchronizer =
            mock(LetterContentEncryptionSynchronizer.class);
    private final LetterContentBackfillProcessor processor =
            new LetterContentBackfillProcessor(letterRepository, synchronizer);

    @Test
    void encryptsLockedLetterWhenCiphertextIsMissing() {
        Letter letter = mock(Letter.class);
        User user = mock(User.class);
        when(letterRepository.findByIdForContentBackfill(10L))
                .thenReturn(Optional.of(letter));
        when(letter.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(7L);
        when(letter.getContent()).thenReturn("content");

        assertThat(processor.encryptIfMissing(10L)).isTrue();

        verify(synchronizer).synchronize(letter, 7L, "content");
    }

    @Test
    void skipsLetterNoLongerMissingCiphertext() {
        when(letterRepository.findByIdForContentBackfill(10L))
                .thenReturn(Optional.empty());

        assertThat(processor.encryptIfMissing(10L)).isFalse();

        verify(synchronizer, never()).synchronize(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
