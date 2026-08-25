package com.deare.backend.api.invite.service;

import com.deare.backend.api.auth.event.SignupCompletedEvent;
import com.deare.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignupBenefitEventHandler {

    private final SignupBenefitOutboxService signupBenefitOutboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SignupCompletedEvent event) {
        process(event.signupBenefitOutboxId());
    }

    public void process(Long outboxId) {
        try {
            signupBenefitOutboxService.process(outboxId);
        } catch (RuntimeException exception) {
            recordFailure(outboxId, exception);
        }
    }

    private void recordFailure(Long outboxId, RuntimeException processingException) {
        try {
            signupBenefitOutboxService.recordFailure(
                    outboxId,
                    failureReason(processingException)
            );
        } catch (RuntimeException recordingException) {
            processingException.addSuppressed(recordingException);
        }
        log.error("회원가입 초대 혜택 처리 실패 - Outbox ID: {}", outboxId, processingException);
    }

    private String failureReason(RuntimeException exception) {
        if (exception instanceof GeneralException generalException) {
            return generalException.getErrorCode().getCode();
        }
        return exception.getClass().getSimpleName();
    }
}
