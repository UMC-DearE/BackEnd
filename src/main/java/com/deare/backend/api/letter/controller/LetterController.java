package com.deare.backend.api.letter.controller;

import com.deare.backend.domain.letter.dto.LetterDetailResponseDTO;
import com.deare.backend.domain.letter.dto.LetterListResponseDTO;
import com.deare.backend.domain.letter.dto.LetterReplyUpsertRequestDTO;
import com.deare.backend.domain.letter.dto.LetterUpdateRequestDTO;
import com.deare.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/letters")
public class LetterController {

    @GetMapping
    @Operation(
            summary = "편지 목록 조회",
            description = "사용자가 소유한 편지 목록을 조회하는 API입니다. 검색, 필터, 정렬, 보기 모드 데이터를 한 번에 제공합니다."
    )
    public ApiResponse<LetterListResponseDTO> getLetterLists() {
        return ApiResponse.success(null);
    }


    @GetMapping("/{id}")
    @Operation(
            summary = "편지 상세 조회",
            description = "사용자가 소유한 편지를 ID로 상세 조회하는 API입니다."
    )
    public ApiResponse<LetterDetailResponseDTO> getLetter(
            @PathVariable Long id
    ) {
        return ApiResponse.success(null);
    }


    @PatchMapping("/{id}")
    @Operation(summary = "편지 내용 수정",
            description = "사용자가 소유한 편지를 수정하는 API입니다.")
    public ApiResponse<Void> updateLetter(
            @PathVariable Long id,
            @Valid @RequestBody LetterUpdateRequestDTO reqDTO
            ){
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "편지 삭제",
            description = "사용자가 소유한 편지를 삭제하는 API입니다.")
    public ApiResponse<Void> deleteLetter(@PathVariable Long id){
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/reply")
    @Operation(summary = "편지 답장 등록/수정/삭제",
            description = "사용자가 소유한 편지의 답장을 등록/수정/삭제하는 API입니다.")
    public ApiResponse<Void> updateReply(
            @PathVariable Long id,
            @Valid @RequestBody LetterReplyUpsertRequestDTO request
    ){
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/like")
    @Operation(summary = "편지 좋아요 추가/삭제",
            description = "사용자가 소유한 편지에 좋아요를 추가/삭제하는 API입니다.")
    public ApiResponse<Void> changeLetterLike(@PathVariable Long id){
        return ApiResponse.success(null);
    }
}
