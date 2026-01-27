package com.deare.backend.api.folder;

import com.deare.backend.domain.folder.dto.FolderCreateRequestDTO;
import com.deare.backend.domain.folder.dto.FolderCreateResponseDTO;
import com.deare.backend.domain.folder.dto.FolderListResponseDTO;
import com.deare.backend.domain.folder.dto.FolderOrderRequestDTO;
import com.deare.backend.domain.folder.dto.FolderUpdateRequestDTO;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folders")
public class FolderController {

    @GetMapping
    @Operation(
            summary = "폴더 목록 조회",
            description = "폴더 목록을 리스트 형태로 조회합니다. 폴더 아이템은 사용자가 설정한 순서대로 반환됩니다."
    )
    public ApiResponse<FolderListResponseDTO> getFolderList() {
        return ApiResponse.success(null);
    }

    @PostMapping
    @Operation(
            summary = "폴더 생성",
            description = "1자 이상, 6자 이하로 폴더명을 설정해야 합니다."
    )
    public ApiResponse<FolderCreateResponseDTO> createFolder(@Valid @RequestBody FolderCreateRequestDTO reqDTO) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{folderId}")
    @Operation(
            summary = "폴더 삭제"
    )
    public ApiResponse<Void> deleteFolder(@PathVariable("folderId") Long folderId) {
        return ApiResponse.success(null);
    }

    @PatchMapping("/orders")
    @Operation(
            summary = "폴더 순서 변경"
    )
    public ApiResponse<Void> updateOrders(@Valid @RequestBody FolderOrderRequestDTO reqDTO) {
        return ApiResponse.success(null);
    }

    @PatchMapping("/{folderId}")
    @Operation(
            summary = "폴더 수정"
    )
    public ApiResponse<Void> updateFolder(
            @PathVariable("folderId") Long folderId,
            @Valid @RequestBody FolderUpdateRequestDTO reqDTO) {
        return ApiResponse.success(null);
    }


    @PostMapping("/{folderId}/letters/{letterId}")
    @Operation(
            summary = "폴더에 편지 추가"
    )
    public ApiResponse<Void> addLetterToFolder(@PathVariable("folderId") Long folderId, @PathVariable("letterId") Long letterId) {
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{folderId}/letters/{letterId}")
    @Operation(
            summary = "폴더에서 편지 삭제"
    )
    public ApiResponse<Void> deleteLetterFromFolder(@PathVariable("folderId") Long folderId, @PathVariable("letterId") Long letterId) {
        return ApiResponse.success(null);
    }
}