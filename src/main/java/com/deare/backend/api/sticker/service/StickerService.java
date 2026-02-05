package com.deare.backend.api.sticker.service;

import com.deare.backend.api.sticker.dto.*;

public interface StickerService {
    StickerCreateResponseDTO create(Long userId, StickerCreateRequestDTO request);
    StickerUpdateResponseDTO update(Long userId, Long stickerId, StickerUpdateRequestDTO request);
    StickerDeleteDTO delete(Long userId, Long stickerId);
}
