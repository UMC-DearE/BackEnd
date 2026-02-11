package com.deare.backend.api.setting.dto.response;

import com.deare.backend.domain.setting.entity.enums.Font;
import com.deare.backend.domain.setting.entity.enums.Theme;

public record ThemeResponseDTO(
        Theme theme,
        Font font
) {}
