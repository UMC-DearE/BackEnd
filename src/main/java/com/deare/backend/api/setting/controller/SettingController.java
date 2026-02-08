package com.deare.backend.api.setting.controller;

import com.deare.backend.api.setting.dto.request.UpdateFontRequestDTO;
import com.deare.backend.api.setting.dto.request.UpdateHomeColorRequestDTO;
import com.deare.backend.api.setting.dto.response.*;
import com.deare.backend.api.setting.service.SettingService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/theme")
    @Operation(summary = "현재 테마/폰트 조회", description = "마이페이지 테마 영역에 적용된 현재 테마(테마/폰트)를 조회합니다.")
    public ApiResponse<ThemeResponseDTO> getTheme() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(settingService.getTheme(userId));
    }

    @PatchMapping("/theme/font")
    @Operation(summary = "기본 폰트 변경(PLUS)", description = "PLUS 회원이 앱 전체 기본 폰트를 변경합니다.")
    public ApiResponse<UpdateFontResponseDTO> updateFont(
            @RequestBody UpdateFontRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(settingService.updateFont(userId, request));
    }

    @GetMapping("/membership")
    @Operation(summary = "멤버십 상태 조회", description = "현재 사용자의 멤버십 상태(FREE/PLUS)를 조회합니다.")
    public ApiResponse<MembershipResponseDTO> getMembership() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(settingService.getMembership(userId));
    }

    @PostMapping("/membership")
    @Operation(summary = "멤버십 결제(임시) - PLUS 전환", description = "PG 연동 전 임시 플로우: 결제 성공으로 간주하고 즉시 PLUS로 전환합니다.")
    public ApiResponse<UpgradeMembershipResponseDTO> upgradeMembership() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(settingService.upgradeMembership(userId));
    }
    @PatchMapping("/homecolor")
    public ApiResponse<UpdateHomeColorResponseDTO> updateHomeColor(
            @Valid @RequestBody UpdateHomeColorRequestDTO request
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(settingService.updateHomeColor(userId, request));
    }
}
