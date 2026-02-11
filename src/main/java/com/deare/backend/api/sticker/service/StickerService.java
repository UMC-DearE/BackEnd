package com.deare.backend.api.sticker.service;

import com.deare.backend.api.sticker.dto.request.StickerCreateRequestDTO;
import com.deare.backend.api.sticker.dto.request.StickerUpdateRequestDTO;
import com.deare.backend.api.sticker.dto.response.StickerCreateResponseDTO;
import com.deare.backend.api.sticker.dto.response.StickerDeleteResponseDTO;
import com.deare.backend.api.sticker.dto.response.StickerUpdateResponseDTO;

public interface StickerService {
    StickerCreateResponseDTO create(Long userId, StickerCreateRequestDTO request);
    StickerUpdateResponseDTO update(Long userId, Long stickerId, StickerUpdateRequestDTO request);
    StickerDeleteResponseDTO delete(Long userId, Long stickerId);
}
