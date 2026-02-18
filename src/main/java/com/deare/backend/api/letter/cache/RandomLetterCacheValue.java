package com.deare.backend.api.letter.cache;

public record RandomLetterCacheValue(
        Long letterId,
        String randomPhrase
) {}
