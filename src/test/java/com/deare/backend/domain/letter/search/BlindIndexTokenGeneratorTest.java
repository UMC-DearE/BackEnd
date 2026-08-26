package com.deare.backend.domain.letter.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlindIndexTokenGeneratorTest {

    private static final SecretKey KEY = key("0123456789abcdef0123456789abcdef");

    @Test
    @DisplayName("같은 키와 검색 문자열은 항상 같은 토큰을 생성한다")
    void generateDeterministicTokens() {
        BlindIndexTokenGenerator generator = new BlindIndexTokenGenerator(KEY);

        List<String> first = generator.generateUnique("가나다");
        List<String> second = generator.generateUnique("가나다");

        assertThat(first).containsExactlyElementsOf(second);
        assertThat(first).hasSize(2).allSatisfy(token -> {
            assertThat(token).hasSize(43);
            assertThat(token).matches("[A-Za-z0-9_-]+");
        });
    }

    @Test
    @DisplayName("HMAC 알고리즘과 토큰 인코딩의 고정 벡터를 유지한다")
    void matchStableTokenVector() {
        BlindIndexTokenGenerator generator = new BlindIndexTokenGenerator(KEY);

        assertThat(generator.generateUnique("가나"))
                .containsExactly("F4kgG4njqEbcQ44SOgPWuT1BkKcGbnldJVjMpmHspFE");
    }

    @Test
    @DisplayName("정규화 결과가 같은 검색 문자열은 같은 토큰을 생성한다")
    void generateSameTokensForEquivalentNormalizedText() {
        BlindIndexTokenGenerator generator = new BlindIndexTokenGenerator(KEY);

        assertThat(generator.generateUnique("  ＡBc  "))
                .containsExactlyElementsOf(generator.generateUnique("abc"));
    }

    @Test
    @DisplayName("서로 다른 키는 같은 검색 문자열에 다른 토큰을 생성한다")
    void separateTokensByKey() {
        BlindIndexTokenGenerator first = new BlindIndexTokenGenerator(KEY);
        BlindIndexTokenGenerator second = new BlindIndexTokenGenerator(
                key("fedcba9876543210fedcba9876543210")
        );

        assertThat(first.generateUnique("가나다"))
                .doesNotContainAnyElementsOf(second.generateUnique("가나다"));
    }

    @Test
    @DisplayName("검색할 수 없는 문자열은 토큰을 생성하지 않는다")
    void returnEmptyForUnsearchableText() {
        BlindIndexTokenGenerator generator = new BlindIndexTokenGenerator(KEY);

        assertThat(generator.generateUnique(null)).isEmpty();
        assertThat(generator.generateUnique("가")).isEmpty();
    }

    @Test
    @DisplayName("HMAC에 사용할 수 없는 키는 생성 시점에 거부한다")
    void rejectInvalidKey() {
        SecretKey invalidKey = new InvalidSecretKey();

        assertThatThrownBy(() -> new BlindIndexTokenGenerator(invalidKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid blind index key.");
    }

    @Test
    @DisplayName("동일한 생성기를 동시에 호출해도 토큰 결과가 섞이지 않는다")
    void generateTokensConcurrently() throws Exception {
        BlindIndexTokenGenerator generator = new BlindIndexTokenGenerator(KEY);
        List<String> expected = generator.generateUnique("가나다라마바사");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<List<String>>> tasks = java.util.stream.IntStream.range(0, 20)
                    .mapToObj(ignored -> (Callable<List<String>>) () -> generator.generateUnique("가나다라마바사"))
                    .toList();

            assertThat(executor.invokeAll(tasks))
                    .allSatisfy(future -> assertThat(future.get()).containsExactlyElementsOf(expected));
        } finally {
            executor.shutdownNow();
        }
    }

    private static SecretKey key(String value) {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private static final class InvalidSecretKey implements SecretKey {

        @Override
        public String getAlgorithm() {
            return "HmacSHA256";
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

        @Override
        public byte[] getEncoded() {
            return null;
        }
    }
}
