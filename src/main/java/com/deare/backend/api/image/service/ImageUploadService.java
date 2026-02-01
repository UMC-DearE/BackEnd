package com.deare.backend.api.image.service;

import com.deare.backend.api.image.dto.ImageUploadResponseDTO;
import com.deare.backend.domain.image.entity.ContentType;
import com.deare.backend.domain.image.entity.FileType;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.global.common.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.global.service.S3Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageUploadService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    public ImageUploadResponseDTO uploadImage(MultipartFile file, String dir){
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
            // contentType에 dirName 같은 게 없다면 contentType.name().toLowerCase()로 바꾸면 됨
            uploaded = s3Service.upload(file, contentType.getDirName());
        } catch (Exception e) {
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




