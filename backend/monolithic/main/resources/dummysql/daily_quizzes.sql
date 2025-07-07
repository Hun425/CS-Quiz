-- admin 사용자 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
)
-- 데일리 퀴즈 생성
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (seq * INTERVAL '1 day'),
    NOW() - (seq * INTERVAL '1 day'),
    '오늘의 CS 퀴즈 #' || (30-seq),
    to_char(NOW() - (seq * INTERVAL '1 day'), 'YYYY년 MM월 DD일') || '의 일일 컴퓨터 과학 퀴즈',
    CASE (seq % 3)
        WHEN 0 THEN 'BEGINNER'
        WHEN 1 THEN 'INTERMEDIATE'
        ELSE 'ADVANCED'
        END,
    true,
    5, -- 각 데일리 퀴즈는 5개 문제
    'DAILY',
    15, -- 15분 제한시간
    (SELECT id FROM admin_user),
    floor(random() * 100 + 50), -- 50-150 시도 횟수
    random() * 40 + 60, -- 60-100 평균 점수
    floor(random() * 500 + 200), -- 200-700 조회수
    CASE
        WHEN seq = 0 THEN (NOW() + INTERVAL '1 day')::timestamp -- 오늘의 퀴즈는 내일까지 유효
        ELSE (NOW() - ((seq-1) * INTERVAL '1 day'))::timestamp -- 과거 퀴즈는 이미 만료됨
        END
FROM generate_series(0, 29) AS seq;