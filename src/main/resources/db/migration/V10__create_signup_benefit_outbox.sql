CREATE TABLE signup_benefit_outbox (
    signup_benefit_outbox_id BIGINT       NOT NULL AUTO_INCREMENT,
    invitee_user_id          BIGINT       NOT NULL,
    invite_code              VARCHAR(64)  NOT NULL,
    status                   VARCHAR(20)  NOT NULL,
    attempt_count            INT          NOT NULL DEFAULT 0,
    next_attempt_at          DATETIME(6)  NULL,
    last_error               VARCHAR(255) NULL,
    created_at               DATETIME(6)  NOT NULL,
    updated_at               DATETIME(6)  NOT NULL,
    PRIMARY KEY (signup_benefit_outbox_id),
    UNIQUE KEY uk_signup_benefit_outbox_invitee (invitee_user_id),
    KEY idx_signup_benefit_outbox_retry (status, next_attempt_at),
    CONSTRAINT fk_signup_benefit_outbox_invitee
        FOREIGN KEY (invitee_user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
