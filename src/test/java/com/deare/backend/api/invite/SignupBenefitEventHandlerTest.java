package com.deare.backend.api.invite;

import com.deare.backend.api.auth.event.SignupCompletedEvent;
import com.deare.backend.api.invite.service.SignupBenefitEventHandler;
import com.deare.backend.api.invite.service.SignupBenefitOutboxService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SignupBenefitEventHandlerTest {

    @Test
    void benefitFailureDoesNotPropagateAfterSignupCommit() {
        SignupBenefitOutboxService outboxService = mock(SignupBenefitOutboxService.class);
        SignupBenefitEventHandler handler = new SignupBenefitEventHandler(outboxService);
        SignupCompletedEvent event = new SignupCompletedEvent(1L);
        doThrow(new IllegalStateException())
                .when(outboxService).process(event.signupBenefitOutboxId());

        assertThatCode(() -> handler.handle(event)).doesNotThrowAnyException();
        verify(outboxService).process(event.signupBenefitOutboxId());
        verify(outboxService).recordFailure(
                event.signupBenefitOutboxId(),
                IllegalStateException.class.getSimpleName()
        );
    }

    @Test
    void successfulBenefitProcessingDoesNotRecordFailure() {
        SignupBenefitOutboxService outboxService = mock(SignupBenefitOutboxService.class);
        SignupBenefitEventHandler handler = new SignupBenefitEventHandler(outboxService);
        SignupCompletedEvent event = new SignupCompletedEvent(1L);

        handler.handle(event);

        verify(outboxService).process(event.signupBenefitOutboxId());
        verifyNoMoreInteractions(outboxService);
    }
}
