package com.deare.backend.global.auth.oauth.client;

import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.google.GoogleTokenResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthTokenClient {

    private final RestClient restClient;
    private final GoogleOAuthProperties props;

    public GoogleOAuthTokenClient(RestClient restClient, GoogleOAuthProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public GoogleTokenResponseDTO exchangeCodeForToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());
        form.add("redirect_uri", props.redirectUri());
        form.add("code", code);

        return restClient.post()
                .uri(props.tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponseDTO.class);
    }
}
