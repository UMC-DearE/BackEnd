package com.deare.backend.api.auth.dto.response;

import lombok.Builder;

/**
 * 회원가입이 필요한 경우 반환하는 응답
 */
@Builder
public record SignupResponseDTO(
        String signupToken,
        String provider,
        String email,
        String message
) {
}
