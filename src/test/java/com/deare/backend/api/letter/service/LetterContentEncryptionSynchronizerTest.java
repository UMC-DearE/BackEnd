package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
import com.deare.backend.domain.letter.crypto.EncryptedLetterContent;
import com.deare.backend.domain.letter.entity.Letter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterContentEncryptionSynchronizerTest {

    private final AesGcmLetterContentCipher cipher = mock(AesGcmLetterContentCipher.class);
    private final Letter letter = mock(Letter.class);

    @Test
    void storesCurrentEncryptedContentForPersistedLetter() {
        LetterContentEncryptionSynchronizer synchronizer =
                new LetterContentEncryptionSynchronizer(Optional.of(cipher));
        when(letter.getId()).thenReturn(20L);
        when(letter.getContentVersion()).thenReturn(3);
        when(cipher.encrypt("content", 10L, 20L, 3))
                .thenReturn(new EncryptedLetterContent(2, "AAAAAAAAAAAAAAAA", "ciphertext"));

        synchronizer.synchronize(letter, 10L, "content");

        verify(letter).storeEncryptedContent("ciphertext", "AAAAAAAAAAAAAAAA", 2, 1);
    }

    @Test
    void clearsStaleEncryptedContentWhenEncryptionIsDisabled() {
        LetterContentEncryptionSynchronizer synchronizer =
                new LetterContentEncryptionSynchronizer(Optional.empty());

        synchronizer.synchronize(letter, 10L, "content");

        verify(letter).clearEncryptedContent();
        verify(cipher, never()).encrypt("content", 10L, 20L, 3);
    }

    @Test
    void rejectsTransientLetterBeforeEncryption() {
        LetterContentEncryptionSynchronizer synchronizer =
                new LetterContentEncryptionSynchronizer(Optional.of(cipher));
        when(letter.getId()).thenReturn(null);

        assertThatThrownBy(() -> synchronizer.synchronize(letter, 10L, "content"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Persisted letter is required for content encryption.");
    }
}
