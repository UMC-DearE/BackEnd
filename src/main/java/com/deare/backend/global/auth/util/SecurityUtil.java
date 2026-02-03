package com.deare.backend.global.auth.util;

import com.deare.backend.api.auth.exception.AuthErrorCode;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.exception.GeneralException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 인증된 사용자 정보 조회 유틸리티
 */
public class SecurityUtil {

    private SecurityUtil() {}

    /**
     * 현재 인증된 사용자 ID 조회
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user.getId();
    }

    /**
     * 현재 인증된 사용자 엔티티 조회
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(AuthErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new GeneralException(AuthErrorCode.UNAUTHORIZED);
    }
}
