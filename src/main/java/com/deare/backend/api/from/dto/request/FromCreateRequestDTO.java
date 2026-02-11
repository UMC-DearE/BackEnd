package com.deare.backend.api.from.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FromCreateRequestDTO(

        @NotBlank(message = "From 이름은 필수입니다.")
        @Size(max = 7, message = "From 이름은 최대 7자까지 가능합니다.")
        String name,

        @NotBlank(message = "배경색은 필수입니다.")
        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6})$",
                message = "배경색은 #RRGGBB 형식이어야 합니다."
        )
        String bgColor,

        @NotBlank(message = "텍스트 색상은 필수입니다.")
        @Pattern(
                regexp = "^#([A-Fa-f0-9]{6})$",
                message = "텍스트 색상은 #RRGGBB 형식이어야 합니다."
        )
        String fontColor
) {
}
