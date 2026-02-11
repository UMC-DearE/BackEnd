package com.deare.backend.global.S3.service;

import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.S3.exception.S3ErrorCode;
import com.deare.backend.global.S3.config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Properties props;

    public S3Service(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.props = props;
    }

    public UploadedFile upload(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(S3ErrorCode.EMPTY_FILE);
        }

        String key = buildKey(dir, file.getOriginalFilename());

        String contentType = (file.getContentType() != null)
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        long contentLength = file.getSize();

        try (var inputStream = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(
                    putReq,
                    RequestBody.fromInputStream(inputStream, contentLength)
            );

            String url = buildCloudFrontUrl(key);
            return new UploadedFile(key, url);

        } catch (IOException e) {
            log.warn("[S3] IO error while reading file. key={}", key, e);
            throw new GeneralException(S3ErrorCode.IO_ERROR);
        } catch (S3Exception e) {
            log.error("[S3] Upload failed. key={} status={} awsErrorCode={} message={}",
                    key,
                    e.statusCode(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "null",
                    e.getMessage(),
                    e
            );
            throw new GeneralException(S3ErrorCode.UPLOAD_FAILED);
        }
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new GeneralException(S3ErrorCode.EMPTY_KEY);
        }

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (S3Exception e) {
            log.error("[S3] Delete failed. key={} status={} awsErrorCode={} message={}",
                    key,
                    e.statusCode(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "null",
                    e.getMessage(),
                    e
            );
            throw new GeneralException(S3ErrorCode.DELETE_FAILED);
        }
    }

    public byte[] downloadBytes(String key) {
        if (key == null || key.isBlank()) {
            throw new GeneralException(S3ErrorCode.EMPTY_KEY);
        }

        try {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(getReq);
            return bytes.asByteArray();

        } catch (NoSuchKeyException e) {
            log.warn("[S3] Object not found. key={}", key, e);
            throw new GeneralException(S3ErrorCode.NOT_FOUND);
        } catch (S3Exception e) {
            log.error("[S3] Download failed. key={} status={} awsErrorCode={} message={}",
                    key,
                    e.statusCode(),
                    e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "null",
                    e.getMessage(),
                    e
            );
            throw new GeneralException(S3ErrorCode.DOWNLOAD_FAILED);
        }
    }

    private String buildCloudFrontUrl(String key) {
        String cf = props.cloudfrontDomain();
        if (cf == null || cf.isBlank()) {
            throw new GeneralException(S3ErrorCode.EMPTY_KEY);
        }

        String base = cf.endsWith("/") ? cf.substring(0, cf.length() - 1) : cf;
        String path = key.startsWith("/") ? key : "/" + key;
        return base + path;
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
