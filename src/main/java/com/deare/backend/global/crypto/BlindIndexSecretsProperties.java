package com.deare.backend.global.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blind-index.keys")
public record BlindIndexSecretsProperties(
        boolean enabled,
        String secretId,
        String region
) {
}
