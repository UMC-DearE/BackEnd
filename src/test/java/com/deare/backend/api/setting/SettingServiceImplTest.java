package com.deare.backend.api.setting;

import com.deare.backend.api.setting.dto.request.UpdateFontRequestDTO;
import com.deare.backend.api.setting.service.SettingServiceImpl;
import com.deare.backend.api.setting.service.SettingWriteService;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.exception.MembershipErrorCode;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingServiceImplTest {

    /**
     * 기존 초대자의 꾸미기 기능 진입 안내 검증
     * (1) 초대 성공 후 PLUS 상태가 멤버십 응답에 반영되는가?
     * (2) INVITER_FEATURE 상태일 때 해금 안내 여부가 true인가?
     */
    @Test
    void membershipResponseReturnsInviterGuideOnly() {
        UserSettingRepository repository = mock(UserSettingRepository.class);
        SettingServiceImpl service = new SettingServiceImpl(mock(SettingWriteService.class), repository);
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        setting.upgradeToPlus();
        setting.requestInviterFeatureGuide();
        when(repository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        var response = service.getMembership(1L);

        assertThat(response.isPlus()).isTrue();
        assertThat(response.showDecorationUnlockGuide()).isTrue();
    }

    /**
     * 신규 초대 가입자의 기능 화면 중복 노출 방지 검증
     * (1) 초대 가입으로 PLUS가 되어도 멤버십 응답의 안내 여부는 false인가?
     * (2) INVITEE_HOME 상태가 기능 화면용 응답으로 노출되지 않는가?
     */
    @Test
    void membershipResponseDoesNotReturnInviteeHomeGuide() {
        UserSettingRepository repository = mock(UserSettingRepository.class);
        SettingServiceImpl service = new SettingServiceImpl(mock(SettingWriteService.class), repository);
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        setting.upgradeToPlus();
        setting.requestInviteeHomeGuide();
        when(repository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        var response = service.getMembership(1L);

        assertThat(response.isPlus()).isTrue();
        assertThat(response.showDecorationUnlockGuide()).isFalse();
    }

    /**
     * FREE 사용자의 폰트 변경 권한 검증
     * (1) PLUS가 아니면 홈 꾸미기와 동일한 MEMBERSHIP 권한 오류가 발생하는가?
     * (2) 권한 오류 시 기존 폰트가 유지되는가?
     */
    @Test
    void freeUserCannotUpdateFont() {
        UserSettingRepository repository = mock(UserSettingRepository.class);
        SettingServiceImpl service = new SettingServiceImpl(mock(SettingWriteService.class), repository);
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        when(repository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        assertThatThrownBy(() -> service.updateFont(
                1L,
                new UpdateFontRequestDTO("CAFE24")
        ))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(MembershipErrorCode.PLUS_REQUIRED));

        assertThat(setting.getFont().name()).isEqualTo("PRETENDARD");
    }

    private User createUser() {
        return User.signUpUser(
                Provider.GOOGLE,
                "setting-user",
                "setting-user@example.com",
                "setting-user"
        );
    }
}
