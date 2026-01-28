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
        private String role; // "user"
        private List<Content> content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String type; // "text" or "image_url"
        private String text; // type="text"일 때
        private ImageUrl image_url; // type="image_url"일 때
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        private String url;
    }

    /** 편지 이미지 + instruction으로 DTO 생성 */
    public static GeminiOcrRequestDTO fromImages(String instruction, List<String> base64Images) {
        List<Content> contents = new ArrayList<>();
        // 지시문 추가
        contents.add(new Content("text", instruction, null));
        // 이미지 추가
        for (String base64 : base64Images) {
            String dataUri = "data:image/jpeg;base64," + base64;
            contents.add(new Content("image_url", null, new ImageUrl(dataUri)));
        }

        Message message = new Message("user", contents);
        return new GeminiOcrRequestDTO("gemini-3-flash-preview", List.of(message));
    }
}

