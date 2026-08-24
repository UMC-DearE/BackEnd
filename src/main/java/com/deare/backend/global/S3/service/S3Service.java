package com.deare.backend.global.S3.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    UploadedFile upload(MultipartFile file, String dir);

    void delete(String key);

    byte[] downloadBytes(String key);

    record UploadedFile(String key, String url) {}
}
