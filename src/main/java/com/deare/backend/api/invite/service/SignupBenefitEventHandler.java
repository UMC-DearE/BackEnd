package com.deare.backend.api.invite.service;

import com.deare.backend.api.auth.event.SignupCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignupBenefitEventHandler {

    private final SignupBenefitWriteService signupBenefitWriteService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SignupCompletedEvent event) {
        try {
            signupBenefitWriteService.apply(event.inviteCode(), event.userId());
        } catch (RuntimeException exception) {
            log.error(exception.getMessage(), exception);
        }
    }
}
