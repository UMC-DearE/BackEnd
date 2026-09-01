package com.deare.backend.domain.letter.search;

public record BlindIndexKeyVersion(int value) implements Comparable<BlindIndexKeyVersion> {

    public BlindIndexKeyVersion {
        if (value <= 0) {
            throw new IllegalArgumentException("Blind index key version must be positive.");
        }
    }

    @Override
    public int compareTo(BlindIndexKeyVersion other) {
        return Integer.compare(value, other.value);
    }
}
