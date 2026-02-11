package com.deare.backend.api.auth.dto.response;

import com.deare.backend.domain.term.entity.Term;

import java.time.LocalDateTime;

/**
 * 약관 개별 항목 응답 DTO
 */
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
