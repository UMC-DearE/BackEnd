package com.deare.backend.api.invite.service;

import com.deare.backend.domain.user.entity.User;

import java.util.List;

public interface SignupBenefitOutboxService {

    Long enqueue(String inviteCode, User invitee);

    void process(Long outboxId);

    void recordFailure(Long outboxId, String errorType);

    List<Long> findRetryableIds(int batchSize);
}
