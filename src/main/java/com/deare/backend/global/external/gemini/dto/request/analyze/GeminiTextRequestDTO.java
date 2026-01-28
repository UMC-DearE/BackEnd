package com.deare.backend.global.external.gemini.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeminiTextRequestDTO {
    private String model;
    private List<Message> messages;

    public static GeminiTextRequestDTO fromLetterText(String text) {
        return new GeminiTextRequestDTO(
                "gemini-3-flash-preview",
                List.of(new Message("user", text))
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
