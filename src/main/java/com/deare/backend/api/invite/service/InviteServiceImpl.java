package com.deare.backend.api.invite.service;

import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.api.invite.dto.response.InviteCodeResponseDTO;
import com.deare.backend.api.invite.dto.response.InviteValidationResponseDTO;
import com.deare.backend.api.invite.exception.InviteErrorCode;
import com.deare.backend.domain.invite.entity.UserInviteCode;
import com.deare.backend.domain.invite.entity.UserInviteHistory;
import com.deare.backend.domain.invite.repository.UserInviteCodeRepository;
import com.deare.backend.domain.invite.repository.UserInviteHistoryRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int CODE_LENGTH = 12;
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private static final String DEFAULT_HOME_COLOR = "#FFFFFF";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserInviteCodeRepository inviteCodeRepository;
    private final UserInviteHistoryRepository inviteHistoryRepository;
    private final UserSettingRepository userSettingRepository;

    @Value("${app.frontend-base-url:https://www.deare.kr}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public InviteCodeResponseDTO issueCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.USER_NOT_FOUND));
        UserInviteCode code = inviteCodeRepository.findByUserId(userId)
                .orElseGet(() -> createUniqueCode(user));
        String link = UriComponentsBuilder.fromUriString(frontendBaseUrl)
                .pathSegment("invite", code.getInviteCode())
                .build().toUriString();
        return new InviteCodeResponseDTO(code.getInviteCode(), link);
    }

    @Override
    @Transactional(readOnly = true)
    public InviteValidationResponseDTO validate(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()
                || !inviteCodeRepository.existsByInviteCode(inviteCode)) {
            throw new GeneralException(InviteErrorCode.INVALID_INVITE_CODE);
        }
        return new InviteValidationResponseDTO(inviteCode);
    }

    @Override
    @Transactional
    public void applySignupBenefit(String inviteCode, User invitee) {
        if (inviteCode == null || inviteCode.isBlank()
                || inviteHistoryRepository.existsByInviteeId(invitee.getId())) return;
        User inviter = inviteCodeRepository.findWithUserByInviteCode(inviteCode)
                .map(UserInviteCode::getUser).orElse(null);
        if (inviter == null || inviter.getId().equals(invitee.getId())) return;

        inviteHistoryRepository.saveAndFlush(UserInviteHistory.create(inviter, invitee));
        upgradeToPlus(inviter);
        upgradeToPlus(invitee);
    }

    private UserInviteCode createUniqueCode(User user) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String value = randomCode();
            if (!inviteCodeRepository.existsByInviteCode(value)) {
                try {
                    return inviteCodeRepository.saveAndFlush(UserInviteCode.create(user, value));
                } catch (DataIntegrityViolationException ignored) {
                    UserInviteCode existing = inviteCodeRepository.findByUserId(user.getId()).orElse(null);
                    if (existing != null) return existing;
                }
            }
        }
        throw new GeneralException(InviteErrorCode.LINK_PROCESSING_FAILED);
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++)
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return code.toString();
    }

    private void upgradeToPlus(User user) {
        UserSetting setting = userSettingRepository.findByUser_Id(user.getId())
                .orElseGet(() -> userSettingRepository.save(
                        UserSetting.createDefault(user, DEFAULT_HOME_COLOR)));
        if (!setting.isPlus()) setting.upgradeToPlus();
    }
}
