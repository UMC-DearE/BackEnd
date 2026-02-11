package com.deare.backend.api.image.controller;

import com.deare.backend.api.image.dto.response.ImageUploadResponseDTO;
import com.deare.backend.api.image.service.ImageUploadService;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class ImageController {

    private final ImageUploadService imageUploadService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(
            summary = "이미지 업로드",
            description = "사용자가 이미지를 업로드하는 API입니다. 파일과 dir(profile, letter, sticker, folder)을 multipart/form-data로 전송합니다."
    )
    public ApiResponse<ImageUploadResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart("dir") String dir
    ) {
        return ApiResponse.success(
                imageUploadService.uploadImage(file, dir)
        );
    }
}