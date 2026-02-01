package com.deare.backend.api.analyze.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalyzeLetterRequestDTO {

    @NotBlank
    private String context;
}
