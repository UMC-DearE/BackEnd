package com.deare.backend.api.letter.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LetterUpdateRequestDTO {

    @Size(max = 5000)
    private String content;

    private LocalDate receivedAt;

    private boolean receivedAtSpecified;

    private Long fromId;

    public boolean hasAnyField() {
        return content != null
                || receivedAtSpecified
                || fromId != null;
    }
}
