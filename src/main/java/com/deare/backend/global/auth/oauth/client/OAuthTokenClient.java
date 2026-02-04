package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.oauth.OAuthTokenResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class OAuthTokenClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties kakaoProps;
    private final GoogleOAuthProperties googleProps;

    public OAuthTokenClient(
            RestClient restClient,
            KakaoOAuthProperties kakaoProps,
            GoogleOAuthProperties googleProps
    ) {
        this.restClient = restClient;
        this.kakaoProps = kakaoProps;
        this.googleProps = googleProps;
    }

    public OAuthTokenResponseDTO exchangeKakaoCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoProps.clientId());
        form.add("redirect_uri", kakaoProps.redirectUri());
        form.add("code", code);

        // 카카오도 client_secret 사용 (보안 강화)
        if (kakaoProps.clientSecret() != null && !kakaoProps.clientSecret().isBlank()) {
            form.add("client_secret", kakaoProps.clientSecret());
        }

        return restClient.post()
                .uri(kakaoProps.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(OAuthTokenResponseDTO.class);
    }

    public OAuthTokenResponseDTO exchangeGoogleCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", googleProps.clientId());
        form.add("client_secret", googleProps.clientSecret());
        form.add("redirect_uri", googleProps.redirectUri());
        form.add("code", code);

        return restClient.post()
                .uri(googleProps.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(OAuthTokenResponseDTO.class);
    }
}
