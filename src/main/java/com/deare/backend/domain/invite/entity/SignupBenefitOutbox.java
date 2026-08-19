package com.deare.backend.domain.invite.entity;

import com.deare.backend.domain.invite.entity.enums.SignupBenefitOutboxStatus;
import com.deare.backend.domain.user.entity.User;
import com.deare.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "signup_benefit_outbox",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_signup_benefit_outbox_invitee",
                columnNames = "invitee_user_id"
        ),
        indexes = @Index(
                name = "idx_signup_benefit_outbox_retry",
                columnList = "status,next_attempt_at"
        )
)
public class SignupBenefitOutbox extends BaseTimeEntity {

    private static final int MAX_ATTEMPTS = 10;
    private static final long MAX_RETRY_DELAY_MINUTES = 60;
    private static final long FAILED_RETRY_DELAY_HOURS = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signup_benefit_outbox_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitee_user_id", nullable = false)
    private User invitee;

    @Column(name = "invite_code", nullable = false, length = 64)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SignupBenefitOutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", length = 255)
    private String lastError;

    public static SignupBenefitOutbox pending(User invitee, String inviteCode) {
        SignupBenefitOutbox outbox = new SignupBenefitOutbox();
        outbox.invitee = invitee;
        outbox.inviteCode = inviteCode;
        outbox.status = SignupBenefitOutboxStatus.PENDING;
        outbox.nextAttemptAt = LocalDateTime.now();
        return outbox;
    }

    public boolean isRetryable() {
        return status != SignupBenefitOutboxStatus.COMPLETED;
    }

    public void complete() {
        status = SignupBenefitOutboxStatus.COMPLETED;
        nextAttemptAt = null;
        lastError = null;
    }

    public void recordFailure(String errorType, LocalDateTime failedAt) {
        attemptCount++;
        lastError = errorType;
        if (attemptCount >= MAX_ATTEMPTS) {
            status = SignupBenefitOutboxStatus.FAILED;
            nextAttemptAt = failedAt.plusHours(FAILED_RETRY_DELAY_HOURS);
            return;
        }
        long multiplier = 1L << Math.min(attemptCount - 1, 6);
        long delayMinutes = Math.min(multiplier, MAX_RETRY_DELAY_MINUTES);
        nextAttemptAt = failedAt.plusMinutes(delayMinutes);
    }
}
