package com.deare.backend.api.invite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupBenefitRetryScheduler {

    private final SignupBenefitOutboxService signupBenefitOutboxService;
    private final SignupBenefitEventHandler eventHandler;

    @Value("${app.signup-benefit.retry-batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.signup-benefit.retry-delay-ms:60000}")
    public void retryPendingBenefits() {
        signupBenefitOutboxService.findRetryableIds(batchSize)
                .forEach(eventHandler::process);
    }
}
