package com.deare.backend.global.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "letter-content.encryption")
public record LetterContentEncryptionProperties(boolean enabled, String secretId, String region) {
}
