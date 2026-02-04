package com.deare.backend.api.ocr.service;

public interface ImageContentLoader {
    byte[] load(String s3Key);
}
