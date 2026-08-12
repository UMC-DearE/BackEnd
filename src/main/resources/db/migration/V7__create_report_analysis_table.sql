-- =============================================
-- V7: report_analysis 테이블 생성
-- AI가 편지 3통 이상을 바탕으로 생성하는 유저 성향 분석(1인 1행, 재분석 시 덮어쓰기)
-- =============================================

CREATE TABLE IF NOT EXISTS report_analysis (
    report_analysis_id   BIGINT       NOT NULL AUTO_INCREMENT,
    user_id               BIGINT       NOT NULL,
    description            VARCHAR(200) NOT NULL,
    hashtag1                VARCHAR(50)  NOT NULL,
    hashtag2                VARCHAR(50)  NOT NULL,
    analyzed_letter_count  INT          NOT NULL,
    analyzed_at            DATETIME(6)  NOT NULL,
    created_at             DATETIME(6)  NOT NULL,
    updated_at             DATETIME(6)  NOT NULL,
    deleted_at             DATETIME(6),
    is_deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (report_analysis_id),
    UNIQUE KEY uq_report_analysis_user (user_id),
    CONSTRAINT fk_report_analysis_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
