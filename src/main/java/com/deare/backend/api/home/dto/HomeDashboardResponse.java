package com.deare.backend.api.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeDashboardResponse {
    private HomeUserDto user;
    private HomeSettingDto setting;
    private List<HomeStickerDto> stickers;
}
