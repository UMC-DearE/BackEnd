-- =============================================
-- V1: 전체 테이블 생성
-- =============================================

-- 1. image (독립 테이블, FK 없음)
CREATE TABLE IF NOT EXISTS image (
    image_id    BIGINT       NOT NULL AUTO_INCREMENT,
    image_key   VARCHAR(512) NOT NULL,
    image_url   VARCHAR(1000) NOT NULL,
    original_file_name VARCHAR(512) NOT NULL,
    file_type   VARCHAR(255) NOT NULL,
    file_size   BIGINT       NOT NULL,
    upload_status VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    deleted_at  DATETIME(6),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. users
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT       NOT NULL AUTO_INCREMENT,
    nickname    VARCHAR(20)  NOT NULL,
    role        VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL,
    provider    VARCHAR(255) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    intro       VARCHAR(50),
    image_id    BIGINT,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    deleted_at  DATETIME(6),
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_users_image FOREIGN KEY (image_id) REFERENCES image (image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. user_setting
CREATE TABLE IF NOT EXISTS user_setting (
    user_setting_id BIGINT      NOT NULL AUTO_INCREMENT,
    theme           VARCHAR(255) NOT NULL,
    font            VARCHAR(255) NOT NULL,
    home_color      VARCHAR(16)  NOT NULL,
    membership_plan VARCHAR(255) NOT NULL,
    user_id         BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME(6),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_setting_id),
    UNIQUE KEY uk_user_setting_user (user_id),
    CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. user_from
CREATE TABLE IF NOT EXISTS user_from (
    user_from_id     BIGINT      NOT NULL AUTO_INCREMENT,
    from_name        VARCHAR(7)  NOT NULL,
    from_bg_color    VARCHAR(16) NOT NULL,
    from_font_color  VARCHAR(16) NOT NULL,
    user_id          BIGINT      NOT NULL,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    deleted_at       DATETIME(6),
    is_deleted       BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_from_id),
    CONSTRAINT fk_user_from_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. user_folder
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
    CONSTRAINT fk_user_folder_user  FOREIGN KEY (user_id)  REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. letter
CREATE TABLE IF NOT EXISTS letter (
    letter_id       BIGINT       NOT NULL AUTO_INCREMENT,
    content         LONGTEXT     NOT NULL,
    received_at     DATE,
    ai_summary      VARCHAR(255) NOT NULL,
    reply           VARCHAR(100),
    is_liked        BOOLEAN      NOT NULL DEFAULT FALSE,
    is_pinned       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_hidden       BOOLEAN      NOT NULL DEFAULT FALSE,
    content_version INT          NOT NULL,
    content_hash    VARCHAR(64)  NOT NULL,
    user_id         BIGINT       NOT NULL,
    user_from_id    BIGINT       NOT NULL,
    user_folder_id  BIGINT,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    deleted_at      DATETIME(6),
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (letter_id),
    CONSTRAINT fk_letter_user   FOREIGN KEY (user_id)      REFERENCES users (user_id),
    CONSTRAINT fk_letter_from   FOREIGN KEY (user_from_id)  REFERENCES user_from (user_from_id),
    CONSTRAINT fk_letter_folder FOREIGN KEY (user_folder_id) REFERENCES user_folder (user_folder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. letter_image
CREATE TABLE IF NOT EXISTS letter_image (
    letter_image_id BIGINT NOT NULL AUTO_INCREMENT,
    image_order     INT    NOT NULL,
    letter_id       BIGINT NOT NULL,
    image_id        BIGINT NOT NULL,
    PRIMARY KEY (letter_image_id),
    CONSTRAINT fk_letter_image_letter FOREIGN KEY (letter_id) REFERENCES letter (letter_id),
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

-- 9. user_term
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
    CONSTRAINT fk_user_term_user FOREIGN KEY (user_id)  REFERENCES users (user_id)
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

-- 12. letter_emotion
CREATE TABLE IF NOT EXISTS letter_emotion (
    letter_emotion_id BIGINT NOT NULL AUTO_INCREMENT,
    letter_id         BIGINT NOT NULL,
    emotion_id        BIGINT NOT NULL,
    PRIMARY KEY (letter_emotion_id),
    UNIQUE KEY uq_letter_emotion (letter_id, emotion_id),
    CONSTRAINT fk_letter_emotion_letter  FOREIGN KEY (letter_id)  REFERENCES letter (letter_id),
    CONSTRAINT fk_letter_emotion_emotion FOREIGN KEY (emotion_id) REFERENCES emotions (emotion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. report
CREATE TABLE IF NOT EXISTS report (
    user_report_id    BIGINT      NOT NULL AUTO_INCREMENT,
    scope             VARCHAR(255) NOT NULL,
    report_year_month DATE        NOT NULL,
    recap_json        JSON,
    top3_from_json    JSON,
    top_phrase_json   JSON,
    emotion_dist_json JSON,
    is_stale          BOOLEAN     NOT NULL DEFAULT FALSE,
    user_id           BIGINT      NOT NULL,
    created_at        DATETIME(6) NOT NULL,
    updated_at        DATETIME(6) NOT NULL,
    deleted_at        DATETIME(6),
    is_deleted        BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_report_id),
    UNIQUE KEY uq_user_scope_month (user_id, scope, report_year_month),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. user_sticker
CREATE TABLE IF NOT EXISTS user_sticker (
    user_sticker_id BIGINT        NOT NULL AUTO_INCREMENT,
    pos_x           DECIMAL(5,2)  NOT NULL,
    pos_y           DECIMAL(5,2)  NOT NULL,
    pos_z           INT           NOT NULL,
    rotation        DECIMAL(6,2)  NOT NULL,
    scale           DECIMAL(3,2)  NOT NULL,
    image_id        BIGINT        NOT NULL,
    user_id         BIGINT        NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    deleted_at      DATETIME(6),
    is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_sticker_id),
    CONSTRAINT fk_user_sticker_image FOREIGN KEY (image_id) REFERENCES image (image_id),
    CONSTRAINT fk_user_sticker_user  FOREIGN KEY (user_id)  REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. test (개발용)
CREATE TABLE IF NOT EXISTS test (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
