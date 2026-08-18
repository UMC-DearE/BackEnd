CREATE TABLE user_invite_code (
    user_invite_code_id BIGINT      NOT NULL AUTO_INCREMENT,
    user_id             BIGINT      NOT NULL,
    invite_code         VARCHAR(64) NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    PRIMARY KEY (user_invite_code_id),
    UNIQUE KEY uk_user_invite_code_user (user_id),
    UNIQUE KEY uk_user_invite_code_code (invite_code),
    CONSTRAINT fk_user_invite_code_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_invite_history (
    user_invite_history_id BIGINT      NOT NULL AUTO_INCREMENT,
    inviter_user_id        BIGINT      NOT NULL,
    invitee_user_id        BIGINT      NOT NULL,
    created_at             DATETIME(6) NOT NULL,
    updated_at             DATETIME(6) NOT NULL,
    PRIMARY KEY (user_invite_history_id),
    UNIQUE KEY uk_user_invite_history_invitee (invitee_user_id),
    CONSTRAINT fk_user_invite_history_inviter
        FOREIGN KEY (inviter_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_invite_history_invitee
        FOREIGN KEY (invitee_user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
