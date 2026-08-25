package com.deare.backend.api.invite;

import com.deare.backend.api.invite.service.InviteService;
import com.deare.backend.api.invite.service.InviteServiceImpl;
import com.deare.backend.api.setting.service.SettingWriteServiceImpl;
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
@Import({QuerydslConfig.class, InviteServiceImpl.class, SettingWriteServiceImpl.class})
@ActiveProfiles("test")
class InviteServiceTest {

    @Autowired private InviteService inviteService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserInviteCodeRepository inviteCodeRepository;
    @Autowired private UserInviteHistoryRepository inviteHistoryRepository;
    @Autowired private UserSettingRepository userSettingRepository;

    /**
     * 초대 코드의 영구성과 재사용 검증
     * (1) 같은 사용자가 다시 요청해도 동일한 초대 코드를 반환하는가?
     * (2) 로그인 링크에 초대 코드가 쿼리 파라미터로 포함되는가?
     * (3) 발급된 코드는 유효하고 존재하지 않는 코드는 거부되는가?
     */
    @Test
    void issueCodeIsPermanentAndReusable() {
        User user = saveUser("owner");

        var first = inviteService.issueCode(user.getId());
        var second = inviteService.issueCode(user.getId());

        assertThat(second.inviteCode()).isEqualTo(first.inviteCode());
        assertThat(second.inviteUrl()).endsWith("/login?inviteCode=" + first.inviteCode());
        assertThat(inviteService.validate(first.inviteCode()).inviteCode())
                .isEqualTo(first.inviteCode());
        assertThatThrownBy(() -> inviteService.validate("missing"))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));
    }

    /**
     * 정상 초대와 중복 처리 검증
     * (1) 초대자와 초대받은 사용자 모두 PLUS 혜택을 받는가?
     * (2) 같은 초대를 다시 처리해도 초대 이력이 한 건만 유지되는가?
     */
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
        assertThat(userSettingRepository.findByUser_Id(inviter.getId())
                .orElseThrow().shouldShowInviteBenefitGuide()).isTrue();
        assertThat(userSettingRepository.findByUser_Id(invitee.getId())
                .orElseThrow().shouldShowInviteBenefitGuide()).isTrue();
    }

    /**
     * 잘못된 초대 코드 처리 검증
     * (1) GeneralException이 발생하는가?
     * (2) 오류 코드가 InviteErrorCode.INVALID_INVITE_CODE인가?
     * (3) 초대 이력과 사용자 혜택이 생성되지 않는가?
     */
    @Test
    void invalidCodeDoesNotCreateBenefit() {
        User invitee = saveUser("invitee");

        assertThatThrownBy(() -> inviteService.applySignupBenefit("missing", invitee))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));

        assertThat(inviteHistoryRepository.findAll()).isEmpty();
        assertThat(userSettingRepository.findByUser_Id(invitee.getId())).isEmpty();
    }

    /**
     * 자기 초대 방지 검증
     * (1) 자신의 초대 코드를 사용하면 INVALID_INVITE_CODE 예외가 발생하는가?
     * (2) 초대 이력과 사용자 혜택이 생성되지 않는가?
     */
    @Test
    void selfInviteDoesNotCreateBenefit() {
        User user = saveUser("self");
        inviteCodeRepository.save(UserInviteCode.create(user, "self-code"));

        assertThatThrownBy(() -> inviteService.applySignupBenefit("self-code", user))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));

        assertThat(inviteHistoryRepository.findAll()).isEmpty();
        assertThat(userSettingRepository.findByUser_Id(user.getId())).isEmpty();
    }

    /**
     * 기존 초대 이력이 있는 사용자의 잘못된 코드 검증
     * (1) 초대 이력이 있어도 존재하지 않는 코드는 INVALID_INVITE_CODE로 거부되는가?
     * (2) 기존 초대 이력이 한 건으로 유지되는가?
     */
    @Test
    void invalidCodeIsRejectedEvenWhenInviteHistoryExists() {
        User inviter = saveUser("inviter");
        User invitee = saveUser("invitee");
        inviteCodeRepository.save(UserInviteCode.create(inviter, "valid-code"));
        inviteService.applySignupBenefit("valid-code", invitee);

        assertThatThrownBy(() -> inviteService.applySignupBenefit("missing", invitee))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));

        assertThat(inviteHistoryRepository.findAll()).hasSize(1);
    }

    /**
     * 기존 초대 이력이 있는 사용자의 자기 초대 검증
     * (1) 초대 이력이 있어도 자신의 코드는 INVALID_INVITE_CODE로 거부되는가?
     * (2) 기존 초대 이력이 한 건으로 유지되는가?
     */
    @Test
    void selfInviteIsRejectedEvenWhenInviteHistoryExists() {
        User inviter = saveUser("inviter");
        User invitee = saveUser("invitee");
        inviteCodeRepository.save(UserInviteCode.create(inviter, "valid-code"));
        inviteService.applySignupBenefit("valid-code", invitee);
        inviteCodeRepository.save(UserInviteCode.create(invitee, "self-code"));

        assertThatThrownBy(() -> inviteService.applySignupBenefit("self-code", invitee))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode())
                        .isEqualTo(InviteErrorCode.INVALID_INVITE_CODE));

        assertThat(inviteHistoryRepository.findAll()).hasSize(1);
    }

    /**
     * 이미 PLUS인 초대자의 정상 초대 검증
     * (1) 초대자가 이미 PLUS여도 초대 이력이 생성되는가?
     * (2) 초대받은 사용자에게 PLUS 혜택이 적용되는가?
     */
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

    /**
     * 안내 완료 후 추가 초대 혜택의 재노출 방지 검증
     * (1) 안내 완료 상태인 초대자는 다른 친구가 가입해도 다시 노출 대기가 되지 않는가?
     * (2) 새로 가입한 초대받은 사용자는 노출 대기 상태가 되는가?
     */
    @Test
    void completedGuideIsNotReopenedByLaterInviteBenefit() {
        User inviter = saveUser("inviter");
        User firstInvitee = saveUser("first-invitee");
        User secondInvitee = saveUser("second-invitee");
        inviteCodeRepository.save(UserInviteCode.create(inviter, "shared-code"));

        inviteService.applySignupBenefit("shared-code", firstInvitee);
        UserSetting inviterSetting = userSettingRepository.findByUser_Id(inviter.getId()).orElseThrow();
        inviterSetting.completeInviteBenefitGuide();

        inviteService.applySignupBenefit("shared-code", secondInvitee);

        assertThat(inviterSetting.shouldShowInviteBenefitGuide()).isFalse();
        assertThat(userSettingRepository.findByUser_Id(secondInvitee.getId())
                .orElseThrow().shouldShowInviteBenefitGuide()).isTrue();
        assertThat(inviteHistoryRepository.findAll()).hasSize(2);
    }

    private User saveUser(String providerId) {
        return userRepository.save(User.signUpUser(
                Provider.GOOGLE, providerId, providerId + "@example.com", providerId));
    }
}
