package com.deare.backend.api.letter.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class LetterUpdateRequestDTO {

    @Size(max = 5000) // 사이즈 제한 생기면 그때 다시 수정
    private String content;

    private LocalDate receivedAt;

    private Long fromId;

    public boolean hasAnyField() {
        return content != null || receivedAt != null || fromId != null;
    }
}
