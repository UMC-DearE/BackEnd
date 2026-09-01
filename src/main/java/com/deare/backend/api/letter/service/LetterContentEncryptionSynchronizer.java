package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
import com.deare.backend.domain.letter.crypto.EncryptedLetterContent;
import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LetterContentEncryptionSynchronizer {

    private static final int FORMAT_VERSION = 1;

    private final Optional<AesGcmLetterContentCipher> cipher;

    public LetterContentEncryptionSynchronizer(Optional<AesGcmLetterContentCipher> cipher) {
        this.cipher = cipher;
    }

    public void synchronize(Letter letter, long userId, String plaintext) {
        if (cipher.isEmpty()) {
            throw new IllegalStateException("Letter content encryption is required.");
        }
        if (letter.getId() == null) {
            throw new IllegalStateException("Persisted letter is required for content encryption.");
        }

        EncryptedLetterContent encrypted = cipher.get().encrypt(
                plaintext,
                userId,
                letter.getId(),
                letter.getContentVersion()
        );
        letter.storeEncryptedContent(
                encrypted.ciphertext(),
                encrypted.nonce(),
                encrypted.keyVersion(),
                FORMAT_VERSION
        );
    }
}
