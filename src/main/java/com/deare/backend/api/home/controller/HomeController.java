package com.deare.backend.api.home.controller;

import com.deare.backend.api.home.dto.request.HomeEditRequestDTO;
import com.deare.backend.api.home.dto.response.HomeDashboardResponse;
import com.deare.backend.api.home.service.HomeService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping
    @Operation(
            summary = "홈 화면 편집 저장",
            description = "스티커 배치 , 배경색 변경 등 홈화면 편집 내용을 저장하는 API입니다."
    )
    public ApiResponse<Void> editHome(@Valid @RequestBody HomeEditRequestDTO request){
        Long userId = SecurityUtil.getCurrentUserId();
        homeService.editHome(userId, request);
        return ApiResponse.success(null);
    }

}
