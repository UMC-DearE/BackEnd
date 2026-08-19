package com.deare.backend.api.invite;

import com.deare.backend.api.auth.event.SignupCompletedEvent;
import com.deare.backend.api.invite.service.SignupBenefitEventHandler;
import com.deare.backend.api.invite.service.SignupBenefitWriteService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SignupBenefitEventHandlerTest {

    @Test
    void benefitFailureDoesNotPropagateAfterSignupCommit() {
        SignupBenefitWriteService writeService = mock(SignupBenefitWriteService.class);
        SignupBenefitEventHandler handler = new SignupBenefitEventHandler(writeService);
        SignupCompletedEvent event = new SignupCompletedEvent(null, 1L);
        doThrow(new RuntimeException())
                .when(writeService).apply(event.inviteCode(), event.userId());

        assertThatCode(() -> handler.handle(event)).doesNotThrowAnyException();
        verify(writeService).apply(event.inviteCode(), event.userId());
    }
}
