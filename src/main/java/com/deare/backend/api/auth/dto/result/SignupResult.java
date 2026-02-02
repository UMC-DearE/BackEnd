package com.deare.backend.api.auth.dto.result;

import com.deare.backend.api.auth.dto.response.SignupResponseDTO;

/**
 * 회원가입 결과
 */
public record SignupResult(
        String accessToken,
        String refreshToken,
        SignupResponseDTO response
) {}
