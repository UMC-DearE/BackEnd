package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.enums.Font;

import java.time.LocalDateTime;

public record UpdateFontResponseDTO(
        Font font,
        LocalDateTime updatedAt
) {}
