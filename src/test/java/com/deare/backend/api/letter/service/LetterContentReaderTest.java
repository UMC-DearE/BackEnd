package com.deare.backend.api.letter.service;

import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
import com.deare.backend.domain.letter.crypto.EncryptedLetterContent;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LetterContentReaderTest {

    @Test
    void readsEncryptedContentBeforePlaintext() {
        AesGcmLetterContentCipher cipher = cipher();
        Letter letter = letter("stale-plaintext");
        EncryptedLetterContent encrypted = cipher.encrypt("decrypted-content", 7L, 10L, 1);
        letter.storeEncryptedContent(
                encrypted.ciphertext(),
                encrypted.nonce(),
                encrypted.keyVersion(),
                1
        );

        assertThat(new LetterContentReader(Optional.of(cipher)).read(letter))
                .isEqualTo("decrypted-content");
    }

    @Test
    void fallsBackToPlaintextWhenCiphertextIsAbsentOrEncryptionIsDisabled() {
        Letter plaintextOnly = letter("legacy-plaintext");
        assertThat(new LetterContentReader(Optional.of(cipher())).read(plaintextOnly))
                .isEqualTo("legacy-plaintext");

        Letter encrypted = letter("rollback-plaintext");
        EncryptedLetterContent value = cipher().encrypt("encrypted", 7L, 10L, 1);
        encrypted.storeEncryptedContent(
                value.ciphertext(),
                value.nonce(),
                value.keyVersion(),
                1
        );
        assertThat(new LetterContentReader(Optional.empty()).read(encrypted))
                .isEqualTo("rollback-plaintext");
    }

    @Test
    void doesNotFallBackToPlaintextWhenEncryptedContentCannotBeAuthenticated() {
        AesGcmLetterContentCipher cipher = cipher();
        Letter letter = letter("must-not-be-returned");
        EncryptedLetterContent encrypted = cipher.encrypt("secret", 7L, 10L, 1);
        String tampered = (encrypted.ciphertext().startsWith("A") ? "B" : "A")
                + encrypted.ciphertext().substring(1);
        letter.storeEncryptedContent(tampered, encrypted.nonce(), encrypted.keyVersion(), 1);

        assertThatThrownBy(() -> new LetterContentReader(Optional.of(cipher)).read(letter))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Letter content decryption failed.");
    }

    @Test
    void rejectsPartialEncryptedContentState() {
        Letter letter = letter("must-not-be-returned");
        ReflectionTestUtils.setField(letter, "contentCiphertext", "ciphertext");

        assertThatThrownBy(() -> new LetterContentReader(Optional.empty()).read(letter))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid encrypted letter content state.");
    }

    private Letter letter(String plaintext) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        Letter letter = new Letter(
                plaintext,
                LocalDate.of(2026, 8, 28),
                "summary",
                1,
                user,
                mock(From.class),
                null
        );
        ReflectionTestUtils.setField(letter, "id", 10L);
        return letter;
    }

    private AesGcmLetterContentCipher cipher() {
        return new AesGcmLetterContentCipher(
                1,
                Map.of(1, new SecretKeySpec(new byte[32], "AES"))
        );
    }
}
