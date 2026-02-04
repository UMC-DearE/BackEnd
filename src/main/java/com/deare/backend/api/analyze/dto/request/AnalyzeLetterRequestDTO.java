package com.deare.backend.api.analyze.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalyzeLetterRequestDTO {

    @NotBlank
    @Size(max=5000, message="편지 내용은 최대 5000자까지 입력이 가능합니다.")
    private String content;

    public static AnalyzeLetterRequestDTO of(String content){
        AnalyzeLetterRequestDTO dto=new AnalyzeLetterRequestDTO();
        dto.content = content;
        return dto;
    }
}
