package com.deare.backend.api.home;

import com.deare.backend.api.home.dto.response.HomeDashboardResponse;
import com.deare.backend.api.home.exception.HomeErrorCode;
import com.deare.backend.api.home.service.HomeServiceImpl;
import com.deare.backend.api.setting.service.SettingWriteService;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeServiceImplTest {

    /**
     * 홈 조회의 친구 초대 혜택 안내 노출 상태 검증
     * (1) 노출 대기 상태이면 홈 응답이 true를 반환하는가?
     * (2) 홈 조회만으로 노출 대기 상태가 완료 처리되지 않는가?
     */
    @Test
    void pendingInviteBenefitGuideIsReturnedWithoutBeingConsumed() {
        UserRepository userRepository = mock(UserRepository.class);
        UserSettingRepository settingRepository = mock(UserSettingRepository.class);
        UserStickerRepository stickerRepository = mock(UserStickerRepository.class);
        HomeServiceImpl service = new HomeServiceImpl(
                userRepository,
                settingRepository,
                stickerRepository,
                mock(ImageRepository.class),
                mock(SettingWriteService.class)
        );
        User user = createUser();
        UserSetting setting = UserSetting.createDefault(user, "#FFFFFF");
        setting.requestInviteBenefitGuide();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));
        when(stickerRepository.findAllByUser_IdOrderByPosZAsc(1L)).thenReturn(List.of());

        HomeDashboardResponse response = service.getHome(1L);

        assertThat(response.getSetting().isShowInviteBenefitGuide()).isTrue();
        assertThat(setting.shouldShowInviteBenefitGuide()).isTrue();
    }

    /**
     * 친구 초대 혜택 안내 노출 완료 처리 검증
     * (1) 완료 후 안내 노출 여부가 false가 되는가?
     * (2) 완료 처리를 반복해도 상태가 유지되는가?
     */
    @Test
    void completingInviteBenefitGuideIsIdempotent() {
        UserSettingRepository settingRepository = mock(UserSettingRepository.class);
        HomeServiceImpl service = new HomeServiceImpl(
                mock(UserRepository.class),
                settingRepository,
                mock(UserStickerRepository.class),
                mock(ImageRepository.class),
                mock(SettingWriteService.class)
        );
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        setting.requestInviteBenefitGuide();
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        service.completeInviteBenefitGuide(1L);
        service.completeInviteBenefitGuide(1L);

        assertThat(setting.shouldShowInviteBenefitGuide()).isFalse();
    }

    /**
     * 친구 초대 혜택 안내 완료 시 설정 미존재 검증
     * (1) 사용자 설정이 없으면 HOME_40402 오류를 반환하는가?
     */
    @Test
    void missingSettingCannotCompleteInviteBenefitGuide() {
        UserSettingRepository settingRepository = mock(UserSettingRepository.class);
        HomeServiceImpl service = new HomeServiceImpl(
                mock(UserRepository.class),
                settingRepository,
                mock(UserStickerRepository.class),
                mock(ImageRepository.class),
                mock(SettingWriteService.class)
        );
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeInviteBenefitGuide(1L))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(
                        ((GeneralException) error).getErrorCode()
                ).isEqualTo(HomeErrorCode.USER_SETTING_NOT_FOUND));
    }

    private User createUser() {
        return User.signUpUser(
                Provider.GOOGLE,
                "home-user",
                "home-user@example.com",
                "home-user"
        );
    }
}
