package com.deare.backend.api.user.controller;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;
import com.deare.backend.api.user.service.UserService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "프로필 조회", description = "현재 로그인한 사용자의 프로필을 조회합니다.")
    public ApiResponse<ProfileResponseDTO> getMyProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(userService.getMyProfile(userId));
    }

    @PatchMapping("/me")
    @Operation(summary = "프로필 수정", description = "프로필(이미지/닉네임/소개글)을 수정합니다.")
    public ApiResponse<ProfileUpdateResponseDTO> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(userService.updateMyProfile(userId, request));
    }
}
