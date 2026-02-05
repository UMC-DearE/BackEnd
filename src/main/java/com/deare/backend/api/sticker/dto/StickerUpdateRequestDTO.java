package com.deare.backend.api.sticker.dto;

import java.math.BigDecimal;

public record StickerUpdateRequestDTO(
        BigDecimal posX,
        BigDecimal posY,
        Integer posZ,
        BigDecimal rotation,
        BigDecimal scale
) {
}
