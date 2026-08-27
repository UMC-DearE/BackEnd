package com.deare.backend.api.folder.controller;

import com.deare.backend.api.folder.dto.request.FolderCreateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderCreateResponseDTO;
import com.deare.backend.api.folder.dto.response.FolderListResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderOrderRequestDTO;
import com.deare.backend.api.folder.dto.request.FolderUpdateRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderOrderResponseDTO;
import com.deare.backend.api.folder.dto.request.FolderLettersRequestDTO;
import com.deare.backend.api.folder.dto.response.FolderLettersResponseDTO;
import com.deare.backend.api.folder.dto.response.UnassignedLetterListResponseDTO;
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
            summary = "폴더 삭제",
            description = "지정한 폴더를 삭제합니다. 폴더에 포함된 편지는 삭제되지 않습니다."
    )
    public ApiResponse<Void> deleteFolder(@PathVariable("folderId") Long folderId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.deleteFolder(userId, folderId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/orders")
    @Operation(
            summary = "폴더 순서 변경",
            description = "사용자가 지정한 순서대로 폴더의 표시 순서를 일괄 변경합니다."
    )
    public ApiResponse<FolderOrderResponseDTO> updateOrders(
            @Valid @RequestBody FolderOrderRequestDTO reqDTO
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderOrderResponseDTO data = folderService.updateOrders(userId, reqDTO);
        return ApiResponse.success(data);
    }

    @PatchMapping("/{folderId}")
    @Operation(
            summary = "폴더 수정",
            description = "지정한 폴더의 이름을 수정합니다. 폴더명은 1자 이상, 6자 이하여야 합니다."
    )
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
            summary = "폴더에 편지 추가",
            description = "지정한 편지 한 개를 폴더에 추가합니다."
    )
    public ApiResponse<Void> addLetterToFolder(
            @PathVariable("folderId") Long folderId,
            @PathVariable("letterId") Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.addLetterToFolder(userId, folderId, letterId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{folderId}/letters")
    @Operation(
            summary = "폴더에 편지 일괄 추가",
            description = "선택한 여러 편지를 지정한 폴더에 한 번에 추가합니다."
    )
    public ApiResponse<FolderLettersResponseDTO> addLettersToFolder(@PathVariable Long folderId, @Valid @RequestBody FolderLettersRequestDTO reqDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        FolderLettersResponseDTO data = folderService.addLettersToFolder(userId, folderId, reqDTO);
        return ApiResponse.success("편지가 폴더에 추가되었습니다.", data);
    }

    @GetMapping("/letters/unassigned")
    @Operation(
            summary = "미분류 편지 목록 조회",
            description = "어떤 폴더에도 소속되지 않은 편지를 조회합니다. 보낸 사람, 즐겨찾기 여부, 검색어로 필터링할 수 있습니다."
    )
    public ApiResponse<UnassignedLetterListResponseDTO> getUnassignedLetters(
            Pageable pageable,
            @RequestParam(required = false) Long fromId,
            @RequestParam(required = false) Boolean isLiked,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        UnassignedLetterListResponseDTO data = folderService.getUnassignedLetters(pageable, userId, fromId, isLiked, keyword);
        return ApiResponse.success("미분류 편지 목록 조회에 성공했습니다.", data);
    }

    @DeleteMapping("/{folderId}/letters/{letterId}")
    @Operation(
            summary = "폴더에서 편지 삭제",
            description = "지정한 편지를 폴더에서 제거합니다. 편지 자체는 삭제되지 않습니다."
    )
    public ApiResponse<Void> deleteLetterFromFolder(
            @PathVariable("folderId") Long folderId,
            @PathVariable("letterId") Long letterId) {
        Long userId = SecurityUtil.getCurrentUserId();
        folderService.removeLetterFromFolder(userId, folderId, letterId);
        return ApiResponse.success(null);
    }
}
