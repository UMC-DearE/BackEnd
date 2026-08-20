package com.deare.backend.api.folder.controller;

import com.deare.backend.api.folder.dto.request.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderListResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderOrderRequestDTO;
import com.deare.backend.api.folder.dto.request.FolderUpdateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderOrderResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderLettersRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderLettersResponseDTO;
import com.deare.backend.api.letter.dto.response.LetterListResponseDTO;
import com.deare.backend.api.folder.service.FolderService;
import com.deare.backend.global.auth.util.SecurityUtil;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    @Operation(
            summary = "폴더 목록 조회",
            description = "폴더 목록을 리스트 형태로 조회합니다. 폴더 아이템은 사용자가 설정한 순서대로 반환됩니다."
    )
    public ApiResponse<FolderListResponseDTO> getFolderList() {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderListResponseDTO data = folderService.getFolderList(userId);
        return ApiResponse.success(data);
    }

    @PostMapping
    @Operation(
            summary = "폴더 생성",
            description = "1자 이상, 6자 이하로 폴더명을 설정해야 합니다."
    )
    public ApiResponse<FolderCreateResponseDTO> createFolder(@Valid @RequestBody FolderCreateRequestDTO reqDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderCreateResponseDTO data = folderService.createFolder(userId, reqDTO);
        return ApiResponse.success(data);
    }

    @DeleteMapping("/{folderId}")
    @Operation(
            summary = "폴더 삭제"
    )
    public ApiResponse<Void> deleteFolder(@PathVariable("folderId") Long folderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.deleteFolder(userId, folderId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/orders")
    @Operation(summary = "폴더 순서 변경")
    public ApiResponse<FolderOrderResponseDTO> updateOrders(
            @Valid @RequestBody FolderOrderRequestDTO reqDTO
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderOrderResponseDTO data = folderService.updateOrders(userId, reqDTO);
        return ApiResponse.success(data);
    }

    @PatchMapping("/{folderId}")
    @Operation(summary = "폴더 수정")
    public ApiResponse<Void> updateFolder(
            @PathVariable("folderId") Long folderId,
            @Valid @RequestBody FolderUpdateRequestDTO reqDTO
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.updateFolder(userId, folderId, reqDTO);
        return ApiResponse.success(null);
    }

    @PostMapping("/{folderId}/letters/{letterId}")
    @Operation(
            summary = "폴더에 편지 추가"
    )
    public ApiResponse<Void> addLetterToFolder(
            @PathVariable("folderId") Long folderId,
            @PathVariable("letterId") Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.addLetterToFolder(userId, folderId, letterId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{folderId}/letters")
    public ApiResponse<FolderLettersResponseDTO> addLettersToFolder(@PathVariable Long folderId, @Valid @RequestBody FolderLettersRequestDTO reqDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderLettersResponseDTO data = folderService.addLettersToFolder(userId, folderId, reqDTO);
        return ApiResponse.success("COMMON200", "편지가 폴더에 추가되었습니다.", data);
    }

    @GetMapping("/{folderId}/letters/available")
    public ApiResponse<LetterListResponseDTO> getAvailableLetters(
            @PathVariable Long folderId,
            Pageable pageable,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Boolean isLiked,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        LetterListResponseDTO data = folderService.getAvailableLetters(pageable, userId, folderId, fromId, isLiked, keyword);
        return ApiResponse.success("COMMON200", "추가 가능한 편지 목록 조회에 성공했습니다.", data);
    }

    @DeleteMapping("/{folderId}/letters/{letterId}")
    @Operation(
            summary = "폴더에서 편지 삭제"
    )
    public ApiResponse<Void> deleteLetterFromFolder(
            @PathVariable("folderId") Long folderId,
            @PathVariable("letterId") Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.removeLetterFromFolder(userId, folderId, letterId);
        return ApiResponse.success(null);
    }
}
