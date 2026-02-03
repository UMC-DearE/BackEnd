package com.deare.backend.api.ocr.service;

import com.deare.backend.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3ImageContentLoader implements ImageContentLoader {

    private final S3Service s3Service;

    @Override
    public byte[] load(String s3Key) {
        return s3Service.downloadBytes(s3Key);
    }
}
