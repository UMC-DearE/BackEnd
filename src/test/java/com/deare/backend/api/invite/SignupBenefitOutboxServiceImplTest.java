package com.deare.backend.api.invite;

import com.deare.backend.api.invite.service.InviteService;
import com.deare.backend.api.invite.service.SignupBenefitOutboxServiceImpl;
import com.deare.backend.domain.invite.entity.SignupBenefitOutbox;
import com.deare.backend.domain.invite.entity.enums.SignupBenefitOutboxStatus;
import com.deare.backend.domain.invite.repository.SignupBenefitOutboxRepository;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.domain.user.entity.enums.Provider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SignupBenefitOutboxServiceImplTest {

    @Test
    void completedOutboxIsNotProcessedTwice() {
        SignupBenefitOutboxRepository repository = mock(SignupBenefitOutboxRepository.class);
        InviteService inviteService = mock(InviteService.class);
        SignupBenefitOutboxServiceImpl service =
                new SignupBenefitOutboxServiceImpl(repository, inviteService);
        User invitee = User.signUpUser(
                Provider.GOOGLE,
                "invitee",
                "invitee@example.com",
                "invitee"
        );
        SignupBenefitOutbox outbox = SignupBenefitOutbox.pending(invitee, "invite-code");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(outbox));

        service.process(1L);
        service.process(1L);

        verify(inviteService).applySignupBenefit(outbox.getInviteCode(), invitee);
        verifyNoMoreInteractions(inviteService);
        assertThat(outbox.getStatus()).isEqualTo(SignupBenefitOutboxStatus.COMPLETED);
    }

    @Test
    void repeatedFailureUsesLongBackoffAndRemainsRecoverable() {
        User invitee = User.signUpUser(
                Provider.GOOGLE,
                "invitee",
                "invitee@example.com",
                "invitee"
        );
        SignupBenefitOutbox outbox = SignupBenefitOutbox.pending(invitee, "invite-code");
        LocalDateTime failedAt = LocalDateTime.now();

        for (int attempt = 0; attempt < 10; attempt++) {
            outbox.recordFailure(IllegalStateException.class.getSimpleName(), failedAt);
        }

        assertThat(outbox.getAttemptCount()).isEqualTo(10);
        assertThat(outbox.getStatus()).isEqualTo(SignupBenefitOutboxStatus.FAILED);
        assertThat(outbox.getNextAttemptAt()).isEqualTo(failedAt.plusHours(6));
        assertThat(outbox.getLastError())
                .isEqualTo(IllegalStateException.class.getSimpleName());
    }
}
