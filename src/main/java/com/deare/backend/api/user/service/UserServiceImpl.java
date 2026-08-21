package com.deare.backend.api.user.service;

import com.deare.backend.api.user.dto.response.ProfileResponseDTO;
import com.deare.backend.api.user.dto.request.ProfileUpdateRequestDTO;
import com.deare.backend.api.user.dto.response.ProfileUpdateResponseDTO;
import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.image.entity.Image;
import com.deare.backend.domain.image.repository.ImageRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.report.repository.ReportAnalysisRepository;
import com.deare.backend.domain.setting.entity.enums.MembershipPlan;
import com.deare.backend.domain.setting.entity.UserSetting;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserSettingRepository userSettingRepository;
    private final JwtService jwtService;
    private final LetterEmotionRepository letterEmotionRepository;
    private final LetterRepository letterRepository;
    private final FromRepository fromRepository;
    private final FolderRepository folderRepository;
    private final UserTermRepository userTermRepository;
    private final UserStickerRepository userStickerRepository;
    private final ReportAnalysisRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        MembershipPlan plan = userSettingRepository.findByUser_Id(userId)
                .map(UserSetting::getMembershipPlan)
                .orElse(MembershipPlan.FREE);

        return toProfileResponse(user, plan);
    }

    @Override
    public ProfileUpdateResponseDTO updateMyProfile(Long userId, ProfileUpdateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(request.resetProfileImage())) {
            user.setImage(null);
        } else if (request.imageId() != null) {
            Image image = imageRepository.findByIdAndIsDeletedFalse(request.imageId())
                    .orElseThrow(() -> new GeneralException(UserErrorCode.USER_IMAGE_NOT_FOUND));
            user.setImage(image);
        }

        if (request.nickname() != null) {
            user.updateNickname(request.nickname());
        }

        if (request.intro() != null) {
            user.updateIntro(request.intro());
        }

        return toUpdateResponse(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        // cascade 삭제 (FK 역순)
        letterEmotionRepository.deleteAllByUserId(userId);
        List<Letter> letters = letterRepository.findAllByUser_Id(userId);
        letterRepository.deleteAll(letters);
        fromRepository.deleteAllByUserId(userId);
        folderRepository.deleteAllByUserId(userId);
        userSettingRepository.deleteByUserId(userId);
        userTermRepository.deleteAllByUserId(userId);
        userStickerRepository.deleteAllByUserId(userId);
        reportRepository.deleteAllByUserId(userId);
        userRepository.delete(user);

        // DB 삭제 완료 후 Redis RefreshToken 삭제
        jwtService.deleteRefreshToken(userId);

        log.info("회원 탈퇴 처리 완료 - User ID: {}, Email: {}", userId, user.getEmail());
    }

    private ProfileResponseDTO toProfileResponse(User user, MembershipPlan plan) {
        return new ProfileResponseDTO(
                user.getId(),
                user.getNickname(),
                user.getIntro(),
                user.getImage() != null ? user.getImage().getUrl() : null,
                plan,
                user.getEmail(),
                user.getProvider()
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
}
