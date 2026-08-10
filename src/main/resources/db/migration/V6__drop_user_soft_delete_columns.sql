-- =============================================
-- V6: users 테이블 소프트딜리트 관련 컬럼 제거
-- 하드딜리트 전환 완료로 is_deleted, deleted_at, status 불필요
-- =============================================

ALTER TABLE users DROP COLUMN is_deleted;
ALTER TABLE users DROP COLUMN deleted_at;
ALTER TABLE users DROP COLUMN status;
