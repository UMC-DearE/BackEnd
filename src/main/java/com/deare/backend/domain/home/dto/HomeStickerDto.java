package com.deare.backend.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeStickerDto {
    private Long stickerId;
    private Long imageId;
    private String imageUrl;

    private double posX;
    private double posY;
    private int posZ;
    private double rotation;
    private double scale;

}
