package com.deare.backend.api.home.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

public record HomeEditRequestDTO(
        @NotBlank
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$",
                message = "HEX 색상 코드 형식이 올바르지 않습니다.")
        String homeColor, List<StickerRequest> stickers
) {
    @NotNull
    public record StickerRequest(
            Long imageId,
            BigDecimal posX,
            BigDecimal posY,
            Integer posZ,
            BigDecimal rotation,
            BigDecimal scale
    ){}
}