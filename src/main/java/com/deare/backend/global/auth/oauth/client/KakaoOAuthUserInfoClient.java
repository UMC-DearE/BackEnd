package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoUserInfoResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthUserInfoClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties props;

    public KakaoOAuthUserInfoClient(RestClient restClient, KakaoOAuthProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public KakaoUserInfoResponseDTO fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(props.userInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfoResponseDTO.class);
    }
}
