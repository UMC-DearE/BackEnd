package com.deare.backend.global.scheduler;

import com.deare.backend.domain.emotion.repository.LetterEmotionRepository;
import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.from.repository.FromRepository;
import com.deare.backend.domain.letter.entity.Letter;
import com.deare.backend.domain.letter.repository.LetterRepository;
import com.deare.backend.domain.report.repository.ReportRepository;
import com.deare.backend.domain.setting.repository.UserSettingRepository;
import com.deare.backend.domain.sticker.repository.UserStickerRepository;
import com.deare.backend.domain.term.repository.UserTermRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 소프트 딜리트된 유저 정리 스케줄러
 * - 삭제 후 30일 지난 유저를 하드 딜리트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private static final int DELETE_AFTER_DAYS = 30;

    private final UserRepository userRepository;
    private final LetterEmotionRepository letterEmotionRepository;
    private final LetterRepository letterRepository;
    private final FromRepository fromRepository;
    private final FolderRepository folderRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserTermRepository userTermRepository;
    private final UserStickerRepository userStickerRepository;
    private final ReportRepository reportRepository;

    /**
     *  매일 새벽 3시에 실행 - 30일 지난 소프트 딜리트 유저 -> 하드 딜리트
     *
     * 삭제 순서 (FK 의존관계 역순):
     * LetterEmotion (Letter FK)
     * Letter (User FK, From FK, Folder FK) - LetterImage는 cascade로 자동 삭제
     * From (User FK)
     * Folder (User FK)
     * UserSetting (User FK)
     * UserTerm (User FK)
     * UserSticker (User FK)
     * Report (User FK)
     * User -> 하드 딜리트 성공
     *
     * image는 고아 객체라서 계속 존재
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedUsers() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(DELETE_AFTER_DAYS);
        List<User> usersToDelete = userRepository.findUsersToHardDelete(threshold);

        if (usersToDelete.isEmpty()) {
            log.info("[UserCleanupScheduler] 삭제 대상 유저 없음");
            return;
        }

        log.info("[UserCleanupScheduler] 삭제 대상 유저 수: {}", usersToDelete.size());

        for (User user : usersToDelete) {
            try {
                hardDeleteUserData(user);
                log.info("[UserCleanupScheduler] 유저 하드 딜리트 완료 - User ID: {}, Email: {}",
                        user.getId(), user.getEmail());
            } catch (Exception e) {
                log.error("[UserCleanupScheduler] 유저 하드 딜리트 실패 - User ID: {}, Error: {}",
                        user.getId(), e.getMessage(), e);
            }
        }

        log.info("[UserCleanupScheduler] 스케줄러 실행 완료");
    }

    /**
     * 유저 관련 모든 데이터 하드 딜리트
     */
    private void hardDeleteUserData(User user) {
        Long userId = user.getId();

        // LetterEmotion 삭제 (Letter FK 참조)
        letterEmotionRepository.deleteAllByUserId(userId);
        // Letter 삭제 (LetterImage는 cascade=ALL, orphanRemoval=true로 자동 삭제)
        List<Letter> letters = letterRepository.findAllByUser_Id(userId);
        letterRepository.deleteAll(letters);
        // From 삭제
        fromRepository.deleteAllByUserId(userId);
        // Folder 삭제
        folderRepository.deleteAllByUserId(userId);
        // UserSetting 삭제
        userSettingRepository.deleteByUserId(userId);
        // UserTerm 삭제
        userTermRepository.deleteAllByUserId(userId);
        // UserSticker 삭제
        userStickerRepository.deleteAllByUserId(userId);
        // Report 삭제
        reportRepository.deleteAllByUserId(userId);

        // User 삭제
        userRepository.delete(user);
    }
}
