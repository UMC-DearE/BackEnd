package com.deare.backend.api.user;

import com.deare.backend.domain.folder.repository.FolderRepository;
import com.deare.backend.domain.letter.repository.LetterImageRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.exception.UserErrorCode;
import com.deare.backend.domain.user.repository.UserRepository;
import com.deare.backend.global.S3.service.S3Service;
import com.deare.backend.global.auth.jwt.JwtService;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawFacade {

    private final UserRepository userRepository;
    private final LetterImageRepository letterImageRepository;
    private final FolderRepository folderRepository;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Optional<S3Service> s3Service;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        List<String> imageKeys = new ArrayList<>();
        if (user.getImage() != null) {
            imageKeys.add(user.getImage().getImageKey());
        }
        imageKeys.addAll(letterImageRepository.findImageKeysByUserId(userId));
        imageKeys.addAll(folderRepository.findImageKeysByUserId(userId));

        userRepository.delete(user);

        log.info("[탈퇴] DB 삭제 완료 - userId={}", userId);

        // DB 커밋 후 외부 리소스 정리 (베스트에포트)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cleanupS3(imageKeys, userId);
                cleanupRedis(userId);
            }
        });
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
