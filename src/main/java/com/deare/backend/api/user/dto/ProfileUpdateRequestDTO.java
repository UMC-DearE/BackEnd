package com.deare.backend.api.user.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequestDTO(

        @Size(min = 2, max = 10, message = "USER_42201")
        String nickname,

        @Size(max = 20, message = "USER_42202")
        String intro,

        Long imageId,

        Boolean resetProfileImage
) {
}
