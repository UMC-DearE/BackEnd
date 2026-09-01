package com.deare.backend.domain.letter.crypto;

import java.util.Objects;

public record EncryptedLetterContent(int keyVersion, String nonce, String ciphertext) {
    public EncryptedLetterContent {
        if (keyVersion <= 0) {
            throw new IllegalArgumentException("Encryption key version must be positive.");
        }
        Objects.requireNonNull(nonce, "Encryption nonce is required.");
        Objects.requireNonNull(ciphertext, "Encrypted content is required.");
    }
}
