package com.deare.backend.api.invite;

import com.deare.backend.api.auth.event.SignupCompletedEvent;
import com.deare.backend.api.invite.exception.InviteErrorCode;
import com.deare.backend.api.invite.service.SignupBenefitEventHandler;
import com.deare.backend.api.invite.service.SignupBenefitOutboxService;
import com.deare.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SignupBenefitEventHandlerTest {

    /**
     * 회원가입 커밋 이후 혜택 처리 실패 검증
     * (1) 혜택 처리 예외가 회원가입 완료 흐름으로 전파되지 않는가?
     * (2) 실패한 Outbox ID와 예외 유형이 실패 기록 메서드에 전달되는가?
     */
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

    /**
     * 혜택 처리 성공 시 실패 기록 방지 검증
     * (1) Outbox 처리 메서드가 호출되는가?
     * (2) 성공한 처리에는 실패 기록 메서드가 호출되지 않는가?
     */
    @Test
    void successfulBenefitProcessingDoesNotRecordFailure() {
        SignupBenefitOutboxService outboxService = mock(SignupBenefitOutboxService.class);
        SignupBenefitEventHandler handler = new SignupBenefitEventHandler(outboxService);
        SignupCompletedEvent event = new SignupCompletedEvent(1L);

        handler.handle(event);

        verify(outboxService).process(event.signupBenefitOutboxId());
        verifyNoMoreInteractions(outboxService);
    }

    /**
     * 구체적인 도메인 오류 기록 검증
     * (1) GeneralException의 클래스명이 아닌 도메인 오류 코드가 기록되는가?
     */
    @Test
    void domainFailureRecordsSpecificErrorCode() {
        SignupBenefitOutboxService outboxService = mock(SignupBenefitOutboxService.class);
        SignupBenefitEventHandler handler = new SignupBenefitEventHandler(outboxService);
        doThrow(new GeneralException(InviteErrorCode.INVALID_INVITE_CODE))
                .when(outboxService).process(1L);

        handler.process(1L);

        verify(outboxService).recordFailure(1L, InviteErrorCode.INVALID_INVITE_CODE.getCode());
    }
}
