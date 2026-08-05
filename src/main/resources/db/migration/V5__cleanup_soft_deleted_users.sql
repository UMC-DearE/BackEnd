-- =============================================
-- V5: 소프트딜리트된 유저 데이터 일괄 정리
-- 하드딜리트 전환 시점에 기존 is_deleted=true 유저 및 연관 데이터 삭제
-- DB FK에 ON DELETE CASCADE 없으므로 연관관계 역순으로 수동 처리
-- =============================================

-- 1. letter_image 삭제 (letter FK 참조)
DELETE li FROM letter_image li
    INNER JOIN letter l ON li.letter_id = l.letter_id
WHERE l.user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 2. letter_emotion 삭제 (letter FK 참조)
DELETE le FROM letter_emotion le
    INNER JOIN letter l ON le.letter_id = l.letter_id
WHERE l.user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 3. letter 삭제
DELETE FROM letter WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 4. user_from 삭제
DELETE FROM user_from WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 5. user_folder 삭제
DELETE FROM user_folder WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 6. user_setting 삭제
DELETE FROM user_setting WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 7. user_term 삭제
DELETE FROM user_term WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 8. user_sticker 삭제
DELETE FROM user_sticker WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 9. report 삭제
DELETE FROM report WHERE user_id IN (SELECT user_id FROM users WHERE is_deleted = true);

-- 10. 유저 삭제
DELETE FROM users WHERE is_deleted = true;
