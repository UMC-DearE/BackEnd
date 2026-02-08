package com.deare.backend.api.user.controller;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;
import com.deare.backend.api.user.service.UserService;
import com.deare.backend.global.auth.cookie.CookieProvider;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CookieProvider cookieProvider;

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

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리합니다. 30일 후 완전 삭제되며, 기한 이전에 다시 로그인하면 계정이 복구됩니다.")
    public ResponseEntity<ApiResponse<Void>> deactivateUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.deactivateUser(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProvider.expireRefreshTokenCookie().toString())
                .body(ApiResponse.success("회원 탈퇴가 완료되었습니다. 30일 후 계정이 완전히 삭제됩니다.", null));
    }
}
