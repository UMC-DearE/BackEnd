package com.deare.backend.api.setting.service;

import com.deare.backend.api.setting.dto.request.UpdateFontRequestDTO;
import com.deare.backend.api.setting.dto.request.UpdateHomeColorRequestDTO;
import com.deare.backend.api.setting.dto.response.*;
import com.deare.backend.domain.setting.entity.Font;
import com.deare.backend.domain.setting.entity.MembershipPlan;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.exception.HomeColorErrorCode;
import com.deare.backend.domain.setting.exception.MembershipErrorCode;
import com.deare.backend.domain.setting.exception.ThemeErrorCode;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final SettingWriteService settingWriteService;
    private final UserSettingRepository userSettingRepository;

    public ThemeResponseDTO getTheme(Long userId) {
        settingWriteService.ensureSettingExists(userId);

        UserSetting setting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(ThemeErrorCode.THEME_INTERNAL_SERVER_ERROR));

        return new ThemeResponseDTO(setting.getTheme(), setting.getFont());
    }

    public MembershipResponseDTO getMembership(Long userId) {
        settingWriteService.ensureSettingExists(userId);

        UserSetting setting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(MembershipErrorCode.MEMBERSHIP_INTERNAL_SERVER_ERROR));

        return new MembershipResponseDTO(setting.getMembershipPlan(), setting.isPlus());
    }

    @Transactional
    public UpgradeMembershipResponseDTO upgradeMembership(Long userId) {
        // 없으면 생성
        settingWriteService.ensureSettingExists(userId);

        UserSetting setting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(MembershipErrorCode.MEMBERSHIP_INTERNAL_SERVER_ERROR));

        if (setting.getMembershipPlan() == MembershipPlan.PLUS) {
            throw new GeneralException(MembershipErrorCode.MEMBERSHIP_CONFLICT);
        }

        setting.upgradeToPlus();

        return new UpgradeMembershipResponseDTO(
                setting.getMembershipPlan(),
                true,
                setting.getUpdatedAt()
        );
    }

    @Transactional
    public UpdateFontResponseDTO updateFont(Long userId, UpdateFontRequestDTO request) {
        settingWriteService.ensureSettingExists(userId);

        UserSetting setting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(ThemeErrorCode.THEME_INTERNAL_SERVER_ERROR));

        if (!setting.isPlus()) {
            throw new GeneralException(ThemeErrorCode.THEME_FORBIDDEN);
        }

        Font font = parseFontOrThrow422(request.font());
        setting.updateFont(font);

        return new UpdateFontResponseDTO(setting.getFont(), setting.getUpdatedAt());
    }

    private Font parseFontOrThrow422(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new GeneralException(ThemeErrorCode.THEME_UNPROCESSABLE_ENTITY);
        }
        try {
            return Font.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException(ThemeErrorCode.THEME_UNPROCESSABLE_ENTITY);
        }
    }
    @Transactional
    public UpdateHomeColorResponseDTO updateHomeColor(Long userId, UpdateHomeColorRequestDTO request) {

        settingWriteService.ensureSettingExists(userId);

        UserSetting setting = userSettingRepository.findByUser_Id(userId)
                .orElseThrow(() -> new GeneralException(HomeColorErrorCode.SETTING_NOT_FOUND ));

        setting.updateHomeColor(request.homeColor());
        return new UpdateHomeColorResponseDTO(setting.getHomeColor());
    }

}
