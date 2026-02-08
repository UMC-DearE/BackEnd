package com.deare.backend.api.letter.controller;

import com.deare.backend.api.letter.dto.*;
import com.deare.backend.api.letter.service.LetterService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @GetMapping
    @Operation(
            summary = "편지 목록 조회",
            description = "사용자가 소유한 편지 목록을 조회하는 API입니다. 검색, 필터, 정렬, 보기 모드 데이터를 한 번에 제공합니다."
    )
    public ApiResponse<LetterListResponseDTO> getLetterLists(
            Pageable pageable,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Boolean isLiked,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(
                letterService.getLetterList(pageable, userId, folderId, fromId, isLiked, keyword)
        );
    }


    @GetMapping("/{letterId}")
    @Operation(
            summary = "편지 상세 조회",
            description = "사용자가 소유한 편지를 ID로 상세 조회하는 API입니다."
    )
    public ApiResponse<LetterDetailResponseDTO> getLetter(
            @PathVariable Long letterId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        return ApiResponse.success(letterService.getLetterDetail(userId, letterId));
    }


    @PatchMapping("/{letterId}")
    @Operation(summary = "편지 내용 수정",
            description = "사용자가 소유한 편지를 수정하는 API입니다.")
    public ApiResponse<Void> updateLetter(
            @PathVariable Long letterId,
            @Valid @RequestBody LetterUpdateRequestDTO reqDTO
            ){
        Long userId = SecurityUtil.getCurrentUserId();

        letterService.updateLetter(userId, letterId, reqDTO);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{letterId}")
    @Operation(summary = "편지 삭제",
            description = "사용자가 소유한 편지를 삭제하는 API입니다.")
    public ApiResponse<Void> deleteLetter(@PathVariable Long letterId){
        Long userId = SecurityUtil.getCurrentUserId();
        letterService.deleteLetter(userId, letterId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{letterId}/reply")
    @Operation(summary = "편지 답장 등록/수정",
            description = "사용자가 소유한 편지의 답장을 등록/수정하는 API입니다.")
    public ApiResponse<Void> upsertReply(
            @PathVariable Long letterId,
            @Valid @RequestBody LetterReplyUpsertRequestDTO reqDTO
    ){
        Long userId = SecurityUtil.getCurrentUserId();
        letterService.upsertReply(userId, letterId, reqDTO);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{letterId}/reply")
    @Operation(summary = "편지 답장 삭제",
            description = "사용자가 소유한 편지의 답장을 삭제하는 API입니다.")
    public ApiResponse<Void> deleteReply(
            @PathVariable Long letterId
    ){
        Long userId = SecurityUtil.getCurrentUserId();
        letterService.deleteReply(userId, letterId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{letterId}/like")
    @Operation(
            summary = "편지 좋아요 추가",
            description = "사용자가 소유한 편지에 좋아요를 추가합니다."
    )
    public ApiResponse<LetterLikeResponseDTO> likeLetter(@PathVariable Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        LetterLikeResponseDTO res = letterService.likeLetter(userId, letterId);
        return ApiResponse.success(res);
    }

    @DeleteMapping("/{letterId}/like")
    @Operation(
            summary = "편지 좋아요 삭제",
            description = "사용자가 소유한 편지의 좋아요를 삭제합니다."
    )
    public ApiResponse<LetterLikeResponseDTO> unlikeLetter(@PathVariable Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        LetterLikeResponseDTO res = letterService.unlikeLetter(userId, letterId);
        return ApiResponse.success(res);
    }
}