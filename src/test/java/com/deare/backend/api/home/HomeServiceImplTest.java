package com.deare.backend.api.home;

import com.deare.backend.api.home.dto.request.HomeEditRequestDTO;
import com.deare.backend.api.home.dto.response.HomeDashboardResponse;
import com.deare.backend.api.home.service.HomeServiceImpl;
import com.deare.backend.api.setting.service.SettingWriteService;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.exception.MembershipErrorCode;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        setting.requestInviteeHomeGuide();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));
        when(stickerRepository.findAllByUser_IdOrderByPosZAsc(1L)).thenReturn(List.of());

        HomeDashboardResponse response = service.getHome(1L);

        assertThat(response.getSetting().isShowDecorationUnlockGuide()).isTrue();
        assertThat(setting.shouldShowInviteeHomeGuide()).isTrue();
    }

    /**
     * 기존 초대자의 홈 화면 미노출 검증
     * (1) 기능 페이지 노출 대기 상태여도 홈 응답은 false인가?
     * (2) 홈 조회만으로 기능 페이지 노출 대기 상태가 완료되지 않는가?
     */
    @Test
    void inviterFeatureGuideIsNotReturnedOnHome() {
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
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        setting.requestInviterFeatureGuide();
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));
        when(stickerRepository.findAllByUser_IdOrderByPosZAsc(1L)).thenReturn(List.of());

        HomeDashboardResponse response = service.getHome(1L);

        assertThat(response.getSetting().isShowDecorationUnlockGuide()).isFalse();
        assertThat(setting.shouldShowInviterFeatureGuide()).isTrue();
    }

    /**
     * 초대 가입 환영 안내 완료 처리의 멱등성 검증
     * (1) 노출 대기 상태가 완료 상태로 변경되는가?
     * (2) 완료 API를 반복 호출해도 안내가 다시 노출되지 않는가?
     */
    @Test
    void completingInviteGuideIsIdempotent() {
        UserSettingRepository settingRepository = mock(UserSettingRepository.class);
        HomeServiceImpl service = new HomeServiceImpl(
                mock(UserRepository.class),
                settingRepository,
                mock(UserStickerRepository.class),
                mock(ImageRepository.class),
                mock(SettingWriteService.class)
        );
        UserSetting setting = UserSetting.createDefault(createUser(), "#FFFFFF");
        setting.requestInviterFeatureGuide();
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        service.completeInviteGuide(1L);
        service.completeInviteGuide(1L);

        assertThat(setting.shouldShowDecorationUnlockGuide()).isFalse();
    }

    /**
     * FREE 사용자의 홈 꾸미기 저장 권한 검증
     * (1) PLUS가 아니면 MEMBERSHIP 권한 오류가 발생하는가?
     * (2) 권한 오류 시 홈 색상과 기존 스티커가 변경되지 않는가?
     */
    @Test
    void freeUserCannotEditHome() {
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(settingRepository.findByUser_Id(1L)).thenReturn(Optional.of(setting));

        assertThatThrownBy(() -> service.editHome(
                1L,
                new HomeEditRequestDTO("#000000", List.of())
        ))
                .isInstanceOf(GeneralException.class)
                .satisfies(error -> assertThat(((GeneralException) error).getErrorCode())
                        .isEqualTo(MembershipErrorCode.PLUS_REQUIRED));

        assertThat(setting.getHomeColor()).isEqualTo("#FFFFFF");
        verify(stickerRepository, never()).deleteAllByUserId(1L);
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
