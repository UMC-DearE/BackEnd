package com.deare.backend.api.sticker.dto.request;

import java.math.BigDecimal;

public record StickerCreateRequestDTO(
        Long imageId,
        BigDecimal posX,
        BigDecimal posY,
        Integer posZ,
        BigDecimal rotation,
        BigDecimal scale
) {
}
