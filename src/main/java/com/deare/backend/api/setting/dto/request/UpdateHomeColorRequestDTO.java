package com.deare.backend.api.setting.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateHomeColorRequestDTO(
        @NotBlank
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
        String homeColor
) {}
