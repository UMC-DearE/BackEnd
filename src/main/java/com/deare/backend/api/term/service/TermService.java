package com.deare.backend.api.term.service;

import com.deare.backend.api.term.dto.response.TermItemResponseDTO;
import com.deare.backend.api.term.dto.response.TermListResponseDTO;
import com.deare.backend.api.term.exception.TermErrorCode;
import com.deare.backend.domain.term.entity.Term;
import com.deare.backend.domain.term.entity.enums.TermType;
import com.deare.backend.domain.term.repository.TermRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public TermListResponseDTO getTerms(String type) {
        List<Term> terms;

        if (type == null) {
            terms = termRepository.findByIsActiveTrue();
        } else {
            TermType termType;
            try {
                termType = TermType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GeneralException(TermErrorCode.INVALID_TERM_TYPE);
            }
            terms = termRepository.findByIsActiveTrueAndType(termType);
        }

        List<TermItemResponseDTO> termItems = terms.stream()
                .map(TermItemResponseDTO::from)
                .toList();

        return new TermListResponseDTO(termItems);
    }
}
