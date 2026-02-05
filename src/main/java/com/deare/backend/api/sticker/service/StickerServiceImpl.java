package com.deare.backend.api.sticker.service;

import com.deare.backend.api.sticker.dto.StickerCreateRequestDTO;
import com.deare.backend.api.sticker.dto.StickerCreateResponseDTO;
import com.deare.backend.api.sticker.dto.StickerUpdateResponseDTO;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional

public class StickerServiceImpl {
    private final UserStickerRepository userStickerRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    @Override
    public StickerCreateResponseDTO create(Long userId, StickerCreateRequestDTO request){
        User user=userRepository.findById(userId).orElseThrow(()->new GeneralException(UserErrorCode.USER_NOT_FOUND));
        Image image=imageRepository.findById(request.imageId()).orElseThrow(()->new GeneralException(ImageErrorCode.IMAGE_40001));

    }
}
