package com.deare.backend.api.image.service;

import com.deare.backend.api.image.dto.response.ImageUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    ImageUploadResponseDTO uploadImage(MultipartFile file, String dir);
}
