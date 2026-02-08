package com.deare.backend.api.from.controller;

import com.deare.backend.api.from.dto.*;
import com.deare.backend.api.from.service.FromService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/from")
@RequiredArgsConstructor
public class FromController {

    private final FromService fromService;

    @GetMapping
    @Operation(
            summary = "From 목록 조회",
            description = "사용자가 보유한 From 태그 목록을 조회하는 API입니다."
    )
    public ApiResponse<FromListResponseDTO> getFroms() {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(
                fromService.getFroms(userId)
        );
    }

    @PostMapping
    @Operation(
            summary = "From 생성",
            description = "사용자가 새로운 From 태그를 생성하는 API입니다."
    )
    public ApiResponse<FromCreateResponseDTO> createFrom(
            @Valid @RequestBody FromCreateRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(
                fromService.createFrom(userId, request)
        );
    }

    @PatchMapping("/{fromId}")
    @Operation(
            summary = "From 수정",
            description = "사용자가 기존 From 태그의 이름 또는 색상을 수정하는 API입니다."
    )
    public ApiResponse<FromUpdateResponseDTO> updateFrom(
            @PathVariable Long fromId,
            @Valid @RequestBody FromUpdateRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(
                fromService.updateFrom(userId, fromId, request)
        );
    }

    @DeleteMapping("/{fromId}")
    @Operation(
            summary = "From 삭제",
            description = "사용자가 생성한 From 태그를 삭제하는 API입니다."
    )
    public ApiResponse<FromDeleteDTO> deleteFrom(
            @PathVariable Long fromId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(
                "삭제가 완료되었습니다.",
                fromService.deleteFrom(userId, fromId)
        );
    }
}
