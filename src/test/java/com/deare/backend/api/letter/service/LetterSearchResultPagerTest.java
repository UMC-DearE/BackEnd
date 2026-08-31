package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LetterSearchResultPagerTest {

    private final LetterContentReader contentReader = mock(LetterContentReader.class);
    private final LetterSearchResultPager pager = new LetterSearchResultPager(contentReader);

    @Test
    void removesFalsePositivesBeforeApplyingRequestedPage() {
        Letter first = letter("first needle");
        Letter falsePositive = letter("different content");
        Letter second = letter("second needle");

        Page<Letter> result = pager.verifyAndPage(
                ignored -> List.of(first, falsePositive, second),
                "NEEDLE",
                PageRequest.of(1, 1)
        );

        assertThat(result.getContent()).containsExactly(second);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void fetchesCandidatesInBoundedBatchesAndKeepsOnlyRequestedPage() {
        List<Letter> candidates = new ArrayList<>();
        for (int i = 0; i < LetterSearchResultPager.CANDIDATE_BATCH_SIZE + 1; i++) {
            candidates.add(letter("needle " + i));
        }
        List<Pageable> requestedBatches = new ArrayList<>();

        Page<Letter> result = pager.verifyAndPage(batch -> {
            requestedBatches.add(batch);
            int from = (int) batch.getOffset();
            int to = Math.min(from + batch.getPageSize(), candidates.size());
            return from >= candidates.size() ? List.of() : candidates.subList(from, to);
        }, "needle", PageRequest.of(1, 1));

        assertThat(requestedBatches).hasSize(2);
        assertThat(requestedBatches)
                .allSatisfy(batch -> assertThat(batch.getPageSize())
                        .isEqualTo(LetterSearchResultPager.CANDIDATE_BATCH_SIZE));
        assertThat(result.getContent()).containsExactly(candidates.get(1));
        assertThat(result.getTotalElements()).isEqualTo(candidates.size());
        assertThat(requestedBatches.get(0).getSort())
                .containsExactly(Sort.Order.desc("id"));
    }

    @Test
    void avoidsOverflowWhenPageSizeIsIntegerMaxValue() {
        Letter match = letter("needle");
        AtomicInteger fetchCount = new AtomicInteger();

        Page<Letter> result = pager.verifyAndPage(
                ignored -> fetchCount.getAndIncrement() == 0 ? List.of(match) : List.of(),
                "needle",
                PageRequest.of(1, Integer.MAX_VALUE)
        );

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Letter letter(String content) {
        Letter letter = mock(Letter.class);
        when(contentReader.read(letter)).thenReturn(content);
        return letter;
    }
}
