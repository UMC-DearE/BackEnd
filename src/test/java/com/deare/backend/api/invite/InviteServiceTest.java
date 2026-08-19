package com.deare.backend.api.invite;

import com.deare.backend.api.invite.service.InviteService;
import com.deare.backend.api.invite.exception.InviteErrorCode;
import com.deare.backend.domain.invite.entity.UserInviteCode;
import com.deare.backend.domain.invite.repository.UserInviteCodeRepository;
import com.deare.backend.domain.invite.repository.UserInviteHistoryRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.config.QuerydslConfig;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({QuerydslConfig.class, InviteService.class})
@ActiveProfiles("test")
class InviteServiceTest {

    @Autowired private InviteService inviteService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserInviteCodeRepository inviteCodeRepository;
    @Autowired private UserInviteHistoryRepository inviteHistoryRepository;
    @Autowired private UserSettingRepository userSettingRepository;

    @Test
    void issueCodeIsPermanentAndReusable() {
        User user = saveUser("owner");

        var first = inviteService.issueCode(user.getId());
        var second = inviteService.issueCode(user.getId());

        assertThat(second.inviteCode()).isEqualTo(first.inviteCode());
        assertThat(second.inviteUrl()).endsWith("/invite/" + first.inviteCode());
        assertThat(inviteService.validate(first.inviteCode()).inviteCode())
                .isEqualTo(first.inviteCode());
        assertThatThrownBy(() -> inviteService.validate("missing"))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));
    }

    @Test
    void successfulInviteUpgradesBothUsersAndStoresHistoryOnce() {
        User inviter = saveUser("inviter");
        User invitee = saveUser("invitee");
        UserInviteCode code = inviteCodeRepository.save(UserInviteCode.create(inviter, "welcome"));

        inviteService.applySignupBenefit(code.getInviteCode(), invitee);
        inviteService.applySignupBenefit(code.getInviteCode(), invitee);

        assertThat(inviteHistoryRepository.findAll()).hasSize(1);
        assertThat(userSettingRepository.findByUser_Id(inviter.getId()).orElseThrow().isPlus()).isTrue();
        assertThat(userSettingRepository.findByUser_Id(invitee.getId()).orElseThrow().isPlus()).isTrue();
    }

    @Test
    void invalidCodeDoesNotCreateBenefit() {
        User invitee = saveUser("invitee");

        inviteService.applySignupBenefit("missing", invitee);

        assertThat(inviteHistoryRepository.findAll()).isEmpty();
        assertThat(userSettingRepository.findByUser_Id(invitee.getId())).isEmpty();
    }

    @Test
    void alreadyPlusInviterStillAllowsInviteeBenefit() {
        User inviter = saveUser("inviter");
        User invitee = saveUser("invitee");
        UserSetting inviterSetting = UserSetting.createDefault(inviter, "#FFFFFF");
        inviterSetting.upgradeToPlus();
        userSettingRepository.save(inviterSetting);
        inviteCodeRepository.save(UserInviteCode.create(inviter, "plus-owner"));

        inviteService.applySignupBenefit("plus-owner", invitee);

        assertThat(inviteHistoryRepository.findAll()).hasSize(1);
        assertThat(userSettingRepository.findByUser_Id(invitee.getId()).orElseThrow().isPlus()).isTrue();
    }

    private User saveUser(String providerId) {
        return userRepository.save(User.signUpUser(
                Provider.GOOGLE, providerId, providerId + "@example.com", providerId));
    }
}
