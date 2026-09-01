package com.deare.backend.domain.letter.search;

import javax.crypto.SecretKey;
import java.util.Objects;

public final class VersionedBlindIndexKey {

    private final BlindIndexKeyVersion version;
    private final SecretKey key;

    public VersionedBlindIndexKey(BlindIndexKeyVersion version, SecretKey key) {
        this.version = Objects.requireNonNull(version);
        this.key = Objects.requireNonNull(key);
    }

    public BlindIndexKeyVersion version() {
        return version;
    }

    public SecretKey key() {
        return key;
    }

    @Override
    public String toString() {
        return "VersionedBlindIndexKey[version=" + version.value() + ", key=REDACTED]";
    }
}
