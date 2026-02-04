package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.Font;
import com.deare.backend.domain.setting.entity.Theme;

public record ThemeResponseDTO(
        Theme theme,
        Font font
) {}
