-- =============================================
-- V2: 초기 데이터 삽입 (약관 + 감정)
-- =============================================

-- 1. 서비스 이용약관 (필수)
INSERT INTO term (
    title,
    type,
    content,
    is_required,
    is_active,
    effective_at,
    version,
    is_deleted,
    created_at,
    updated_at
)
VALUES (
    '서비스 이용약관',
    'SERVICE',
    '서비스 이용약관 내용입니다.',
    1,
    1,
    '2025-01-01 00:00:00',
    '1.0',
    0,
    NOW(),
    NOW()
);

-- 2. 개인정보 처리방침 (필수)
INSERT INTO term (
    title,
    type,
    content,
    is_required,
    is_active,
    effective_at,
    version,
    is_deleted,
    created_at,
    updated_at
)
VALUES (
    '개인정보 처리방침',
    'PRIVACY',
    '개인정보 처리방침 내용입니다.',
    1,
    1,
    '2025-01-01 00:00:00',
    '1.0',
    0,
    NOW(),
    NOW()
);

-- 3. 마케팅 수신 동의 (선택)
INSERT INTO term (
    title,
    type,
    content,
    is_required,
    is_active,
    effective_at,
    version,
    is_deleted,
    created_at,
    updated_at
)
VALUES (
    '마케팅 수신 동의',
    'MARKETING',
    '마케팅 정보 수신에 동의합니다.',
    0,
    1,
    '2025-01-01 00:00:00',
    '1.0',
    0,
    NOW(),
    NOW()
);

-- 4. 감정 카테고리 초기 데이터
INSERT INTO emotion_category (bg_color, font_color, type) VALUES
('FFE0D8', 'F57542', '따뜻함 & 애정'),
('FFF0BA', 'FFB245', '즐거움 & 에너지'),
('E6FECB', '62BA65', '위로 & 지지'),
('F5E0F9', 'D572B7', '그리움 & 차분함'),
('D7E2F9', '6B80B5', '고민 & 복잡함');

-- 5. 감정 초기 데이터
INSERT INTO emotions (name, category_id) VALUES
('사랑', 1),
('애정', 1),
('다정함', 1),
('소중함', 1),
('헌신', 1),
('신뢰', 1),
('친밀감', 1),
('포근함', 1),
('정서적유대', 1),
('기쁨', 2),
('즐거움', 2),
('유쾌함', 2),
('신남', 2),
('활력', 2),
('기대감', 2),
('희열', 2),
('만족', 2),
('성취감', 2),
('위로', 3),
('공감', 3),
('격려', 3),
('지지', 3),
('이해', 3),
('위안', 3),
('안심', 3),
('든든함', 3),
('회복감', 3),
('그리움', 4),
('아련함', 4),
('회상', 4),
('차분함', 4),
('고요', 4),
('담담함', 4),
('평온', 4),
('여운', 4),
('고민', 5),
('혼란', 5),
('불안', 5),
('걱정', 5),
('망설임', 5),
('답답함', 5),
('후회', 5),
('두려움', 5),
('외로움', 5);
