package com.deare.backend.api.from.dto.response;

import java.util.List;

public record FromListResponseDTO(
        List<FromResponseDTO> froms
) {
}
