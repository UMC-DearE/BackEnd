package com.deare.backend.global.crypto;

import com.deare.backend.domain.letter.crypto.AesGcmLetterContentCipher;
import com.deare.backend.domain.letter.crypto.EncryptedLetterContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LetterContentSecretsManagerConfigTest {

    private final LetterContentSecretsManagerConfig config =
            new LetterContentSecretsManagerConfig();
    private final LetterContentEncryptionProperties properties =
            new LetterContentEncryptionProperties(
                    true,
                    "deare/prod/letter-content",
                    "ap-northeast-2"
            );
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loadsCurrentAndReadablePreviousKeysFromSecretString() {
        SecretsManagerClient client = clientReturning(validSecret());
        EncryptedLetterContent oldContent = new AesGcmLetterContentCipher(
                1,
                Map.of(1, key("0123456789abcdef0123456789abcdef"))
        ).encrypt("old content", 10L, 20L, 1);

        AesGcmLetterContentCipher cipher = config.letterContentCipher(
                client,
                objectMapper,
                properties
        );

        assertThat(cipher.encrypt("new content", 10L, 20L, 2).keyVersion()).isEqualTo(2);
        assertThat(cipher.decrypt(oldContent, 10L, 20L, 1)).isEqualTo("old content");
        verify(client).getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(properties.secretId())
                        .build()
        );
    }

    @Test
    void failsClosedForMalformedVersionPolicyAndMissingKeys() {
        assertInvalidSecret("""
                {
                  "currentVersion": 2,
                  "readableVersions": [1, 2],
                  "keys": {}
                }
                """);
        assertInvalidSecret("""
                {
                  "currentVersion": 2,
                  "readableVersions": [2, 2],
                  "keys": {"2": "%s"}
                }
                """.formatted(encoded("fedcba9876543210fedcba9876543210")));
        assertInvalidSecret("""
                {
                  "currentVersion": 1,
                  "readableVersions": [1],
                  "keys": {"1": "%s"}
                }
                """.formatted(encoded("too-short")));
    }

    @Test
    void rejectsMissingSecretConfiguration() {
        LetterContentEncryptionProperties missingSecretId =
                new LetterContentEncryptionProperties(true, "", "ap-northeast-2");

        assertThatThrownBy(() -> config.letterContentSecretsManagerClient(missingSecretId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Letter content Secrets Manager secret ID is required.");
    }

    @Test
    void keepsBlindIndexAndLetterContentClientsExplicitlyQualified() throws Exception {
        Qualifier blindIndexQualifier = BlindIndexSecretsManagerConfig.class
                .getMethod(
                        "blindIndexKeyProvider",
                        SecretsManagerClient.class,
                        ObjectMapper.class,
                        BlindIndexSecretsProperties.class
                )
                .getParameters()[0]
                .getAnnotation(Qualifier.class);
        Qualifier letterContentQualifier = LetterContentSecretsManagerConfig.class
                .getMethod(
                        "letterContentCipher",
                        SecretsManagerClient.class,
                        ObjectMapper.class,
                        LetterContentEncryptionProperties.class
                )
                .getParameters()[0]
                .getAnnotation(Qualifier.class);

        assertThat(blindIndexQualifier.value()).isEqualTo("blindIndexSecretsManagerClient");
        assertThat(letterContentQualifier.value()).isEqualTo("letterContentSecretsManagerClient");
    }

    private SecretsManagerClient clientReturning(String secret) {
        SecretsManagerClient client = mock(SecretsManagerClient.class);
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(GetSecretValueResponse.builder().secretString(secret).build());
        return client;
    }

    private void assertInvalidSecret(String secret) {
        assertThatThrownBy(() -> config.letterContentCipher(
                clientReturning(secret),
                objectMapper,
                properties
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid letter content key ring secret.");
    }

    private String validSecret() {
        return """
                {
                  "currentVersion": 2,
                  "readableVersions": [2, 1],
                  "keys": {
                    "1": "%s",
                    "2": "%s"
                  }
                }
                """.formatted(
                encoded("0123456789abcdef0123456789abcdef"),
                encoded("fedcba9876543210fedcba9876543210")
        );
    }

    private SecretKey key(String value) {
        return new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private String encoded(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
