package com.deare.backend.api.report.controller;

import com.deare.backend.api.report.dto.response.ReportStatsResponseDTO;
import com.deare.backend.api.report.service.ReportStatsService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportStatsController {

    private final ReportStatsService reportStatsService;

    @Operation(
            summary = "리포트 통계 조회",
            description = "통계에 필요한 데이터 반환합니다."
    )
    @GetMapping
    public ApiResponse<ReportStatsResponseDTO> getStatics(
    ){
        Long userId= SecurityUtil.getCurrentUserId();
        ReportStatsResponseDTO response=reportStatsService.getDummyStatics(userId);

        return ApiResponse.success(response);
    }
}
