package com.deare.backend.global.auth.oauth.config;

import com.deare.backend.global.auth.oauth.dto.google.GoogleOAuthProperties;
import com.deare.backend.global.auth.oauth.dto.kakao.KakaoOAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KakaoOAuthProperties.class,
        GoogleOAuthProperties.class
})
public class OAuthPropertiesConfig {
}
