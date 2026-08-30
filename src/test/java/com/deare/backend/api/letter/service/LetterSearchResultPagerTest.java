package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LetterSearchResultPagerTest {

    private final LetterContentReader contentReader = mock(LetterContentReader.class);
    private final LetterSearchResultPager pager = new LetterSearchResultPager(contentReader);

    @Test
    void removesBlindIndexFalsePositivesAndCalculatesPageAfterVerification() {
        Letter first = mock(Letter.class);
        Letter falsePositive = mock(Letter.class);
        Letter second = mock(Letter.class);
        when(contentReader.read(first)).thenReturn("첫 번째 NEEDLE 편지");
        when(contentReader.read(falsePositive)).thenReturn("일치하지 않는 본문");
        when(contentReader.read(second)).thenReturn("두 번째 needle 편지");

        PageRequest requestedPage = PageRequest.of(1, 1);
        Page<Letter> result = pager.verifyAndPage(
                new PageImpl<>(List.of(first, falsePositive, second)),
                "ＮＥＥＤＬＥ",
                requestedPage
        );

        assertThat(result.getContent()).containsExactly(second);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void keepsRepositoryPageWhenKeywordIsBlank() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Letter> repositoryPage = Page.empty(pageable);

        assertThat(pager.verifyAndPage(repositoryPage, " ", pageable))
                .isSameAs(repositoryPage);
    }
}
