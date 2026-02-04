package com.deare.backend.api.auth.dto.response;

import com.deare.backend.domain.term.entity.Term;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회원가입용 약관 조회 응답 DTO
 */
public record TermResponseDTO(
        List<TermItemDTO> terms
) {
    public record TermItemDTO(
            Long termId,
            String title,
            String type,
            String content,
            boolean isRequired,
            LocalDateTime effectiveAt,
            String version
    ) {
        public static TermItemDTO from(Term term) {
            return new TermItemDTO(
                    term.getId(),
                    term.getTitle(),
                    term.getType().name(),
                    term.getContent(),
                    term.isRequired(),
                    term.getEffectiveAt(),
                    term.getVersion()
            );
        }
    }

    public static TermResponseDTO from(List<Term> terms) {
        List<TermItemDTO> termItems = terms.stream()
                .map(TermItemDTO::from)
                .toList();
        return new TermResponseDTO(termItems);
    }
}
