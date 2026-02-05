package com.deare.backend.api.sticker.controller;

import com.deare.backend.api.sticker.dto.*;
import com.deare.backend.api.sticker.service.StickerService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stickers")
@RequiredArgsConstructor
public class StickerController {

    private final StickerService stickerService;

    @PostMapping
    @Operation(
            summary = "스티커 추가",
            description = "홈 화면에 스티커를 추가하는 API입니다."
    )
    public ApiResponse<StickerCreateResponseDTO> createSticker(
            @Valid @RequestBody StickerCreateRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(stickerService.create(userId, request));
    }

    @PatchMapping("/{stickerId}")
    @Operation(
            summary = "스티커 수정",
            description = "홈 화면에 배치된 스티커의 위치/회전/크기를 수정하는 API입니다."
    )
    public ApiResponse<StickerUpdateResponseDTO> updateSticker(
            @PathVariable Long stickerId,
            @Valid @RequestBody StickerUpdateRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(stickerService.update(userId, stickerId, request));
    }

    @DeleteMapping("/{stickerId}")
    @Operation(
            summary = "스티커 삭제",
            description = "홈 화면에 배치된 스티커를 삭제하는 API입니다."
    )
    public ApiResponse<StickerDeleteDTO> deleteSticker(
            @PathVariable Long stickerId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(stickerService.delete(userId, stickerId));
    }
}
