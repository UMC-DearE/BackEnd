package com.deare.backend.api.ocr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OcrLettersRequestDTO {

    @NotEmpty(message = "imageIds는 비어있을 수 없습니다.")
    @Size(max = 10, message = "이미지는 최대 10장까지 요청 가능합니다.")
    private List<@NotNull Long> imageIds;
}
