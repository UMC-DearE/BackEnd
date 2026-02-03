package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.google.GoogleUserInfoResponseDTO;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoUserInfoResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OAuthUserInfoClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties kakaoProps;
    private final GoogleOAuthProperties googleProps;

    public OAuthUserInfoClient(
            RestClient restClient,
            KakaoOAuthProperties kakaoProps,
            GoogleOAuthProperties googleProps
    ) {
        this.restClient = restClient;
        this.kakaoProps = kakaoProps;
        this.googleProps = googleProps;
    }

    public KakaoUserInfoResponseDTO fetchKakaoUserInfo(String accessToken) {
        return restClient.get()
                .uri(kakaoProps.userInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponseDTO.class);
    }

    public GoogleUserInfoResponseDTO fetchGoogleUserInfo(String accessToken) {
        return restClient.get()
                .uri(googleProps.userInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponseDTO.class);
    }
}
