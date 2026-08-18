package com.deare.backend.api.term.dto.response;

import java.util.List;

public record TermListResponseDTO(
        List<TermItemResponseDTO> terms
) {}
