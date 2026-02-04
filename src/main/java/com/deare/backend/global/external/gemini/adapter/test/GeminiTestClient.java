package com.deare.backend.global.external.gemini.adapter.test;

import java.util.List;

public interface GeminiTestClient {
    String geminiTest(String text);
    String geminiOcrTest(String instruction, List<String> base64Images);
}
