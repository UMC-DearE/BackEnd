package com.deare.backend.api.from.controller;

import com.deare.backend.api.from.dto.FromCreateRequestDTO;
import com.deare.backend.api.from.dto.FromCreateResponseDTO;
import com.deare.backend.api.from.dto.FromListResponseDTO;
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
}
