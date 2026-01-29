package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.google.GoogleUserInfoResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthUserInfoClient {

    private final RestClient restClient;
    private final GoogleOAuthProperties props;

    public GoogleOAuthUserInfoClient(RestClient restClient, GoogleOAuthProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public GoogleUserInfoResponseDTO fetchUserInfo(String accessToken) {
        return restClient.get()
                .uri(props.userInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfoResponseDTO.class);
    }
}
