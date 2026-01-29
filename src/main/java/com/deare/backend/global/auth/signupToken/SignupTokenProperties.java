package com.deare.backend.global.auth.signupToken;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "signup-token")
public class SignupTokenProperties {
    
    private String secretKey;
    private Long expiration;
}
