-- admin 사용자 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
),
-- Java 태그 ID 가져오기
     java_tag AS (
         SELECT id FROM public.tags WHERE name = 'Java'
     )
-- Java 면접 퀴즈 생성
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
-- 초급 Java 면접 퀴즈
SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '자바 기초 면접 질문 모음 Vol.' || seq,
    '자바 프로그래밍 초보자를 위한 기본 개념과 면접 준비 질문 모음입니다.',
    'BEGINNER',
    true,
    10, -- 각 퀴즈는 10개 문제
    'TOPIC_BASED',
    30, -- 30분 제한시간
    (SELECT id FROM admin_user),
    floor(random() * 80 + 30), -- 30-110 시도 횟수
    random() * 25 + 70, -- 70-95 평균 점수
    floor(random() * 500 + 200), -- 200-700 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 3) AS seq

UNION ALL

-- 중급 Java 면접 퀴즈
SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '중급 자바 개발자 면접 질문 Vol.' || seq,
    '자바 개발자 면접에서 자주 물어보는 중급 수준의 개념과 문제들을 다룹니다.',
    'INTERMEDIATE',
    true,
    10, -- 각 퀴즈는 10개 문제
    'TOPIC_BASED',
    40, -- 40분 제한시간
    (SELECT id FROM admin_user),
    floor(random() * 60 + 20), -- 20-80 시도 횟수
    random() * 20 + 65, -- 65-85 평균 점수
    floor(random() * 400 + 150), -- 150-550 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 2) AS seq

UNION ALL

-- 고급 Java 면접 퀴즈
SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '자바 심화 면접 질문 Vol.' || seq,
    '자바 고급 개념 및 기술 면접에 자주 나오는 심화 문제들입니다.',
    'ADVANCED',
    true,
    10, -- 각 퀴즈는 10개 문제
    'TOPIC_BASED',
    45, -- 45분 제한시간
    (SELECT id FROM admin_user),
    floor(random() * 40 + 10), -- 10-50 시도 횟수
    random() * 15 + 60, -- 60-75 평균 점수
    floor(random() * 300 + 100), -- 100-400 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 2) AS seq;

-- Java 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, (SELECT id FROM java_tag)
FROM public.quizzes q
WHERE q.title LIKE '자바%' OR q.title LIKE '중급 자바%' OR q.title LIKE '자바 심화%';