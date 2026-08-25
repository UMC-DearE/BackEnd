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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SignupBenefitOutboxServiceImplTest {

    /**
     * 혜택 처리 실패 시 Outbox 완료 방지 검증
     * (1) 혜택 처리 예외가 호출자에게 전달되는가?
     * (2) 실패한 Outbox가 COMPLETED로 변경되지 않고 PENDING을 유지하는가?
     */
    @Test
    void failedBenefitDoesNotCompleteOutbox() {
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
        SignupBenefitOutbox outbox = SignupBenefitOutbox.pending(invitee, "invalid-code");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(outbox));
        doThrow(new IllegalStateException()).when(inviteService)
                .applySignupBenefit(outbox.getInviteCode(), invitee);

        assertThatThrownBy(() -> service.process(1L))
                .isInstanceOf(IllegalStateException.class);

        assertThat(outbox.getStatus()).isEqualTo(SignupBenefitOutboxStatus.PENDING);
    }

    /**
     * 완료된 Outbox의 중복 처리 방지 검증
     * (1) 같은 Outbox를 다시 처리해도 혜택 적용은 한 번만 호출되는가?
     * (2) 완료 상태가 그대로 유지되는가?
     */
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

    /**
     * 반복 실패의 재시도 상태 관리 검증
     * (1) 실패 횟수와 마지막 오류가 기록되는가?
     * (2) 최대 시도 횟수에 도달하면 FAILED 상태와 장기 재시도 시각이 설정되는가?
     */
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

    /**
     * 실패 Outbox의 재처리 성공 검증
     * (1) 재처리가 성공하면 COMPLETED 상태로 변경되는가?
     * (2) 이전 오류와 다음 재시도 시각이 초기화되는가?
     */
    @Test
    void failedOutboxCanBeRetriedAndCompleted() {
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
        outbox.recordFailure(IllegalStateException.class.getSimpleName(), LocalDateTime.now());
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(outbox));

        service.process(1L);

        assertThat(outbox.getStatus()).isEqualTo(SignupBenefitOutboxStatus.COMPLETED);
        assertThat(outbox.getLastError()).isNull();
        assertThat(outbox.getNextAttemptAt()).isNull();
    }
}
