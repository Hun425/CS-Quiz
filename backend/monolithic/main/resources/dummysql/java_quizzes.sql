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
SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '자바 기초 면접 질문 모음 Vol.' || seq,
    '자바 프로그래밍 초보자를 위한 기본 개념과 면접 준비 질문 모음입니다.',
    'BEGINNER',
    true,
    10,
    'TOPIC_BASED',
    30,
    (SELECT id FROM admin_user),
    floor(random() * 80 + 30),
    random() * 25 + 70,
    floor(random() * 500 + 200),
    NULL::timestamp
FROM generate_series(1, 3) AS seq

UNION ALL

SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '중급 자바 개발자 면접 질문 Vol.' || seq,
    '자바 개발자 면접에서 자주 물어보는 중급 수준의 개념과 문제들을 다룹니다.',
    'INTERMEDIATE',
    true,
    10,
    'TOPIC_BASED',
    40,
    (SELECT id FROM admin_user),
    floor(random() * 60 + 20),
    random() * 20 + 65,
    floor(random() * 400 + 150),
    NULL::timestamp
FROM generate_series(1, 2) AS seq

UNION ALL

SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '30 days')),
    '자바 심화 면접 질문 Vol.' || seq,
    '자바 고급 개념 및 기술 면접에 자주 나오는 심화 문제들입니다.',
    'ADVANCED',
    true,
    10,
    'TOPIC_BASED',
    45,
    (SELECT id FROM admin_user),
    floor(random() * 40 + 10),
    random() * 15 + 60,
    floor(random() * 300 + 100),
    NULL::timestamp
FROM generate_series(1, 2) AS seq;

-- Java 퀴즈와 태그 연결 (별도 쿼리로 실행해야 함)
WITH java_tag AS (
    SELECT id FROM public.tags WHERE name = 'Java'
)
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, (SELECT id FROM java_tag)
FROM public.quizzes q
WHERE q.title LIKE '자바%' OR q.title LIKE '중급 자바%' OR q.title LIKE '자바 심화%';