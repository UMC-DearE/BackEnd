package com.deare.backend.api.report.controller;

import com.deare.backend.api.report.dto.response.ReportReanalyzeResponseDTO;
import com.deare.backend.api.report.dto.response.ReportResponseDTO;
import com.deare.backend.api.report.service.ReportService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "리포트 조회",
            description = "From 랭킹, AI 성향 분석 결과, 재분석 가능 여부를 함께 반환합니다."
    )
    @GetMapping
    public ApiResponse<ReportResponseDTO> getReport() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(reportService.getReport(userId));
    }


    @Operation(
            summary = "다시 분석하기",
            description = "AI 성향 분석을 재실행합니다."
    )
    @PostMapping("/reanalyze")
    public ApiResponse<ReportReanalyzeResponseDTO> reanalyze() {
        Long userId = SecurityUtil.getCurrentUserId();
        ReportReanalyzeResponseDTO result = reportService.reanalyze(userId);
        return ApiResponse.success("AI 성향 분석에 성공하였습니다.", result);
    }
}
