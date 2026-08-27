package com.deare.backend.domain.letter.search;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HkdfBlindIndexKeyProviderTest {

    private static final SecretKey ROOT_KEY_V1 = key("0123456789abcdef0123456789abcdef");
    private static final SecretKey ROOT_KEY_V2 = key("fedcba9876543210fedcba9876543210");

    @Test
    void derivesDeterministicKeyForSameUserAndVersion() {
        HkdfBlindIndexKeyProvider provider = provider();

        assertThat(provider.currentKey(1L).key().getEncoded())
                .containsExactly(provider.currentKey(1L).key().getEncoded());
    }

    @Test
    void matchesStableDerivedKeyVector() {
        HkdfBlindIndexKeyProvider provider = provider();

        assertThat(Base64.getEncoder().encodeToString(provider.currentKey(1L).key().getEncoded()))
                .isEqualTo("8CD1664IDCldcF8FINEYD3p/wQ6Qq7nn8+k6zzvhUy4=");
    }

    @Test
    void separatesDerivedKeysByUserAndVersion() {
        HkdfBlindIndexKeyProvider provider = provider();

        byte[] userOneCurrent = provider.currentKey(1L).key().getEncoded();
        byte[] userTwoCurrent = provider.currentKey(2L).key().getEncoded();
        byte[] userOnePrevious = provider.readableKeys(1L).get(1).key().getEncoded();

        assertThat(userOneCurrent).isNotEqualTo(userTwoCurrent);
        assertThat(userOneCurrent).isNotEqualTo(userOnePrevious);
    }

    @Test
    void returnsCurrentVersionFirstAndAllReadableVersions() {
        HkdfBlindIndexKeyProvider provider = provider();

        assertThat(provider.currentKey(1L).version()).isEqualTo(new BlindIndexKeyVersion(2));
        assertThat(provider.readableKeys(1L))
                .extracting(key -> key.version().value())
                .containsExactly(2, 1);
    }

    @Test
    void derivedKeyProducesUserScopedBlindIndexTokens() {
        HkdfBlindIndexKeyProvider provider = provider();
        BlindIndexTokenGenerator userOne = new BlindIndexTokenGenerator(provider.currentKey(1L).key());
        BlindIndexTokenGenerator userTwo = new BlindIndexTokenGenerator(provider.currentKey(2L).key());

        assertThat(userOne.generateUnique("가나다"))
                .doesNotContainAnyElementsOf(userTwo.generateUnique("가나다"));
    }

    @Test
    void rejectsInvalidUserAndKeyRingConfiguration() {
        HkdfBlindIndexKeyProvider provider = provider();

        assertThatThrownBy(() -> provider.currentKey(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must be positive.");
        assertThatThrownBy(() -> new HkdfBlindIndexKeyProvider(
                2,
                List.of(1, 2),
                Map.of(1, ROOT_KEY_V1, 2, ROOT_KEY_V2)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current blind index key version must be the first readable version.");
        assertThatThrownBy(() -> new HkdfBlindIndexKeyProvider(
                2,
                List.of(2, 1),
                Map.of(2, ROOT_KEY_V2)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Every readable blind index key version requires a root key.");
        assertThatThrownBy(() -> new HkdfBlindIndexKeyProvider(
                1,
                List.of(1),
                Map.of(1, key("too-short"))
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Blind index root key must contain at least 256 bits.");
    }

    @Test
    void redactsKeyFromStringRepresentation() {
        VersionedBlindIndexKey key = provider().currentKey(1L);

        assertThat(key.toString())
                .isEqualTo("VersionedBlindIndexKey[version=2, key=REDACTED]")
                .doesNotContain(java.util.Base64.getEncoder().encodeToString(key.key().getEncoded()));
    }

    private HkdfBlindIndexKeyProvider provider() {
        return new HkdfBlindIndexKeyProvider(
                2,
                List.of(2, 1),
                Map.of(1, ROOT_KEY_V1, 2, ROOT_KEY_V2)
        );
    }

    private static SecretKey key(String value) {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
