package com.deare.backend.api.from.dto;

import java.util.List;

public record FromListResponseDTO(
        List<FromResponseDTO> froms
) {
}
