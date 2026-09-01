package com.deare.backend.api.user;

import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.report.repository.ReportAnalysisRepository;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.S3.service.S3Service;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawFacade {

    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final LetterImageRepository letterImageRepository;
    private final LetterEmotionRepository letterEmotionRepository;
    private final FolderRepository folderRepository;
    private final FromRepository fromRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserTermRepository userTermRepository;
    private final UserStickerRepository userStickerRepository;
    private final ReportAnalysisRepository reportAnalysisRepository;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Optional<S3Service> s3Service;

    public void withdraw(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(UserErrorCode.USER_NOT_FOUND);
        }

        // S3 키 수집 (삭제 전에 미리)
        List<String> imageKeys = collectImageKeys(userId);

        // 자식 엔티티 삭제 — 각각 독립 트랜잭션, 실패해도 탈퇴 계속
        tryDelete("ReportAnalysis", () -> reportAnalysisRepository.deleteAllByUserId(userId));
        tryDelete("UserSticker",    () -> userStickerRepository.deleteAllByUserId(userId));
        tryDelete("UserTerm",       () -> userTermRepository.deleteAllByUserId(userId));
        tryDelete("UserSetting",    () -> userSettingRepository.deleteByUserId(userId));
        tryDelete("LetterEmotion",  () -> letterEmotionRepository.deleteAllByUserId(userId));
        // letter 삭제 시 ON DELETE CASCADE로 letter_image, letter_search_token 자동 삭제
        tryDelete("Letter",         () -> letterRepository.deleteAllByUserId(userId));
        tryDelete("Folder",         () -> folderRepository.deleteAllByUserId(userId));
        tryDelete("UserFrom",       () -> fromRepository.deleteAllByUserId(userId));

        // 유저 삭제 — ON DELETE CASCADE가 남은 고아 객체 처리
        userRepository.findById(userId).ifPresent(user -> {
            userRepository.delete(user);
            log.info("[탈퇴] 유저 DB 삭제 완료 - userId={}", userId);
        });

        // 외부 리소스 정리 (베스트에포트)
        cleanupS3(imageKeys, userId);
        cleanupRedis(userId);
    }

    private List<String> collectImageKeys(Long userId) {
        List<String> keys = new ArrayList<>();
        try {
            userRepository.findProfileImageKeyByUserId(userId).ifPresent(keys::add);
        } catch (Exception e) {
            log.error("[탈퇴] 프로필 이미지 키 조회 실패 - userId={}", userId, e);
        }
        try {
            keys.addAll(letterImageRepository.findImageKeysByUserId(userId));
        } catch (Exception e) {
            log.error("[탈퇴] 편지 이미지 키 조회 실패 - userId={}", userId, e);
        }
        try {
            keys.addAll(folderRepository.findImageKeysByUserId(userId));
        } catch (Exception e) {
            log.error("[탈퇴] 폴더 이미지 키 조회 실패 - userId={}", userId, e);
        }
        return keys;
    }

    private void tryDelete(String label, Runnable deleteOp) {
        try {
            deleteOp.run();
        } catch (Exception e) {
            log.error("[탈퇴] {} 삭제 실패 (CASCADE로 처리됨) - {}", label, e.getMessage());
        }
    }

    private void cleanupS3(List<String> imageKeys, Long userId) {
        s3Service.ifPresent(s3 -> {
            for (String key : imageKeys) {
                try {
                    s3.delete(key);
                } catch (Exception e) {
                    log.error("[탈퇴] S3 이미지 삭제 실패 - userId={} key={}", userId, key, e);
                }
            }
        });
    }

    private void cleanupRedis(Long userId) {
        try {
            jwtService.deleteRefreshToken(userId);
        } catch (Exception e) {
            log.error("[탈퇴] Redis RT 삭제 실패 - userId={}", userId, e);
        }
        try {
            redisTemplate.delete("letters:pinned:" + userId);
        } catch (Exception e) {
            log.error("[탈퇴] Redis pinned 삭제 실패 - userId={}", userId, e);
        }
    }
}
