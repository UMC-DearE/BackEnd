package com.deare.backend.domain.letter.search;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HkdfBlindIndexKeyProvider implements BlindIndexKeyProvider {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_ROOT_KEY_BYTES = 32;
    private static final byte[] HKDF_SALT =
            "deare:letter-search:blind-index:hkdf:v1".getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY_CONTEXT =
            "deare:letter-search:user-key:v1\0".getBytes(StandardCharsets.UTF_8);

    private final BlindIndexKeyVersion currentVersion;
    private final List<BlindIndexKeyVersion> readableVersions;
    private final Map<BlindIndexKeyVersion, byte[]> rootKeys;

    public HkdfBlindIndexKeyProvider(
            int currentVersion,
            List<Integer> readableVersions,
            Map<Integer, SecretKey> rootKeys
    ) {
        this.currentVersion = new BlindIndexKeyVersion(currentVersion);
        this.readableVersions = copyReadableVersions(readableVersions);
        this.rootKeys = copyRootKeys(rootKeys);
        validateConfiguration();
    }

    @Override
    public VersionedBlindIndexKey currentKey(long userId) {
        return derive(currentVersion, userId);
    }

    @Override
    public List<VersionedBlindIndexKey> readableKeys(long userId) {
        validateUserId(userId);
        return readableVersions.stream()
                .map(version -> derive(version, userId))
                .toList();
    }

    private VersionedBlindIndexKey derive(BlindIndexKeyVersion version, long userId) {
        validateUserId(userId);
        byte[] rootKey = rootKeys.get(version);
        if (rootKey == null) {
            throw new IllegalStateException("Blind index root key is unavailable for configured version.");
        }

        byte[] pseudoRandomKey = hmac(HKDF_SALT, rootKey);
        byte[] info = ByteBuffer.allocate(KEY_CONTEXT.length + Integer.BYTES + Long.BYTES + 1)
                .put(KEY_CONTEXT)
                .putInt(version.value())
                .putLong(userId)
                .put((byte) 1)
                .array();
        byte[] derivedKey = hmac(pseudoRandomKey, info);

        try {
            return new VersionedBlindIndexKey(
                    version,
                    new SecretKeySpec(derivedKey, HMAC_ALGORITHM)
            );
        } finally {
            Arrays.fill(pseudoRandomKey, (byte) 0);
            Arrays.fill(derivedKey, (byte) 0);
            Arrays.fill(info, (byte) 0);
        }
    }

    private List<BlindIndexKeyVersion> copyReadableVersions(List<Integer> versions) {
        Objects.requireNonNull(versions);
        if (versions.isEmpty()) {
            throw new IllegalArgumentException("At least one readable blind index key version is required.");
        }

        List<BlindIndexKeyVersion> copied = versions.stream()
                .map(BlindIndexKeyVersion::new)
                .distinct()
                .toList();
        if (copied.size() != versions.size()) {
            throw new IllegalArgumentException("Readable blind index key versions must be unique.");
        }
        return copied;
    }

    private Map<BlindIndexKeyVersion, byte[]> copyRootKeys(Map<Integer, SecretKey> source) {
        Objects.requireNonNull(source);
        Map<BlindIndexKeyVersion, byte[]> copied = new LinkedHashMap<>();

        source.forEach((version, secretKey) -> {
            BlindIndexKeyVersion keyVersion = new BlindIndexKeyVersion(version);
            byte[] encoded = Objects.requireNonNull(secretKey, "Blind index root key is required.").getEncoded();
            if (encoded == null || encoded.length < MIN_ROOT_KEY_BYTES) {
                throw new IllegalArgumentException("Blind index root key must contain at least 256 bits.");
            }
            copied.put(keyVersion, encoded.clone());
        });

        return Map.copyOf(copied);
    }

    private void validateConfiguration() {
        if (!readableVersions.get(0).equals(currentVersion)) {
            throw new IllegalArgumentException("Current blind index key version must be the first readable version.");
        }
        if (!rootKeys.keySet().containsAll(readableVersions)) {
            throw new IllegalArgumentException("Every readable blind index key version requires a root key.");
        }
    }

    private void validateUserId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
    }

    private byte[] hmac(byte[] key, byte[] input) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Required HMAC algorithm is unavailable.", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid blind index root key.", e);
        }
    }
}
