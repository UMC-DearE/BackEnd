package com.deare.backend.api.home.service;

import com.deare.backend.api.home.dto.HomeDashboardResponse;
import com.deare.backend.api.home.dto.HomeSettingDto;
import com.deare.backend.api.home.dto.HomeStickerDto;
import com.deare.backend.api.home.dto.HomeUserDto;
import com.deare.backend.api.home.exception.HomeErrorCode;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.entity.UserSticker;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserStickerRepository stickerRepository;

    @Transactional(readOnly=true)
    public HomeDashboardResponse getHome(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(HomeErrorCode.USER_NOT_FOUND));

        String profileImageUrl = user.getImage() != null
                ? user.getImage().getImageUrl()
                : null;

        HomeUserDto userDto = new HomeUserDto(
                user.getId(),
                user.getNickname(),
                user.getIntro(),
                profileImageUrl
        );

        UserSetting userSetting = userSettingRepository.findByUser_Id(userId)
                .orElse(null);

        String homeColor = (userSetting != null)
                ? userSetting.getHomeColor()
                : "#FFFFFF";

        HomeSettingDto settingDto = new HomeSettingDto(homeColor);

        List<HomeStickerDto> stickerDtos = stickerRepository
                .findAllByUser_IdOrderByPosZAsc(userId)
                .stream()
                .map(this::toStickerDto)
                .toList();

        return new HomeDashboardResponse(
                userDto,
                settingDto,
                stickerDtos
        );
    }

    private HomeStickerDto toStickerDto(UserSticker sticker) {
        return new HomeStickerDto(
                sticker.getId(),
                sticker.getImage().getId(),
                sticker.getImage().getImageUrl(),
                sticker.getPosX(),
                sticker.getPosY(),
                sticker.getPosZ(),
                sticker.getRotation(),
                sticker.getScale()
        );
    }
}
