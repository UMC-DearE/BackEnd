package com.deare.backend.api.setting.service;

import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingWriteServiceImpl implements SettingWriteService {

    private static final String DEFAULT_HOME_COLOR = "#FFFFFF";

    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void ensureSettingExists(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.INTERNAL_ERROR));
        if (userSettingRepository.findByUser_Id(userId).isPresent()) return;
        userSettingRepository.save(UserSetting.createDefault(user, DEFAULT_HOME_COLOR));
    }
}
