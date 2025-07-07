- 매우 간단한 테스트용 SQL

-- 1. 관리자 사용자 생성
INSERT INTO public.users (
    email, username, experience, is_active, level, provider, provider_id,
    required_experience, role, total_points, created_at, updated_at
) VALUES (
    'admin@test.com', 'admin', 1000, true, 10, 'GITHUB', 'admin_123',
    1000, 'ADMIN', 5000, NOW(), NOW()
);

-- 2. 기본 태그 생성
INSERT INTO public.tags (created_at, name, description) VALUES
    (NOW(), 'Java', 'Java 프로그래밍'),
    (NOW(), 'Spring', 'Spring Framework');

-- 3. 간단한 퀴즈 생성
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit, creator_id,
    attempt_count, avg_score, view_count
) VALUES (
    NOW(), NOW(), '테스트 퀴즈', '테스트용 퀴즈', 'BEGINNER',
    true, 1, 'REGULAR', 30,
    (SELECT id FROM public.users WHERE email = 'admin@test.com'),
    0, 0.0, 0
);

-- 4. 간단한 질문 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
) VALUES (
    NOW(), NOW(), '테스트 질문입니다', 'MULTIPLE_CHOICE', 'BEGINNER', 
    '정답', '["선택1", "정답", "선택3", "선택4"]'::jsonb, 
    '테스트 설명', 10, 30,
    (SELECT id FROM public.quizzes WHERE title = '테스트 퀴즈')
); 