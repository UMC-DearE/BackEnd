package com.deare.backend.api.invite.controller;

import com.deare.backend.api.invite.dto.response.InviteCodeResponseDTO;
import com.deare.backend.api.invite.dto.response.InviteValidationResponseDTO;
import com.deare.backend.api.invite.service.InviteService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Invite", description = "초대 API")
@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;

    @Operation(summary = "내 초대 코드 및 링크 발급")
    @GetMapping("/code")
    public ApiResponse<InviteCodeResponseDTO> issueCode() {
        return ApiResponse.success(
                "OK",
                "초대 링크 조회에 성공했습니다.",
                inviteService.issueCode(SecurityUtil.getCurrentUserId()));
    }

    @Operation(summary = "초대 코드 유효성 검증")
    @GetMapping("/{inviteCode}/validate")
    public ApiResponse<InviteValidationResponseDTO> validate(
            @PathVariable String inviteCode) {
        return ApiResponse.success(
                "OK",
                "유효한 초대 링크입니다.",
                inviteService.validate(inviteCode));
    }
}
