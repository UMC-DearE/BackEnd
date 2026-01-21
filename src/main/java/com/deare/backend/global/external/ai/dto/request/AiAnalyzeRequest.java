package com.deare.backend.global.external.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AiAnalyzeRequest {
    //text: 편지글
    private String text;
}
