package com.deare.backend.domain.letter.search;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public final class BlindIndexTokenGenerator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final byte[] TOKEN_CONTEXT = "letter-search:v1\0".getBytes(StandardCharsets.UTF_8);

    private final SecretKey blindIndexKey;

    public BlindIndexTokenGenerator(SecretKey blindIndexKey) {
        this.blindIndexKey = Objects.requireNonNull(blindIndexKey);
        createMac();
    }

    public List<String> generateUnique(String text) {
        return LetterSearchBigramGenerator.generateUnique(text).stream()
                .map(this::generateToken)
                .toList();
    }

    private String generateToken(String bigram) {
        Mac mac = createMac();
        mac.update(TOKEN_CONTEXT);
        byte[] token = mac.doFinal(bigram.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private Mac createMac() {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(blindIndexKey);
            return mac;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Required HMAC algorithm is unavailable.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid blind index key.", e);
        }
    }
}
