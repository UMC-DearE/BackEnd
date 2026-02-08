package com.deare.backend.api.from.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FromUpdateRequestDTO(

        @Size(min = 1, max = 7, message = "From 이름은 1자 이상 7자 이하여야 합니다.")
        String name,

        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6})$",
                message = "bgColor는 #RRGGBB 형식이어야 합니다."
        )
        String bgColor,

        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6})$",
                message = "fontColor는 #RRGGBB 형식이어야 합니다."
        )
        String fontColor
) {
}
