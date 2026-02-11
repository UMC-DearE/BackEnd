package com.deare.backend.domain.image.entity;

import com.deare.backend.domain.image.entity.enums.ContentType;
import com.deare.backend.domain.image.entity.enums.FileType;
import com.deare.backend.domain.image.entity.enums.UploadStatus;
import com.deare.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "image")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_key", nullable = false, length = 512)
    private String imageKey;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "original_file_name", nullable = false, length = 512)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private UploadStatus uploadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    private Image(
            String imageKey,
            String imageUrl,
            String originalFileName,
            FileType fileType,
            Long fileSize,
            UploadStatus uploadStatus,
            ContentType contentType
    ) {
        this.imageKey = imageKey;
        this.imageUrl = imageUrl;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.uploadStatus = uploadStatus;
        this.contentType = contentType;
    }
    public static Image create(
            String imageKey,
            String imageUrl,
            String originalFileName,
            FileType fileType,
            Long fileSize,
            ContentType contentType
    ) {
        return new Image(
                imageKey,
                imageUrl,
                originalFileName,
                fileType,
                fileSize,
                UploadStatus.COMPLETE,
                contentType
        );
    }

    public String getUrl() {
        return imageUrl;
    }
}

