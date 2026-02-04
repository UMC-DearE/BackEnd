package com.deare.backend.api.ocr.dto.response;

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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrResultDTO {
        private Long imageId;
        private boolean success;
        private String text;        // success=true 일 때만
        private String errorCode;   // success=false 일 때만
        private String message;     // success=false 일 때만

        public static OcrResultDTO ok(Long imageId, String text) {
            return OcrResultDTO.builder()
                    .imageId(imageId)
                    .success(true)
                    .text(text)
                    .build();
        }

        public static OcrResultDTO fail(Long imageId, String errorCode, String message) {
            return OcrResultDTO.builder()
                    .imageId(imageId)
                    .success(false)
                    .errorCode(errorCode)
                    .message(message)
                    .build();
        }
    }
}
