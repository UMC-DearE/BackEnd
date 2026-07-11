package com.deare.backend.api.home.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
public class HomeEditRequest {
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$",
            message = "HEX 색상 코드 형식이 올바르지 않습니다.")
    private String homeColor;
    private List<StickerRequest> stickers;

    @Getter
    @NoArgsConstructor
    public static class StickerRequest {
        private Long imageId;
        private BigDecimal posX;
        private BigDecimal posY;
        private Integer posZ;
        private BigDecimal rotation;
        private BigDecimal scale;
    }
}
