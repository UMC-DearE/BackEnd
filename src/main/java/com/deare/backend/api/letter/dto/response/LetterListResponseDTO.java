package com.deare.backend.api.letter.dto.response;

import com.deare.backend.api.letter.dto.result.LetterItemDTO;

import java.util.List;

public record LetterListResponseDTO(
        long totalElements,
        int totalPages,
        int size,
        int number,
        List<LetterItemDTO> content
) {}
