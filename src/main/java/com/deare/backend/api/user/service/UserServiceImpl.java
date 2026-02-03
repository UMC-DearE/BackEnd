package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.ProfileResponseDTO;
import com.deare.backend.api.user.dto.ProfileUpdateRequestDTO;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    // 프로필 조회
    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile(Long userId) {
        // 삭제되지 않은 사용자만 조회
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_40401));

        return toResponse(user);
    }

    // 프로필 수정
    @Override
    public ProfileResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_40401));

        // 1) 기본 이미지로 초기화
        if (Boolean.TRUE.equals(request.resetProfileImage())) {
            user.setImage(null); // 기본 이미지로 변경 (연관 해제만)
        }

        // 2) 새 프로필 이미지 설정
        else if (request.imageId() != null) {
            Image image = imageRepository.findByIdAndIsDeletedFalse(request.imageId())
                    .orElseThrow(() -> new GeneralException(UserErrorCode.USER_40402));

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

        return toResponse(user);
    }

    // 응답 생성
    // 프로필 이미지가 없는 경우, profileImageUrl은 null로 반환
    private ProfileResponseDTO toResponse(User user) {
        String imageUrl = (user.getImage() == null)
                ? null
                : user.getImage().getUrl();

        return new ProfileResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getIntro(),
                imageUrl
        );
    }
}
