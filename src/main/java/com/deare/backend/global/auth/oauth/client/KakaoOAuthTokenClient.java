package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoTokenResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthTokenClient {

    private final RestClient restClient;
    private final KakaoOAuthProperties props;

    public KakaoOAuthTokenClient(RestClient restClient, KakaoOAuthProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public KakaoTokenResponseDTO exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.clientId());
        form.add("redirect_uri", props.redirectUri());
        form.add("code", code);

        if (props.clientSecret() != null && !props.clientSecret().isBlank()) {
            form.add("client_secret", props.clientSecret());
        }

        return restClient.post()
                .uri(props.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponseDTO.class);
    }
}
