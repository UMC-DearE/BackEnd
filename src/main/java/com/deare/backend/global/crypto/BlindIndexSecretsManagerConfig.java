package com.deare.backend.global.crypto;

import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.deare.backend.domain.letter.search.HkdfBlindIndexKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(BlindIndexSecretsProperties.class)
@ConditionalOnProperty(
        prefix = "blind-index.keys",
        name = "enabled",
        havingValue = "true"
)
public class BlindIndexSecretsManagerConfig {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Bean(destroyMethod = "close")
    public SecretsManagerClient blindIndexSecretsManagerClient(BlindIndexSecretsProperties properties) {
        validateProperties(properties);
        return SecretsManagerClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public BlindIndexKeyProvider blindIndexKeyProvider(
            @Qualifier("blindIndexSecretsManagerClient") SecretsManagerClient secretsManagerClient,
            ObjectMapper objectMapper,
            BlindIndexSecretsProperties properties
    ) {
        validateProperties(properties);
        GetSecretValueResponse response = secretsManagerClient.getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(properties.secretId())
                        .build()
        );

        if (!StringUtils.hasText(response.secretString())) {
            throw new IllegalStateException("Blind index key ring secret must contain a secret string.");
        }

        try {
            BlindIndexKeyRingSecret keyRing = objectMapper.readValue(
                    response.secretString(),
                    BlindIndexKeyRingSecret.class
            );
            return new HkdfBlindIndexKeyProvider(
                    keyRing.currentVersion(),
                    keyRing.readableVersions(),
                    decodeRootKeys(keyRing.rootKeys())
            );
        } catch (JsonProcessingException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalStateException("Invalid blind index key ring secret.", e);
        }
    }

    private Map<Integer, SecretKey> decodeRootKeys(Map<Integer, String> encodedRootKeys) {
        if (encodedRootKeys == null || encodedRootKeys.isEmpty()) {
            throw new IllegalArgumentException("Blind index root keys are required.");
        }

        Map<Integer, SecretKey> decodedRootKeys = new LinkedHashMap<>();
        encodedRootKeys.forEach((version, encodedKey) -> {
            byte[] decoded = Base64.getDecoder().decode(encodedKey);
            try {
                decodedRootKeys.put(version, new SecretKeySpec(decoded, HMAC_ALGORITHM));
            } finally {
                Arrays.fill(decoded, (byte) 0);
            }
        });
        return Map.copyOf(decodedRootKeys);
    }

    private void validateProperties(BlindIndexSecretsProperties properties) {
        if (!StringUtils.hasText(properties.secretId())) {
            throw new IllegalStateException("Blind index Secrets Manager secret ID is required.");
        }
        if (!StringUtils.hasText(properties.region())) {
            throw new IllegalStateException("Blind index Secrets Manager region is required.");
        }
    }
}
