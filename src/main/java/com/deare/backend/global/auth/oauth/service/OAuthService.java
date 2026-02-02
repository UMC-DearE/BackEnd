package com.deare.backend.global.auth.oauth.service;

import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.global.auth.oauth.client.GoogleOAuthTokenClient;
import com.deare.backend.global.auth.oauth.client.GoogleOAuthUserInfoClient;
import com.deare.backend.global.auth.oauth.client.KakaoOAuthTokenClient;
import com.deare.backend.global.auth.oauth.client.KakaoOAuthUserInfoClient;
import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.google.GoogleTokenResponseDTO;
import com.deare.backend.global.auth.oauth.dto.google.GoogleUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoTokenResponseDTO;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthAuthorizeResponseDTO;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthCallbackUserInfoResponseDTO;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final KakaoOAuthProperties kakaoProps;
    private final GoogleOAuthProperties googleProps;

    private final KakaoOAuthTokenClient kakaoTokenClient;
    private final KakaoOAuthUserInfoClient kakaoUserInfoClient;

    private final GoogleOAuthTokenClient googleTokenClient;
    private final GoogleOAuthUserInfoClient googleUserInfoClient;

    private final OAuthStateService oAuthStateService;

    /**
     * OAuth 인증 URL 생성
     * - state 파라미터 포함 (CSRF 방지)
     */
    public OAuthAuthorizeResponseDTO buildAuthorizeUrl(String provider) {
        // State 생성 및 Redis 저장
        String state = oAuthStateService.generateState();

        String url = switch (provider) {
            case "kakao" -> UriComponentsBuilder
                    .fromUriString(kakaoProps.authorizeUri())
                    .queryParam("client_id", kakaoProps.clientId())
                    .queryParam("redirect_uri", kakaoProps.redirectUri())
                    .queryParam("response_type", "code")
                    .queryParam("state", state)
                    .build()
                    .toUriString();

            case "google" -> UriComponentsBuilder
                    .fromUriString(googleProps.authorizeUri())
                    .queryParam("client_id", googleProps.clientId())
                    .queryParam("redirect_uri", googleProps.redirectUri())
                    .queryParam("response_type", "code")
                    .queryParam("scope", googleProps.scope())
                    .queryParam("access_type", "offline")
                    .queryParam("prompt", "consent")
                    .queryParam("state", state)
                    .build()
                    .toUriString();

            default -> throw new GeneralException(AuthErrorCode.INVALID_PROVIDER);
        };

        return new OAuthAuthorizeResponseDTO(url, LocalDateTime.now());
    }

    /**
     * State 검증
     */
    public void validateState(String state) {
        if (!oAuthStateService.validateAndDeleteState(state)) {
            throw new GeneralException(AuthErrorCode.INVALID_STATE);
        }
    }

    /**
     * OAuth 콜백 처리
     * - provider access token은 userinfo 1회 호출 후 폐기
     */
    public OAuthCallbackUserInfoResponseDTO handleCallback(String provider, String code) {
        if ("kakao".equals(provider)) {
            return handleKakaoCallback(code);
        }

        if ("google".equals(provider)) {
            return handleGoogleCallback(code);
        }

        throw new GeneralException(AuthErrorCode.INVALID_PROVIDER);
    }

    private OAuthCallbackUserInfoResponseDTO handleKakaoCallback(String code) {
        KakaoTokenResponseDTO token;
        try {
            token = kakaoTokenClient.exchangeCodeForToken(code);
        } catch (Exception e) {
            throw new GeneralException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
        }

        KakaoUserInfoResponseDTO userInfo;
        try {
            userInfo = kakaoUserInfoClient.fetchUserInfo(token.accessToken());
        } catch (Exception e) {
            throw new GeneralException(AuthErrorCode.OAUTH_USERINFO_REQUEST_FAILED);
        }

        String email = userInfo.kakaoAccount() != null
                ? userInfo.kakaoAccount().email()
                : null;

        return new OAuthCallbackUserInfoResponseDTO(
                "kakao",
                String.valueOf(userInfo.id()),
                email,
                LocalDateTime.now()
        );
    }

    private OAuthCallbackUserInfoResponseDTO handleGoogleCallback(String code) {
        GoogleTokenResponseDTO token;
        try {
            token = googleTokenClient.exchangeCodeForToken(code);
        } catch (Exception e) {
            throw new GeneralException(AuthErrorCode.OAUTH_TOKEN_REQUEST_FAILED);
        }

        GoogleUserInfoResponseDTO userInfo;
        try {
            userInfo = googleUserInfoClient.fetchUserInfo(token.accessToken());
        } catch (Exception e) {
            throw new GeneralException(AuthErrorCode.OAUTH_USERINFO_REQUEST_FAILED);
        }

        return new OAuthCallbackUserInfoResponseDTO(
                "google",
                userInfo.sub(),
                userInfo.email(),
                LocalDateTime.now()
        );
    }
}
