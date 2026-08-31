package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.search.LetterSearchBigramGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class LetterSearchResultPager {

    static final int CANDIDATE_BATCH_SIZE = 200;

    private final LetterContentReader contentReader;

    public LetterSearchResultPager(LetterContentReader contentReader) {
        this.contentReader = contentReader;
    }

    public Page<Letter> verifyAndPage(
            Function<Pageable, List<Letter>> candidateBatchFetcher,
            String keyword,
            Pageable requestedPage
    ) {
        String normalizedKeyword = LetterSearchBigramGenerator.normalize(keyword);
        long requestedStart = requestedPage.getOffset();
        long requestedEnd = requestedStart + (long) requestedPage.getPageSize();
        long matchedCount = 0;
        List<Letter> pageContent = new ArrayList<>(
                Math.min(requestedPage.getPageSize(), CANDIDATE_BATCH_SIZE)
        );

        Sort batchSort = stableSort(requestedPage.getSort());
        for (int batchNumber = 0; ; batchNumber++) {
            Pageable batchPage = PageRequest.of(
                    batchNumber,
                    CANDIDATE_BATCH_SIZE,
                    batchSort
            );
            List<Letter> candidates = candidateBatchFetcher.apply(batchPage);

            for (Letter candidate : candidates) {
                if (!matches(candidate, normalizedKeyword)) {
                    continue;
                }
                if (matchedCount >= requestedStart && matchedCount < requestedEnd) {
                    pageContent.add(candidate);
                }
                matchedCount++;
            }

            if (candidates.size() < CANDIDATE_BATCH_SIZE) {
                break;
            }
        }

        return new PageImpl<>(pageContent, requestedPage, matchedCount);
    }

    private Sort stableSort(Sort requestedSort) {
        if (requestedSort.isUnsorted()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        boolean sortsById = requestedSort.stream()
                .anyMatch(order -> order.getProperty().equals("id"));
        return sortsById
                ? requestedSort
                : requestedSort.and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private boolean matches(Letter letter, String normalizedKeyword) {
        return LetterSearchBigramGenerator.normalize(contentReader.read(letter))
                .contains(normalizedKeyword);
    }
}
