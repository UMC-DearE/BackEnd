package com.deare.backend.api.home.controller;

import com.deare.backend.api.home.service.HomeService;
import com.deare.backend.api.home.dto.HomeDashboardResponse;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
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
    public ApiResponse<HomeDashboardResponse>getHome(){
        Long userId = SecurityUtil.getCurrentUserId();
        HomeDashboardResponse data = homeService.getHome(userId);
        return ApiResponse.success(data);
    }

}
