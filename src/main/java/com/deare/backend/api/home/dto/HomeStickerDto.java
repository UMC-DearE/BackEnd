package com.deare.backend.api.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class HomeStickerDto {
    private Long stickerId;
    private Long imageId;
    private String imageUrl;

    private BigDecimal posX;
    private BigDecimal posY;
    private int posZ;
    private BigDecimal rotation;
    private BigDecimal scale;

}
