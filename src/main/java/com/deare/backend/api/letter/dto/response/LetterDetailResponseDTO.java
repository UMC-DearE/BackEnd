package com.deare.backend.api.letter.dto.response;

import com.deare.backend.api.letter.dto.result.EmotionTagDTO;
import com.deare.backend.api.letter.dto.result.LetterFolderDTO;
import com.deare.backend.api.letter.dto.result.LetterFromDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LetterDetailResponseDTO(
        String content,
        LocalDate receivedAt,
        String aiSummary,
        boolean isLiked,
        String reply,
        LetterFromDTO from,
        LocalDateTime createdAt,
        LetterFolderDTO folder,
        List<EmotionTagDTO> emotionTags,
        List<String> imageUrls
) {}
