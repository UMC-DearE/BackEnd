package com.deare.backend.api.setting.service;

import com.deare.backend.api.setting.dto.request.UpdateFontRequestDTO;
import com.deare.backend.api.setting.dto.response.*;

public interface SettingService {

    ThemeResponseDTO getTheme(Long userId);

    MembershipResponseDTO getMembership(Long userId);

    UpdateFontResponseDTO updateFont(Long userId, UpdateFontRequestDTO request);
}
