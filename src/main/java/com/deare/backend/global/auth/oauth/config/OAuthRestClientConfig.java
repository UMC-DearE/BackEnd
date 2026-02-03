package com.deare.backend.global.auth.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OAuthRestClientConfig {

    @Bean
    public RestClient oauthRestClient() {
        return RestClient.builder().build();
    }
}
