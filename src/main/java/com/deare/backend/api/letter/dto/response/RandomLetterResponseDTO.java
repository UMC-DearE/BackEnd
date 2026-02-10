package com.deare.backend.api.letter.dto.response;

public record RandomLetterResponseDTO(
        boolean hasLetter,
        DateDTO date,
        Long letterId,
        String randomPhrase,
        boolean isPinned
) {
    public record DateDTO(
            String fullDate,
            String month,
            int day,
            String dayOfWeek
    ) {}
}
