package com.deare.backend.api.invite.service;

import com.deare.backend.domain.invite.entity.SignupBenefitOutbox;
import com.deare.backend.domain.invite.repository.SignupBenefitOutboxRepository;
import com.deare.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupBenefitOutboxServiceImpl implements SignupBenefitOutboxService {

    private final SignupBenefitOutboxRepository outboxRepository;
    private final InviteService inviteService;

    @Override
    @Transactional
    public Long enqueue(String inviteCode, User invitee) {
        return outboxRepository.save(SignupBenefitOutbox.pending(invitee, inviteCode)).getId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long outboxId) {
        SignupBenefitOutbox outbox = outboxRepository.findByIdForUpdate(outboxId).orElse(null);
        if (outbox == null || !outbox.isRetryable()) return;

        inviteService.applySignupBenefit(outbox.getInviteCode(), outbox.getInvitee());
        outbox.complete();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long outboxId, String errorType) {
        SignupBenefitOutbox outbox = outboxRepository.findByIdForUpdate(outboxId).orElse(null);
        if (outbox == null || !outbox.isRetryable()) return;

        outbox.recordFailure(errorType, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findRetryableIds(int batchSize) {
        return outboxRepository.findRetryableIds(
                LocalDateTime.now(),
                PageRequest.of(0, Math.max(1, batchSize))
        );
    }
}
