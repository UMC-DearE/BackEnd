package com.deare.backend.domain.user.repository;

import com.deare.backend.domain.user.entity.enums.Provider;
import com.deare.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByIdAndIsDeletedFalse(Long userId);

    /**
     * 활성 유저 조회 (로그인 시 사용)
     */
    Optional<User> findByProviderAndProviderIdAndIsDeletedFalse(Provider provider, String providerId);

    /**
     * 삭제 중인 유저 조회 (복구 분기용 처리)
     */
    Optional<User> findByProviderAndProviderIdAndIsDeletedTrue(Provider provider, String providerId);

    /**
     * 스케줄러용 - 삭제 후 30일 지난 유저 조회
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = true AND u.deletedAt < :threshold")
    List<User> findUsersToHardDelete(@Param("threshold") LocalDateTime threshold);
}
