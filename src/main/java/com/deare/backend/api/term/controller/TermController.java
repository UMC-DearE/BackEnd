package com.deare.backend.api.term.controller;

import com.deare.backend.api.term.dto.response.TermListResponseDTO;
import com.deare.backend.api.term.service.TermService;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @GetMapping
    @Operation(summary = "약관 목록 조회", description = "마이페이지에서 약관 목록을 조회합니다. type 파라미터로 필터링 가능합니다. (SERVICE / PRIVACY / MARKETING)")
    public ApiResponse<TermListResponseDTO> getTerms(
            @RequestParam(required = false) String type
    ) {
        return ApiResponse.success(termService.getTerms(type));
    }
}
