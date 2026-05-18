-- V4: 활성 약관 제목 수정 + 마케팅 약관 비활성화
UPDATE term SET title = '서비스 이용약관',   updated_at = NOW() WHERE type = 'SERVICE'   AND is_active = 1;
UPDATE term SET title = '개인정보처리방침',  updated_at = NOW() WHERE type = 'PRIVACY'   AND is_active = 1;
UPDATE term SET is_active = 0,           updated_at = NOW() WHERE type = 'MARKETING' AND is_active = 1;
