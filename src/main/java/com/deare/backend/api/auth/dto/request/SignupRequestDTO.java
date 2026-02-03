package com.deare.backend.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 회원가입 요청 DTO
 */
public record SignupRequestDTO(

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하입니다")
        String nickname,

        List<Long> termIds
) {
}
