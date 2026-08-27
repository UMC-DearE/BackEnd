package com.deare.backend.api.folder.dto.response;

import com.deare.backend.api.folder.dto.result.UnassignedLetterItemDTO;

import java.util.List;

public record UnassignedLetterListResponseDTO(
        long totalElements,
        int totalPages,
        int size,
        int number,
        List<UnassignedLetterItemDTO> content
) {
}
