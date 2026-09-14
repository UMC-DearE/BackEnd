package com.deare.backend.api.letter.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LetterContentChangeRateCalculatorTest {

    @Test
    void returnsZeroWhenContentIsIdentical() {
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate(
                "오늘 하루도 수고했어", "오늘 하루도 수고했어");

        assertThat(rate).isEqualTo(0.0);
    }

    @Test
    void returnsOneWhenContentIsCompletelyReplaced() {
        // 원문/수정문 단어 수가 같고 공통 단어가 하나도 없는 경우 -> 100%
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate(
                "하나 둘 셋 넷 다섯", "여섯 일곱 여덟 아홉 열");

        assertThat(rate).isEqualTo(1.0);
    }

    @Test
    void calculatesPartialChangeRateByWordCount() {
        // 원문 5단어 중 1단어만 교체 -> 20%
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate(
                "하나 둘 셋 넷 다섯", "하나 둘 셋 넷 여섯");

        assertThat(rate).isCloseTo(0.2, within(1e-9));
    }

    @Test
    void treatsAppendedWordsAsChange() {
        // 원문 3단어에 2단어 추가 -> 2/3
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate(
                "하나 둘 셋", "하나 둘 셋 넷 다섯");

        assertThat(rate).isCloseTo(2.0 / 3.0, within(1e-9));
    }

    @Test
    void countsDeletionAndInsertionAtDifferentPositionsSeparately() {
        // 앞에서 1단어 삭제 + 뒤에서 2단어 삽입이 동시에 일어나는 경우.
        // LCS 길이(2)만 빼는 근사식이면 2/3으로 과소평가되지만,
        // 실제 편집거리(삭제1+삽입2=3)를 기준으로 하면 3/3 = 100%여야 한다.
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate(
                "하나 둘 셋", "둘 셋 넷 다섯");

        assertThat(rate).isEqualTo(1.0);
    }

    @Test
    void returnsZeroWhenBothContentsAreBlank() {
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate("", "   ");

        assertThat(rate).isEqualTo(0.0);
    }

    @Test
    void returnsOneWhenOldContentIsBlankButNewIsNot() {
        double rate = LetterContentChangeRateCalculator.calculateWordChangeRate("", "새로운 내용");

        assertThat(rate).isEqualTo(1.0);
    }
}
