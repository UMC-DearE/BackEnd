package com.deare.backend.api.setting.service;

import com.deare.backend.api.setting.dto.request.UpdateFontRequestDTO;
import com.deare.backend.api.setting.dto.response.*;
import com.deare.backend.domain.setting.entity.Font;
import com.deare.backend.domain.setting.entity.MembershipPlan;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.exception.MembershipErrorCode;
import com.deare.backend.domain.setting.exception.ThemeErrorCode;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private static final String DEFAULT_HOME_COLOR = "#FFFFFF";

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    public ThemeResponseDTO getTheme(Long userId) {
        UserSetting setting = getOrCreateSetting(userId);
        return new ThemeResponseDTO(setting.getTheme(), setting.getFont());
    }

    @Transactional
    public UpdateFontResponseDTO updateFont(Long userId, UpdateFontRequestDTO request) {
        UserSetting setting = getOrCreateSetting(userId);

        // PLUS만 가능
        if (!setting.isPlus()) {
            throw new GeneralException(ThemeErrorCode.THEME_FORBIDDEN);
        }

        Font font = parseFontOrThrow422(request.font());
        setting.updateFont(font);

        LocalDateTime updatedAt = setting.getUpdatedAt();
        return new UpdateFontResponseDTO(setting.getFont(), updatedAt);
    }

    public MembershipResponseDTO getMembership(Long userId) {
        UserSetting setting = getOrCreateSetting(userId);
        return new MembershipResponseDTO(setting.getMembershipPlan(), setting.isPlus());
    }

    @Transactional
    public UpgradeMembershipResponseDTO upgradeMembership(Long userId) {
        UserSetting setting = getOrCreateSetting(userId);

        // 이미 PLUS면 409
        if (setting.getMembershipPlan() == MembershipPlan.PLUS) {
            throw new GeneralException(MembershipErrorCode.MEMBERSHIP_CONFLICT);
        }

        setting.upgradeToPlus();

        LocalDateTime updatedAt = setting.getUpdatedAt();
        return new UpgradeMembershipResponseDTO(setting.getMembershipPlan(), true, updatedAt);
    }

    private UserSetting getOrCreateSetting(Long userId) {
        return userSettingRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new GeneralException(ThemeErrorCode.THEME_INTERNAL_SERVER_ERROR));

                    UserSetting created = UserSetting.createDefault(user, DEFAULT_HOME_COLOR);
                    return userSettingRepository.save(created);
                });
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
}
