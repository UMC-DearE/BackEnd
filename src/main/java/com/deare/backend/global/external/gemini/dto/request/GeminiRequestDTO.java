package com.deare.backend.global.external.gemini.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeminiRequestDTO {
    private String model;
    private List<Message> messages;

    public static GeminiRequestDTO fromUserText(String text) {
        return new GeminiRequestDTO(
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
