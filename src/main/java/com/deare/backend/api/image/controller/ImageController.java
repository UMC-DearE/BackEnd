package com.deare.backend.api.image.controller;

import com.deare.backend.api.image.dto.ImageUploadResponseDTO;
import com.deare.backend.api.image.service.ImageUploadService;
import com.deare.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping
    public ApiResponse<ImageUploadResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "dir", required = false) String dir
    ) {
        return ApiResponse.success(
                imageUploadService.uploadImage(file, dir)
        );
    }
}
