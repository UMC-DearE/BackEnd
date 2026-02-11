package com.deare.backend.api.home.dto.response;

import com.deare.backend.api.home.dto.result.HomeSettingDto;
import com.deare.backend.api.home.dto.result.HomeStickerDto;
import com.deare.backend.api.home.dto.result.HomeUserDto;
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
