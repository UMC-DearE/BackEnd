package com.deare.backend.api.home.service.dto;

import java.util.List;

public record HomeDashboardResult(
        HomeUserResult user,
        HomeSettingResult setting,
        List<HomeStickerResult> stickers
) {}
