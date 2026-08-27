package com.deare.backend.domain.letter.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LetterSearchBigramGeneratorTest {

    @Test
    @DisplayName("검색 문자열을 NFKC와 소문자 기준으로 정규화한다")
    void normalizeCompatibilityCharactersAndCase() {
        assertThat(LetterSearchBigramGenerator.normalize("  ＡBc  ")).isEqualTo("abc");
    }

    @Test
    @DisplayName("연속된 Unicode 공백과 개행을 하나의 공백으로 정규화한다")
    void normalizeWhitespace() {
        assertThat(LetterSearchBigramGenerator.normalize("가\t \n나\u2003다"))
                .isEqualTo("가 나 다");
    }

    @Test
    @DisplayName("정규화된 문자열에서 순서대로 2-gram을 생성한다")
    void generateKoreanBigrams() {
        assertThat(LetterSearchBigramGenerator.generateUnique("가나다"))
                .containsExactly("가나", "나다");
    }

    @Test
    @DisplayName("UTF-16 surrogate pair를 분리하지 않고 code point 기준으로 생성한다")
    void generateBigramsByCodePoint() {
        assertThat(LetterSearchBigramGenerator.generateUnique("가😀나"))
                .containsExactly("가😀", "😀나");
    }

    @Test
    @DisplayName("반복되는 2-gram은 최초 순서를 유지하며 한 번만 생성한다")
    void removeDuplicateBigrams() {
        assertThat(LetterSearchBigramGenerator.generateUnique("aaaa"))
                .containsExactly("aa");
    }

    @Test
    @DisplayName("null, 공백 또는 한 글자는 검색 가능한 2-gram을 생성하지 않는다")
    void returnEmptyWhenTextIsNotSearchable() {
        assertThat(LetterSearchBigramGenerator.generateUnique(null)).isEmpty();
        assertThat(LetterSearchBigramGenerator.generateUnique(" \n\t ")).isEmpty();
        assertThat(LetterSearchBigramGenerator.generateUnique("가")).isEmpty();
        assertThat(LetterSearchBigramGenerator.generateUnique("😀")).isEmpty();
    }
}
