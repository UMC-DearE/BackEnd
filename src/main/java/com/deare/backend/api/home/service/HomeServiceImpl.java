package com.deare.backend.api.home.service;

import com.deare.backend.api.home.dto.request.HomeEditRequestDTO;
import com.deare.backend.api.home.dto.request.HomeEditRequestDTO.StickerRequest;
import com.deare.backend.api.home.dto.response.HomeDashboardResponse;
import com.deare.backend.api.home.dto.result.HomeSettingDto;
import com.deare.backend.api.home.dto.result.HomeStickerDto;
import com.deare.backend.api.home.dto.result.HomeUserDto;
import com.deare.backend.api.home.exception.HomeErrorCode;
import com.deare.backend.api.setting.service.SettingWriteService;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.exception.ImageErrorCode;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.exception.MembershipErrorCode;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserStickerRepository stickerRepository;
    private final ImageRepository imageRepository;
    private final SettingWriteService settingWriteService;

    @Override
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
                : "#F7F7F7F7";

        HomeSettingDto settingDto = new HomeSettingDto(
                homeColor,
                userSetting != null && userSetting.shouldShowInviteeHomeGuide()
        );

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

    @Override
    @Transactional
    public void editHome(Long userId, HomeEditRequestDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(HomeErrorCode.USER_NOT_FOUND));

        settingWriteService.ensureSettingExists(userId);
        UserSetting userSetting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(HomeErrorCode.HOME_INTERNAL_ERROR));
        if (!userSetting.isPlus()) {
            throw new GeneralException(MembershipErrorCode.PLUS_REQUIRED);
        }
        userSetting.updateHomeColor(request.homeColor());

        stickerRepository.deleteAllByUserId(userId);

        List<StickerRequest> stickerRequests = request.stickers();
        if (stickerRequests.isEmpty()) {
            return;
        }

        List<Long> imageIds = stickerRequests.stream()
                .map(HomeEditRequestDTO.StickerRequest::imageId)
                .distinct()
                .toList();

        Map<Long, Image> imageMap = imageRepository.findAllById(imageIds).stream()
                .collect(Collectors.toMap(Image::getId, image -> image));

        List<UserSticker> newStickers = stickerRequests.stream()
                .map(dto -> {
                    Image image = imageMap.get(dto.imageId());
                    if (image == null) {
                        throw new GeneralException(ImageErrorCode.IMAGE_40001);
                    }
                    return UserSticker.create(
                            user, image,
                            dto.posX(), dto.posY(), dto.posZ(),
                            dto.rotation(), dto.scale()
                    );
                })
                .toList();

        stickerRepository.saveAll(newStickers);
    }

    @Override
    @Transactional
    public void completeInviteGuide(Long userId) {
        settingWriteService.ensureSettingExists(userId);
        UserSetting userSetting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(HomeErrorCode.HOME_INTERNAL_ERROR));
        userSetting.completeDecorationUnlockGuide();
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
