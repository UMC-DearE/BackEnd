package com.deare.backend.global.external.gemini.dto.request.ocr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiOcrRequestDTO {

    private String model;
    private List<Message> messages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private List<Content> content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String type;
        private String text;
        private ImageUrl image_url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        private String url;
    }


    public static GeminiOcrRequestDTO fromImages(
            String model,
            String instruction,
            List<String> base64Images
    ) {
        List<Content> contents = new ArrayList<>();

        contents.add(new Content("text", instruction, null));

        for (String base64 : base64Images) {
            String dataUri = "data:image/jpeg;base64," + base64;
            contents.add(new Content("image_url", null, new ImageUrl(dataUri)));
        }

        Message message = new Message("user", contents);
        return new GeminiOcrRequestDTO(model, List.of(message));
    }
}

