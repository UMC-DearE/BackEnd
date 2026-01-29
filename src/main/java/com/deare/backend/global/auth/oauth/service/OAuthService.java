package com.deare.backend.global.auth.oauth.service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
public class OAuthService {

    private final KakaoOAuthProperties kakaoProps;
    private final GoogleOAuthProperties googleProps;

    private final KakaoOAuthTokenClient kakaoTokenClient;
    private final KakaoOAuthUserInfoClient kakaoUserInfoClient;

    private final GoogleOAuthTokenClient googleTokenClient;
    private final GoogleOAuthUserInfoClient googleUserInfoClient;

    public OAuthService(
            KakaoOAuthProperties kakaoProps,
            GoogleOAuthProperties googleProps,
            KakaoOAuthTokenClient kakaoTokenClient,
            KakaoOAuthUserInfoClient kakaoUserInfoClient,
            GoogleOAuthTokenClient googleTokenClient,
            GoogleOAuthUserInfoClient googleUserInfoClient
    ) {
        this.kakaoProps = kakaoProps;
        this.googleProps = googleProps;
        this.kakaoTokenClient = kakaoTokenClient;
        this.kakaoUserInfoClient = kakaoUserInfoClient;
        this.googleTokenClient = googleTokenClient;
        this.googleUserInfoClient = googleUserInfoClient;
    }

    public OAuthAuthorizeResponseDTO buildAuthorizeUrl(String provider) {
        String url = switch (provider) {
            case "kakao" -> UriComponentsBuilder
                    .fromUriString(kakaoProps.authorizeUri())
                    .queryParam("client_id", kakaoProps.clientId())
                    .queryParam("redirect_uri", kakaoProps.redirectUri())
                    .queryParam("response_type", "code")
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
                    .build()
                    .toUriString();

            default -> throw new IllegalArgumentException("Unsupported provider");
        };

        return new OAuthAuthorizeResponseDTO(url, LocalDateTime.now());
    }

    /**
     * 핵심 정책:
     * - provider refresh token ❌
     * - provider access token은 userinfo 1회 호출 후 폐기
     */
    public OAuthCallbackUserInfoResponseDTO handleCallback(String provider, String code) {
        if ("kakao".equals(provider)) {
            KakaoTokenResponseDTO token = kakaoTokenClient.exchangeCodeForToken(code);
            KakaoUserInfoResponseDTO userInfo = kakaoUserInfoClient.fetchUserInfo(token.accessToken());

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

        if ("google".equals(provider)) {
            GoogleTokenResponseDTO token = googleTokenClient.exchangeCodeForToken(code);
            GoogleUserInfoResponseDTO userInfo = googleUserInfoClient.fetchUserInfo(token.accessToken());

            return new OAuthCallbackUserInfoResponseDTO(
                    "google",
                    userInfo.sub(),
                    userInfo.email(),
                    LocalDateTime.now()
            );
        }

        throw new IllegalArgumentException("Unsupported provider");
    }
}
