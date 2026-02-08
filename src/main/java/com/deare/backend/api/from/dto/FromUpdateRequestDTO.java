package com.deare.backend.api.from.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FromUpdateRequestDTO(

        @Size(max = 7, message = "From 이름은 최대 7자까지 가능합니다.")
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
