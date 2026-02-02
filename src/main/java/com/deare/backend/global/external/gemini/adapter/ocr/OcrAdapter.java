package com.deare.backend.global.external.gemini.adapter.ocr;

public interface OcrAdapter {
    String ocr(String instruction, String base64Image);
}
