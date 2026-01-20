package com.deare.backend.global.service;

import com.deare.backend.global.config.S3Properties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties props;

    public S3Service(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    public UploadedFile upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        String key = buildKey(dir, file.getOriginalFilename());
        String contentType = (file.getContentType() != null)
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            String url = "https://%s.s3.%s.amazonaws.com/%s"
                    .formatted(props.bucket(), props.region(), key);

            return new UploadedFile(key, url);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 IO 오류", e);
        } catch (S3Exception e) {
            throw new RuntimeException("S3 업로드 실패: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key가 비어 있습니다.");
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (S3Exception e) {
            throw new RuntimeException("S3 삭제 실패: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private String buildKey(String dir, String originalFilename) {
        String safeDir = (dir == null || dir.isBlank()) ? "uploads" : dir.strip();
        String ext = "";

        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                ext = originalFilename.substring(dot);
            }
        }

        return safeDir + "/" + UUID.randomUUID() + ext;
    }

    public record UploadedFile(String key, String url) {}
}
