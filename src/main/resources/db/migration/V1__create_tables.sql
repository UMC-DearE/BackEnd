-- =============================================
-- V1: 전체 테이블 생성 (최종 통합 스키마)
-- V1~V18 마이그레이션 통합본. IF NOT EXISTS로 기존 DB 무해.
-- =============================================

-- 1. image
CREATE TABLE IF NOT EXISTS image (
    image_id           BIGINT        NOT NULL AUTO_INCREMENT,
    image_key          VARCHAR(512)  NOT NULL,
    image_url          VARCHAR(1000) NOT NULL,
    original_file_name VARCHAR(512)  NOT NULL,
    file_type          VARCHAR(255)  NOT NULL,
    file_size          BIGINT        NOT NULL,
    upload_status      VARCHAR(255)  NOT NULL,
    content_type       VARCHAR(255)  NOT NULL,
    created_at         DATETIME(6)   NOT NULL,
    updated_at         DATETIME(6)   NOT NULL,
    deleted_at         DATETIME(6),
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. users (status/is_deleted/deleted_at 제거 - V6 반영)
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT       NOT NULL AUTO_INCREMENT,
    nickname    VARCHAR(20)  NOT NULL,
    role        VARCHAR(255) NOT NULL,
    provider    VARCHAR(255) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    intro       VARCHAR(50),
    image_id    BIGINT,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_image FOREIGN KEY (image_id) REFERENCES image (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. user_setting (decoration_unlock_guide_status - V11, CASCADE - V18)
CREATE TABLE IF NOT EXISTS user_setting (
    user_setting_id                BIGINT       NOT NULL AUTO_INCREMENT,
    theme                          VARCHAR(255) NOT NULL,
    font                           VARCHAR(255) NOT NULL,
    home_color                     VARCHAR(16)  NOT NULL,
    membership_plan                VARCHAR(255) NOT NULL,
    decoration_unlock_guide_status VARCHAR(20)  NOT NULL DEFAULT 'NOT_ELIGIBLE',
    user_id                        BIGINT       NOT NULL,
    created_at                     DATETIME(6)  NOT NULL,
    updated_at                     DATETIME(6)  NOT NULL,
    deleted_at                     DATETIME(6),
    is_deleted                     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_setting_id),
    UNIQUE KEY uk_user_setting_user (user_id),
    CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. user_from (from_name VARCHAR(10) - V14, CASCADE - V18)
CREATE TABLE IF NOT EXISTS user_from (
    user_from_id    BIGINT      NOT NULL AUTO_INCREMENT,
    from_name       VARCHAR(10) NOT NULL,
    from_bg_color   VARCHAR(16) NOT NULL,
    from_font_color VARCHAR(16) NOT NULL,
    user_id         BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL,
    deleted_at      DATETIME(6),
    is_deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_from_id),
    CONSTRAINT fk_user_from_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. user_folder (CASCADE - V18)
CREATE TABLE IF NOT EXISTS user_folder (
    user_folder_id BIGINT      NOT NULL AUTO_INCREMENT,
    folder_name    VARCHAR(6)  NOT NULL,
    folder_order   INT         NOT NULL,
    image_id       BIGINT,
    user_id        BIGINT      NOT NULL,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    deleted_at     DATETIME(6),
    is_deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_folder_id),
    CONSTRAINT fk_user_folder_image FOREIGN KEY (image_id) REFERENCES image (image_id),
    CONSTRAINT fk_user_folder_user  FOREIGN KEY (user_id)  REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. letter (암호화 컬럼 - V13/V15/V16/V17/V19, CASCADE - V18)
CREATE TABLE IF NOT EXISTS letter (
    letter_id                      BIGINT      NOT NULL AUTO_INCREMENT,
    content_ciphertext             LONGTEXT    NULL,
    content_encryption_nonce       VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NULL,
    content_encryption_key_version INT         NULL,
    content_encryption_format_version INT      NULL,
    received_at                    DATE,
    ai_summary                     VARCHAR(255) NOT NULL,
    reply                          VARCHAR(100),
    is_liked                       BOOLEAN     NOT NULL DEFAULT FALSE,
    is_pinned                      BOOLEAN     NOT NULL DEFAULT FALSE,
    is_hidden                      BOOLEAN     NOT NULL DEFAULT FALSE,
    content_version                INT         NOT NULL,
    user_id                        BIGINT      NOT NULL,
    user_from_id                   BIGINT      NOT NULL,
    user_folder_id                 BIGINT,
    created_at                     DATETIME(6) NOT NULL,
    updated_at                     DATETIME(6) NOT NULL,
    deleted_at                     DATETIME(6),
    is_deleted                     BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (letter_id),
    CONSTRAINT fk_letter_user   FOREIGN KEY (user_id)        REFERENCES users (user_id)                ON DELETE CASCADE,
    CONSTRAINT fk_letter_from   FOREIGN KEY (user_from_id)   REFERENCES user_from (user_from_id),
    CONSTRAINT fk_letter_folder FOREIGN KEY (user_folder_id) REFERENCES user_folder (user_folder_id),
    CONSTRAINT chk_letter_content_encryption_state CHECK (
        CHAR_LENGTH(content_ciphertext) > 0
        AND CHAR_LENGTH(content_encryption_nonce) = 16
        AND content_encryption_key_version > 0
        AND content_encryption_format_version = 1
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. letter_image (CASCADE - V18)
CREATE TABLE IF NOT EXISTS letter_image (
    letter_image_id BIGINT NOT NULL AUTO_INCREMENT,
    image_order     INT    NOT NULL,
    letter_id       BIGINT NOT NULL,
    image_id        BIGINT NOT NULL,
    PRIMARY KEY (letter_image_id),
    CONSTRAINT fk_letter_image_letter FOREIGN KEY (letter_id) REFERENCES letter (letter_id) ON DELETE CASCADE,
    CONSTRAINT fk_letter_image_image  FOREIGN KEY (image_id)  REFERENCES image (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. term
CREATE TABLE IF NOT EXISTS term (
    terms_id     BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100) NOT NULL,
    type         VARCHAR(255) NOT NULL,
    content      LONGTEXT     NOT NULL,
    is_required  BOOLEAN      NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    effective_at DATETIME(6)  NOT NULL,
    version      VARCHAR(100) NOT NULL DEFAULT '1.0',
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    deleted_at   DATETIME(6),
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (terms_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. user_term (CASCADE - V18)
CREATE TABLE IF NOT EXISTS user_term (
    user_term_id BIGINT      NOT NULL AUTO_INCREMENT,
    is_agreed    BOOLEAN     NOT NULL DEFAULT FALSE,
    agreed_at    DATETIME(6),
    terms_id     BIGINT      NOT NULL,
    user_id      BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,
    deleted_at   DATETIME(6),
    is_deleted   BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_term_id),
    CONSTRAINT fk_user_term_term FOREIGN KEY (terms_id) REFERENCES term (terms_id),
    CONSTRAINT fk_user_term_user FOREIGN KEY (user_id)  REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. emotion_category
CREATE TABLE IF NOT EXISTS emotion_category (
    category_id BIGINT      NOT NULL AUTO_INCREMENT,
    type        VARCHAR(20) NOT NULL,
    bg_color    VARCHAR(16) NOT NULL,
    font_color  VARCHAR(16) NOT NULL,
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. emotions
CREATE TABLE IF NOT EXISTS emotions (
    emotion_id  BIGINT      NOT NULL AUTO_INCREMENT,
    name        VARCHAR(20) NOT NULL,
    category_id BIGINT      NOT NULL,
    PRIMARY KEY (emotion_id),
    CONSTRAINT fk_emotion_category FOREIGN KEY (category_id) REFERENCES emotion_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. letter_emotion (CASCADE - V18)
CREATE TABLE IF NOT EXISTS letter_emotion (
    letter_emotion_id BIGINT NOT NULL AUTO_INCREMENT,
    letter_id         BIGINT NOT NULL,
    emotion_id        BIGINT NOT NULL,
    PRIMARY KEY (letter_emotion_id),
    UNIQUE KEY uq_letter_emotion (letter_id, emotion_id),
    CONSTRAINT fk_letter_emotion_letter  FOREIGN KEY (letter_id)  REFERENCES letter (letter_id)  ON DELETE CASCADE,
    CONSTRAINT fk_letter_emotion_emotion FOREIGN KEY (emotion_id) REFERENCES emotions (emotion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. user_sticker (CASCADE - V18)
CREATE TABLE IF NOT EXISTS user_sticker (
    user_sticker_id BIGINT       NOT NULL AUTO_INCREMENT,
    pos_x           DECIMAL(5,2) NOT NULL,
    pos_y           DECIMAL(5,2) NOT NULL,
    pos_z           INT          NOT NULL,
    rotation        DECIMAL(6,2) NOT NULL,
    scale           DECIMAL(3,2) NOT NULL,
    image_id        BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME(6),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_sticker_id),
    CONSTRAINT fk_user_sticker_image FOREIGN KEY (image_id) REFERENCES image (image_id),
    CONSTRAINT fk_user_sticker_user  FOREIGN KEY (user_id)  REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. test (개발용)
CREATE TABLE IF NOT EXISTS test (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. report_analysis (V7, CASCADE - V18)
CREATE TABLE IF NOT EXISTS report_analysis (
    report_analysis_id    BIGINT       NOT NULL AUTO_INCREMENT,
    user_id               BIGINT       NOT NULL,
    description           VARCHAR(200) NOT NULL,
    hashtag1              VARCHAR(50)  NOT NULL,
    hashtag2              VARCHAR(50)  NOT NULL,
    analyzed_letter_count INT          NOT NULL,
    analyzed_at           DATETIME(6)  NOT NULL,
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    deleted_at            DATETIME(6),
    is_deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (report_analysis_id),
    UNIQUE KEY uq_report_analysis_user (user_id),
    CONSTRAINT chk_report_analysis_analyzed_letter_count CHECK (analyzed_letter_count >= 0),
    CONSTRAINT fk_report_analysis_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. user_invite_code (V8, ON DELETE CASCADE 원래 포함)
CREATE TABLE IF NOT EXISTS user_invite_code (
    user_invite_code_id BIGINT      NOT NULL AUTO_INCREMENT,
    user_id             BIGINT      NOT NULL,
    invite_code         VARCHAR(64) NOT NULL,
    created_at          DATETIME(6) NOT NULL,
    updated_at          DATETIME(6) NOT NULL,
    PRIMARY KEY (user_invite_code_id),
    UNIQUE KEY uk_user_invite_code_user (user_id),
    UNIQUE KEY uk_user_invite_code_code (invite_code),
    CONSTRAINT fk_user_invite_code_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. user_invite_history (V8, ON DELETE CASCADE 원래 포함)
CREATE TABLE IF NOT EXISTS user_invite_history (
    user_invite_history_id BIGINT      NOT NULL AUTO_INCREMENT,
    inviter_user_id        BIGINT      NOT NULL,
    invitee_user_id        BIGINT      NOT NULL,
    created_at             DATETIME(6) NOT NULL,
    updated_at             DATETIME(6) NOT NULL,
    PRIMARY KEY (user_invite_history_id),
    UNIQUE KEY uk_user_invite_history_invitee (invitee_user_id),
    CONSTRAINT fk_user_invite_history_inviter FOREIGN KEY (inviter_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_invite_history_invitee FOREIGN KEY (invitee_user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 18. signup_benefit_outbox (V10, ON DELETE CASCADE 원래 포함)
CREATE TABLE IF NOT EXISTS signup_benefit_outbox (
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
    CONSTRAINT fk_signup_benefit_outbox_invitee FOREIGN KEY (invitee_user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. letter_search_token (V12, ON DELETE CASCADE 원래 포함)
CREATE TABLE IF NOT EXISTS letter_search_token (
    letter_search_token_id BIGINT      NOT NULL AUTO_INCREMENT,
    letter_id              BIGINT      NOT NULL,
    index_key_version      INT         NOT NULL,
    token                  VARCHAR(43) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (letter_search_token_id),
    UNIQUE KEY uk_letter_search_token (letter_id, index_key_version, token),
    KEY idx_letter_search_token_candidate (index_key_version, token, letter_id),
    CONSTRAINT fk_letter_search_token_letter FOREIGN KEY (letter_id) REFERENCES letter (letter_id) ON DELETE CASCADE,
    CONSTRAINT chk_letter_search_token_key_version CHECK (index_key_version > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
