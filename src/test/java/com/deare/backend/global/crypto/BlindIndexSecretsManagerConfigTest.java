package com.deare.backend.global.crypto;

import com.deare.backend.domain.letter.search.BlindIndexKeyProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlindIndexSecretsManagerConfigTest {

    private final BlindIndexSecretsManagerConfig config = new BlindIndexSecretsManagerConfig();
    private final BlindIndexSecretsProperties properties = new BlindIndexSecretsProperties(
            true,
            "deare/prod/blind-index",
            "ap-northeast-2"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loadsVersionedKeyRingFromSecretString() {
        SecretsManagerClient client = mock(SecretsManagerClient.class);
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(GetSecretValueResponse.builder()
                        .secretString(validSecret())
                        .build());

        BlindIndexKeyProvider provider = config.blindIndexKeyProvider(
                client,
                objectMapper,
                properties
        );

        assertThat(provider.currentKey(1L).version().value()).isEqualTo(2);
        assertThat(provider.readableKeys(1L))
                .extracting(key -> key.version().value())
                .containsExactly(2, 1);
        verify(client).getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(properties.secretId())
                        .build()
        );
    }

    @Test
    void failsClosedForMissingOrMalformedSecret() {
        SecretsManagerClient client = mock(SecretsManagerClient.class);
        when(client.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(GetSecretValueResponse.builder()
                        .secretString("{\"currentVersion\":2}")
                        .build());

        assertThatThrownBy(() -> config.blindIndexKeyProvider(client, objectMapper, properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid blind index key ring secret.");
    }

    @Test
    void rejectsMissingSecretConfiguration() {
        BlindIndexSecretsProperties missingSecretId = new BlindIndexSecretsProperties(
                true,
                "",
                "ap-northeast-2"
        );

        assertThatThrownBy(() -> config.blindIndexSecretsManagerClient(missingSecretId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Blind index Secrets Manager secret ID is required.");
    }

    private String validSecret() {
        return """
                {
                  "currentVersion": 2,
                  "readableVersions": [2, 1],
                  "rootKeys": {
                    "1": "%s",
                    "2": "%s"
                  }
                }
                """.formatted(
                Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes()),
                Base64.getEncoder().encodeToString("fedcba9876543210fedcba9876543210".getBytes())
        );
    }
}
