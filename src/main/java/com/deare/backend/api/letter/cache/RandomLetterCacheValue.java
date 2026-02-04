package com.deare.backend.api.letter.cache;

public record RandomLetterCacheValue(
        long userId,
        boolean hasLetter,
        String fullDate,
        Long letterId,
        String randomPhrase,
        boolean isPinned
) {}
