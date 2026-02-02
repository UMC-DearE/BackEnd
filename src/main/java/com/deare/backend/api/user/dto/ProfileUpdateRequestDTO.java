package com.deare.backend.api.user.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequestDTO(

        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하입니다.")
        String nickname,

        @Size(max = 20, message = "소개글은 최대 20자입니다.")
        String intro,

        Long imageId,

        Boolean resetProfileImage
) {
}
