package com.deare.backend.api.home.controller;

import com.deare.backend.api.home.dto.response.HomeDashboardResponse;
import com.deare.backend.api.home.service.HomeService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(
            summary = "홈 대시보드 조회",
            description = "사용자의 홈 대시보드(유저 정보, 배경색, 스티커 목록)를 조회하는 API입니다."
    )
    public ApiResponse<HomeDashboardResponse> getHome() {
        Long userId = SecurityUtil.getCurrentUserId();
        HomeDashboardResponse res = homeService.getHome(userId);
        return ApiResponse.success(res);
    }
}
