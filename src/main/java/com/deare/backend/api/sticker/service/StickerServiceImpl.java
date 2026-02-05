package com.deare.backend.api.sticker.service;

import com.deare.backend.api.sticker.dto.*;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.sticker.entity.UserSticker;
import com.deare.backend.domain.sticker.exception.StickerErrorCode;
import com.deare.backend.domain.sticker.repository.UserStickerImageQueryRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StickerServiceImpl implements StickerService {

    private final UserStickerRepository userStickerRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserStickerImageQueryRepository userStickerImageQueryRepository;

    @Override
    public StickerCreateResponseDTO create(Long userId, StickerCreateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        Image image = imageRepository.findById(request.imageId())
                .orElseThrow(() -> new GeneralException(ImageErrorCode.IMAGE_40001));

        UserSticker sticker = UserSticker.create(
                user,
                image,
                request.posX(),
                request.posY(),
                request.posZ(),
                request.rotation(),
                request.scale()
        );

        UserSticker saved = userStickerRepository.save(sticker);
        return new StickerCreateResponseDTO(saved.getId());
    }

    @Override
    public StickerUpdateResponseDTO update(Long userId, Long stickerId, StickerUpdateRequestDTO request) {
        UserSticker sticker = userStickerRepository.findById(stickerId)
                .orElseThrow(() -> new GeneralException(StickerErrorCode.NOT_FOUND));

        if (!sticker.isOwnedBy(userId)) {
            throw new GeneralException(StickerErrorCode.FORBIDDEN);
        }

        sticker.updateTransform(
                request.posX(),
                request.posY(),
                request.posZ(),
                request.rotation(),
                request.scale()
        );

        return new StickerUpdateResponseDTO(sticker.getId());
    }

    @Override
    public StickerDeleteDTO delete(Long userId, Long stickerId) {
        UserSticker sticker = userStickerRepository.findById(stickerId)
                .orElseThrow(() -> new GeneralException(StickerErrorCode.NOT_FOUND));

        if (!sticker.isOwnedBy(userId)) {
            throw new GeneralException(StickerErrorCode.FORBIDDEN);
        }

        userStickerRepository.delete(sticker);
        return new StickerDeleteDTO(stickerId);
    }
}

