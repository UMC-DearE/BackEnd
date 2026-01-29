package com.deare.backend.api.auth.dto.request;

import java.util.List;

/**
 * 회원가입 요청 DTO
 */
public record SignupRequestDTO(
        String nickname,
        List<Long> agreedTermIds
) {
}
