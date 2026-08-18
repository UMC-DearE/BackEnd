package com.deare.backend.domain.invite.repository;

import com.deare.backend.domain.invite.entity.UserInviteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInviteHistoryRepository extends JpaRepository<UserInviteHistory, Long> {

    List<UserInviteHistory> findAllByInviterId(Long inviterId);

    boolean existsByInviteeId(Long inviteeId);
}
