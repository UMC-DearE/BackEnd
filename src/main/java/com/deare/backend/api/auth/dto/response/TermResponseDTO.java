package com.deare.backend.api.auth.dto.response;

import com.deare.backend.domain.term.entity.Term;

import java.util.List;

/**
 * 회원가입용 약관 조회 응답 DTO
 */
public record TermResponseDTO(
        List<TermItemDTO> terms
) {
    public static TermResponseDTO from(List<Term> terms) {
        List<TermItemDTO> termItems = terms.stream()
                .map(TermItemDTO::from)
                .toList();
        return new TermResponseDTO(termItems);
    }
}
