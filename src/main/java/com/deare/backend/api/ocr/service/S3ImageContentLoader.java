package com.deare.backend.api.ocr.service;

import com.deare.backend.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3ImageContentLoader implements ImageContentLoader {

    private final S3Service s3Service;

    @Override
    public byte[] load(String s3Key) {
        return s3Service.downloadBytes(s3Key);
    }
}
