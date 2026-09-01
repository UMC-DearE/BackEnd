package com.deare.backend.domain.letter.entity;

import com.deare.backend.domain.from.entity.From;
import com.deare.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class LetterEncryptionStateTest {

    @Test
    void storesEncryptedContentAsOneState() {
        Letter letter = letter();

        letter.storeEncryptedContent("ciphertext", "AAAAAAAAAAAAAAAA", 2, 1);

        assertThat(letter.getContentCiphertext()).isEqualTo("ciphertext");
        assertThat(letter.getContentEncryptionNonce()).isEqualTo("AAAAAAAAAAAAAAAA");
        assertThat(letter.getContentEncryptionKeyVersion()).isEqualTo(2);
        assertThat(letter.getContentEncryptionFormatVersion()).isEqualTo(1);

    }

    @Test
    void rejectsPartialOrUnsupportedEncryptedContentState() {
        Letter letter = letter();

        assertThatThrownBy(() -> letter.storeEncryptedContent("", "AAAAAAAAAAAAAAAA", 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> letter.storeEncryptedContent("ciphertext", "short", 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> letter.storeEncryptedContent(
                "ciphertext",
                "AAAAAAAAAAAAAAAA",
                1,
                2
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private Letter letter() {
        return new Letter(
                LocalDate.of(2026, 8, 28),
                "summary",
                1,
                mock(User.class),
                mock(From.class),
                null
        );
    }
}
