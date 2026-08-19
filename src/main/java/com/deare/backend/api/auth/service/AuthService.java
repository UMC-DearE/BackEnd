package com.deare.backend.api.auth.service;

import com.deare.backend.api.auth.dto.request.SignupRequestDTO;
import com.deare.backend.api.auth.dto.response.TermResponseDTO;
import com.deare.backend.api.auth.dto.result.OAuthCallbackResult;
import com.deare.backend.api.auth.dto.result.SignupResult;
import com.deare.backend.api.auth.dto.result.TokenPair;

public interface AuthService {

    OAuthCallbackResult handleOAuthCallback(String provider, String code, String inviteCode);

    TokenPair refresh(String refreshToken);

    void validateSignupToken(String signupToken);

    TermResponseDTO getSignupTerms();

    SignupResult signup(String signupToken, SignupRequestDTO request);

    void logout(Long userId);
}
