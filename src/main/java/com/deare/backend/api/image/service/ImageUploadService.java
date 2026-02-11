package com.deare.backend.api.image.service;

import com.deare.backend.api.image.dto.response.ImageUploadResponseDTO;
import com.deare.backend.domain.image.entity.enums.ContentType;
import com.deare.backend.domain.image.entity.enums.FileType;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.global.common.exception.GeneralException;
import com.deare.backend.global.S3.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
@Slf4j
public class ImageUploadService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    public ImageUploadResponseDTO uploadImage(MultipartFile file, String dir) {

        if (file == null || file.isEmpty()) {
            throw new GeneralException(ImageErrorCode.IMAGE_40001);
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new GeneralException(ImageErrorCode.IMAGE_40001);
        }

        ContentType contentType = ContentType.from(dir);
        FileType fileType = FileType.from(originalFileName);

        S3Service.UploadedFile uploaded;
        try {
            uploaded = s3Service.upload(file, contentType.getDirName());
        } catch (Exception e) {
            log.error(
                    "S3 업로드 실패: originalFileName={}, dir={}, size={}",
                    originalFileName,
                    contentType.getDirName(),
                    file.getSize(),
                    e
            );

            throw new GeneralException(ImageErrorCode.IMAGE_50001);
        }

        Image image = Image.create(
                uploaded.key(),
                uploaded.url(),
                originalFileName,
                fileType,
                file.getSize(),
                contentType
        );

        Image saved = imageRepository.save(image);

        return new ImageUploadResponseDTO(
                saved.getId(),
                saved.getImageKey(),
                saved.getImageUrl()
        );
    }
}
