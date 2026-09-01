package com.deare.backend.domain.letter.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class AesGcmLetterContentCipher {

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int AUTH_TAG_LENGTH_BITS = 128;
    private static final byte[] AAD_CONTEXT =
            "deare:letter-content:aes-gcm:v1".getBytes(StandardCharsets.UTF_8);

    private final int currentKeyVersion;
    private final Map<Integer, SecretKey> readableKeys;
    private final SecureRandom secureRandom;

    public AesGcmLetterContentCipher(
            int currentKeyVersion,
            Map<Integer, SecretKey> readableKeys
    ) {
        this(currentKeyVersion, readableKeys, new SecureRandom());
    }

    AesGcmLetterContentCipher(
            int currentKeyVersion,
            Map<Integer, SecretKey> readableKeys,
            SecureRandom secureRandom
    ) {
        if (currentKeyVersion <= 0) {
            throw new IllegalArgumentException("Current encryption key version must be positive.");
        }
        this.currentKeyVersion = currentKeyVersion;
        this.readableKeys = copyKeys(readableKeys);
        if (!this.readableKeys.containsKey(currentKeyVersion)) {
            throw new IllegalArgumentException("Current encryption key must be readable.");
        }
        this.secureRandom = Objects.requireNonNull(secureRandom, "Secure random is required.");
    }

    public EncryptedLetterContent encrypt(
            String plaintext,
            long userId,
            long letterId,
            int contentVersion
    ) {
        Objects.requireNonNull(plaintext, "Letter content is required.");
        validateContext(userId, letterId, contentVersion);

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);
        try {
            Cipher cipher = createCipher(
                    Cipher.ENCRYPT_MODE,
                    readableKeys.get(currentKeyVersion),
                    nonce,
                    associatedData(userId, letterId, contentVersion)
            );
            return new EncryptedLetterContent(
                    currentKeyVersion,
                    encode(nonce),
                    encode(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)))
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Letter content encryption failed.", exception);
        } finally {
            Arrays.fill(nonce, (byte) 0);
        }
    }

    public String decrypt(
            EncryptedLetterContent encryptedContent,
            long userId,
            long letterId,
            int contentVersion
    ) {
        Objects.requireNonNull(encryptedContent, "Encrypted letter content is required.");
        validateContext(userId, letterId, contentVersion);

        SecretKey key = readableKeys.get(encryptedContent.keyVersion());
        if (key == null) {
            throw new IllegalStateException("Letter content decryption failed.");
        }

        byte[] nonce = null;
        byte[] ciphertext = null;
        try {
            nonce = decode(encryptedContent.nonce());
            ciphertext = decode(encryptedContent.ciphertext());
            if (nonce.length != NONCE_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encryption nonce.");
            }

            Cipher cipher = createCipher(
                    Cipher.DECRYPT_MODE,
                    key,
                    nonce,
                    associatedData(userId, letterId, contentVersion)
            );
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Letter content decryption failed.", exception);
        } finally {
            if (nonce != null) {
                Arrays.fill(nonce, (byte) 0);
            }
            if (ciphertext != null) {
                Arrays.fill(ciphertext, (byte) 0);
            }
        }
    }

    private Cipher createCipher(
            int mode,
            SecretKey key,
            byte[] nonce,
            byte[] associatedData
    ) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key, new GCMParameterSpec(AUTH_TAG_LENGTH_BITS, nonce));
        cipher.updateAAD(associatedData);
        return cipher;
    }

    private byte[] associatedData(long userId, long letterId, int contentVersion) {
        return ByteBuffer.allocate(AAD_CONTEXT.length + Long.BYTES * 2 + Integer.BYTES)
                .put(AAD_CONTEXT)
                .putLong(userId)
                .putLong(letterId)
                .putInt(contentVersion)
                .array();
    }

    private void validateContext(long userId, long letterId, int contentVersion) {
        if (userId <= 0 || letterId <= 0 || contentVersion <= 0) {
            throw new IllegalArgumentException("Valid letter encryption context is required.");
        }
    }

    private Map<Integer, SecretKey> copyKeys(Map<Integer, SecretKey> source) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("At least one encryption key is required.");
        }

        Map<Integer, SecretKey> copied = new LinkedHashMap<>();
        source.forEach((version, key) -> {
            if (version == null || version <= 0 || key == null) {
                throw new IllegalArgumentException("Valid versioned encryption keys are required.");
            }
            byte[] encoded = key.getEncoded();
            if (encoded == null || encoded.length != KEY_LENGTH_BYTES) {
                throw new IllegalArgumentException("Letter encryption keys must contain 256 bits.");
            }
            try {
                copied.put(version, new SecretKeySpec(encoded, "AES"));
            } finally {
                Arrays.fill(encoded, (byte) 0);
            }
        });
        return Map.copyOf(copied);
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
