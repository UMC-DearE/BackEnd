package com.deare.backend.api.ocr.dto.response;

import com.deare.backend.api.ocr.dto.result.OcrResultDTO;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrLettersResponseDTO {

    private String combinedText;
    private List<OcrResultDTO> results;
    private SummaryDTO summary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDTO {
        private int total;
        private int success;
        private int failed;
        private boolean partialFailure;
        private List<Long> failedImageIds;
    }
}
