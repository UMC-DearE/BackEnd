package com.deare.backend.api.letter.service;

import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.search.LetterSearchBigramGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class LetterSearchResultPager {
    private final LetterContentReader contentReader;

    public LetterSearchResultPager(LetterContentReader contentReader) {
        this.contentReader = contentReader;
    }

    public Page<Letter> verifyAndPage(Page<Letter> candidates, String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) return candidates;

        String normalizedKeyword = LetterSearchBigramGenerator.normalize(keyword);
        List<Letter> matches = candidates.getContent().stream()
                .filter(letter -> LetterSearchBigramGenerator.normalize(contentReader.read(letter))
                        .contains(normalizedKeyword))
                .toList();
        int fromIndex = Math.toIntExact(Math.min(pageable.getOffset(), matches.size()));
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), matches.size());
        return new PageImpl<>(matches.subList(fromIndex, toIndex), pageable, matches.size());
    }
}
