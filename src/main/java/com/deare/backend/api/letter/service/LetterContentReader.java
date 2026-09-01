package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
import com.deare.backend.domain.letter.crypto.EncryptedLetterContent;
import com.deare.backend.domain.letter.entity.Letter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class LetterContentReader {

    private final Optional<AesGcmLetterContentCipher> cipher;

    public LetterContentReader(Optional<AesGcmLetterContentCipher> cipher) {
        this.cipher = cipher;
    }

    public String read(Letter letter) {
        Objects.requireNonNull(letter, "Letter is required.");
        Optional<EncryptedLetterContent> encryptedContent = letter.encryptedContent();
        if (cipher.isEmpty() || encryptedContent.isEmpty()) {
            return letter.getContent();
        }
        if (letter.getId() == null || letter.getUser() == null || letter.getUser().getId() == null) {
            throw new IllegalStateException("Persisted owned letter is required for decryption.");
        }
        return cipher.get().decrypt(
                encryptedContent.get(),
                letter.getUser().getId(),
                letter.getId(),
                letter.getContentVersion()
        );
    }
}
