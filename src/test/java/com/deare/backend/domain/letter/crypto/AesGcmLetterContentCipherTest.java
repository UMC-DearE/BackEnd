package com.deare.backend.domain.letter.crypto;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesGcmLetterContentCipherTest {

    @Test
    void encryptsAndDecryptsWithBoundLetterContext() {
        AesGcmLetterContentCipher cipher = cipher(2, Map.of(2, key(2)));

        EncryptedLetterContent encrypted = cipher.encrypt("소중한 편지 본문", 10L, 20L, 3);

        assertThat(encrypted.keyVersion()).isEqualTo(2);
        assertThat(encrypted.nonce()).hasSize(16);
        assertThat(encrypted.ciphertext()).doesNotContain("소중한 편지 본문");
        assertThat(cipher.decrypt(encrypted, 10L, 20L, 3))
                .isEqualTo("소중한 편지 본문");
    }

    @Test
    void usesFreshNonceForEveryEncryption() {
        AesGcmLetterContentCipher cipher = cipher(1, Map.of(1, key(1)));

        EncryptedLetterContent first = cipher.encrypt("same", 10L, 20L, 1);
        EncryptedLetterContent second = cipher.encrypt("same", 10L, 20L, 1);

        assertThat(first.nonce()).isNotEqualTo(second.nonce());
        assertThat(first.ciphertext()).isNotEqualTo(second.ciphertext());
    }

    @Test
    void decryptsContentWrittenWithReadablePreviousKey() {
        EncryptedLetterContent oldContent = cipher(1, Map.of(1, key(1)))
                .encrypt("old", 10L, 20L, 1);
        AesGcmLetterContentCipher rotated = cipher(2, Map.of(1, key(1), 2, key(2)));

        assertThat(rotated.decrypt(oldContent, 10L, 20L, 1)).isEqualTo("old");
        assertThat(rotated.encrypt("new", 10L, 20L, 2).keyVersion()).isEqualTo(2);
    }

    @Test
    void rejectsTamperingAndMismatchedContextWithoutLeakingPlaintext() {
        AesGcmLetterContentCipher cipher = cipher(1, Map.of(1, key(1)));
        EncryptedLetterContent encrypted = cipher.encrypt("secret-content", 10L, 20L, 1);
        byte[] tamperedBytes = Base64.getUrlDecoder().decode(encrypted.ciphertext());
        tamperedBytes[0] ^= 1;
        EncryptedLetterContent tampered = new EncryptedLetterContent(
                encrypted.keyVersion(),
                encrypted.nonce(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedBytes)
        );

        assertDecryptionFails(() -> cipher.decrypt(tampered, 10L, 20L, 1));
        assertDecryptionFails(() -> cipher.decrypt(encrypted, 11L, 20L, 1));
        assertDecryptionFails(() -> cipher.decrypt(encrypted, 10L, 21L, 1));
        assertDecryptionFails(() -> cipher.decrypt(encrypted, 10L, 20L, 2));
    }

    @Test
    void rejectsInvalidKeyConfigurationAndUnknownKeyVersion() {
        assertThatThrownBy(() -> cipher(0, Map.of(1, key(1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cipher(2, Map.of(1, key(1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cipher(
                1,
                Map.of(1, new SecretKeySpec(new byte[16], "AES"))
        )).isInstanceOf(IllegalArgumentException.class);

        AesGcmLetterContentCipher cipher = cipher(1, Map.of(1, key(1)));
        EncryptedLetterContent unknownVersion =
                new EncryptedLetterContent(2, "AAAAAAAAAAAAAAAA", "AA");
        assertDecryptionFails(() -> cipher.decrypt(unknownVersion, 10L, 20L, 1));
    }

    private AesGcmLetterContentCipher cipher(
            int currentVersion,
            Map<Integer, SecretKey> keys
    ) {
        return new AesGcmLetterContentCipher(currentVersion, keys);
    }

    private SecretKey key(int seed) {
        byte[] bytes = new byte[32];
        Arrays.fill(bytes, (byte) seed);
        return new SecretKeySpec(bytes, "AES");
    }

    private void assertDecryptionFails(ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Letter content decryption failed.")
                .hasMessageNotContaining("secret-content");
    }
}
