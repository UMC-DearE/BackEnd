package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.setting.entity.MembershipPlan;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserSettingRepository userSettingRepository;
    private final JwtService jwtService;

    // 프로필 조회
    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile(Long userId) {
        // 삭제되지 않은 사용자만 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        MembershipPlan plan = userSettingRepository.findByUser_Id(userId)
                .map(UserSetting::getMembershipPlan)
                .orElse(MembershipPlan.FREE); // 없으면 FREE

        return toProfileResponse(user, plan);
    }

    // 프로필 수정
    @Override
    public ProfileUpdateResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        // 1) 기본 이미지로 초기화
        if (Boolean.TRUE.equals(request.resetProfileImage())) {
            user.setImage(null); // 기본 이미지로 변경 (연관 해제만)
        }

        // 2) 새 프로필 이미지 설정
        else if (request.imageId() != null) {
            Image image = imageRepository.findByIdAndIsDeletedFalse(request.imageId())
                    .orElseThrow(() -> new GeneralException(UserErrorCode.USER_IMAGE_NOT_FOUND));

            user.setImage(image);
        }

        // 3) 닉네임 수정 (null이면 변경하지 않음)
        if (request.nickname() != null) {
            user.updateNickname(request.nickname());
        }

        // 4) 소개글 수정 (null이면 변경하지 않음)
        if (request.intro() != null) {
            user.updateIntro(request.intro());
        }

        return toUpdateResponse(user);
    }

    // 응답 생성
    // 프로필 이미지가 없는 경우, profileImageUrl은 null로 반환
    private ProfileResponseDTO toProfileResponse(User user, MembershipPlan plan) {
        return new ProfileResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getIntro(),
                user.getImage() != null ? user.getImage().getUrl() : null,
                plan
        );
    }

    private ProfileUpdateResponseDTO toUpdateResponse(User user) {
        return new ProfileUpdateResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getIntro(),
                user.getImage() != null ? user.getImage().getUrl() : null
        );
    }

    /**
     * 회원 탈퇴 (소프트 딜리트)
     * - status를 INACTIVE로 변경
     * - isDeleted=true, deletedAt=now()
     * - RefreshToken 삭제 (강제 로그아웃)
     */
    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        user.deactivate();

        // RefreshToken 삭제 (강제 로그아웃)
        jwtService.deleteRefreshToken(userId);

        log.info("회원 탈퇴 처리 완료 - User ID: {}, Email: {}", user.getId(), user.getEmail());
    }
}
