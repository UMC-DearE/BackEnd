package com.deare.backend.domain.home.controller;

import com.deare.backend.domain.home.service.HomeService;
import com.deare.backend.domain.home.dto.HomeDashboardResponse;
import com.deare.backend.global.common.response.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/home")
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public ApiResponse<HomeDashboardResponse>getHome(){
        Long userId = 1L;
        HomeDashboardResponse data = homeService.getHome(userId);
        return ApiResponse.success(data);
    }

}
