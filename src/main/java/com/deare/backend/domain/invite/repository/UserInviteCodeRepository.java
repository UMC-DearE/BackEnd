package com.deare.backend.domain.invite.repository;

import com.deare.backend.domain.invite.entity.UserInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInviteCodeRepository extends JpaRepository<UserInviteCode, Long> {

    Optional<UserInviteCode> findByUserId(Long userId);

    Optional<UserInviteCode> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    @Query("""
            SELECT code
            FROM UserInviteCode code
            JOIN FETCH code.user
            WHERE code.inviteCode = :inviteCode
            """)
    Optional<UserInviteCode> findWithUserByInviteCode(
            @Param("inviteCode") String inviteCode);
}
