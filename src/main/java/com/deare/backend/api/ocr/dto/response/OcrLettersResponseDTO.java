package com.deare.backend.api.ocr.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OcrLettersResponseDTO {

    private String combinedText;
    private List<OcrResultDTO> results;

    @Getter
    @Builder
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
                    .text(null)
                    .errorCode(errorCode)
                    .message(message)
                    .build();
        }
    }
}
