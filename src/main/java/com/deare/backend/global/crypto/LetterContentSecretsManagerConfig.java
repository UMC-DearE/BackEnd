package com.deare.backend.global.crypto;

import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(LetterContentEncryptionProperties.class)
@ConditionalOnProperty(prefix = "letter-content.encryption", name = "enabled", havingValue = "true")
public class LetterContentSecretsManagerConfig {

    @Bean(name = "letterContentSecretsManagerClient", destroyMethod = "close")
    public SecretsManagerClient letterContentSecretsManagerClient(LetterContentEncryptionProperties properties) {
        validateProperties(properties);
        return SecretsManagerClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public AesGcmLetterContentCipher letterContentCipher(
            @Qualifier("letterContentSecretsManagerClient") SecretsManagerClient client,
            ObjectMapper objectMapper,
            LetterContentEncryptionProperties properties
    ) {
        validateProperties(properties);
        String secret = client.getSecretValue(GetSecretValueRequest.builder()
                        .secretId(properties.secretId()).build())
                .secretString();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("Letter content key ring secret must contain a secret string.");
        }
        try {
            LetterContentKeyRingSecret keyRing = objectMapper.readValue(secret, LetterContentKeyRingSecret.class);
            validateKeyRing(keyRing);
            return new AesGcmLetterContentCipher(keyRing.currentVersion(),
                    decodeKeys(keyRing.readableVersions(), keyRing.keys()));
        } catch (JsonProcessingException | IllegalArgumentException | NullPointerException exception) {
            throw new IllegalStateException("Invalid letter content key ring secret.", exception);
        }
    }

    private void validateKeyRing(LetterContentKeyRingSecret keyRing) {
        List<Integer> versions = keyRing.readableVersions();
        if (keyRing.currentVersion() <= 0 || versions == null || versions.isEmpty()
                || !Integer.valueOf(keyRing.currentVersion()).equals(versions.get(0))
                || versions.stream().anyMatch(version -> version == null || version <= 0)
                || versions.stream().distinct().count() != versions.size()) {
            throw new IllegalArgumentException("Invalid letter content key versions.");
        }
    }

    private Map<Integer, SecretKey> decodeKeys(List<Integer> versions, Map<Integer, String> encodedKeys) {
        if (encodedKeys == null || !encodedKeys.keySet().containsAll(versions)) {
            throw new IllegalArgumentException("Every readable version requires an encryption key.");
        }
        Map<Integer, SecretKey> keys = new LinkedHashMap<>();
        for (Integer version : versions) {
            byte[] decoded = Base64.getDecoder().decode(encodedKeys.get(version));
            try {
                keys.put(version, new SecretKeySpec(decoded, "AES"));
            } finally {
                Arrays.fill(decoded, (byte) 0);
            }
        }
        return Map.copyOf(keys);
    }

    private void validateProperties(LetterContentEncryptionProperties properties) {
        if (!StringUtils.hasText(properties.secretId())) {
            throw new IllegalStateException("Letter content Secrets Manager secret ID is required.");
        }
        if (!StringUtils.hasText(properties.region())) {
            throw new IllegalStateException("Letter content Secrets Manager region is required.");
        }
    }
}
