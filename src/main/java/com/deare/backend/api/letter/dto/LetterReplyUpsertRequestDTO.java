package com.deare.backend.api.letter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LetterReplyUpsertRequestDTO {

    @NotBlank(message = "reply는 필수입니다.")
    @Size(max = 100, message = "reply는 공백 포함 100자 이내여야 합니다.")
    private String reply;
}
