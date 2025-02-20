-- 기본 사용자 데이터를 위한 레벨 정보 준비
WITH user_level_info AS (
    SELECT 
        unnest(ARRAY['admin', 'user1', 'user2']) as username,
        unnest(ARRAY[5, 2, 1]) as level,
        unnest(ARRAY[500, 200, 150]) as current_exp,
        unnest(ARRAY[1000, 400, 200]) as required_exp,
        unnest(ARRAY[1000, 500, 300]) as total_points
),
-- 기본 사용자 데이터 생성
initial_users AS (
    INSERT INTO public.users (
        id, created_at, email, is_active, provider, provider_id, role, 
        total_points, updated_at, username, experience, level, 
        required_experience, last_login
    )
    SELECT 
        gen_random_uuid(),
        NOW(),
        username || '@example.com',
        true,
        CASE username
            WHEN 'admin' THEN 'GOOGLE'
            WHEN 'user1' THEN 'GITHUB'
            ELSE 'KAKAO'
        END,
        username || '123',
        CASE username
            WHEN 'admin' THEN 'ADMIN'
            ELSE 'USER'
        END,
        total_points,
        NOW(),
        username,
        current_exp,
        level,
        required_exp,
        NOW()
    FROM user_level_info
    RETURNING id, username, role, level, experience as current_exp, required_experience as required_exp
),
-- 태그 생성
inserted_tags AS (
    INSERT INTO public.tags (id, created_at, name, description)
    VALUES 
    (gen_random_uuid(), NOW(), 'JavaScriptt', 'JavaScript programming language'),
    (gen_random_uuid(), NOW(), 'Pythonn', 'Python programming language'),
    (gen_random_uuid(), NOW(), 'Databasee', 'Database concepts and SQL')
    RETURNING id, name
),
-- 태그 동의어 추가
tag_synonyms_insert AS (
    INSERT INTO public.tag_synonyms (tag_id, synonym)
    SELECT id, 'JS' FROM inserted_tags WHERE name = 'JavaScript'
    UNION ALL
    SELECT id, 'py' FROM inserted_tags WHERE name = 'Python'
),
-- 관리자 ID 획득
admin_user AS (
    SELECT id FROM initial_users WHERE role = 'ADMIN' LIMIT 1
),
-- 퀴즈 생성을 위한 시퀀스 준비
quiz_sequence AS (
    SELECT 
        gen_random_uuid() as quiz_id,
        seq,
        CASE (seq % 3)
            WHEN 0 THEN 'BEGINNER'
            WHEN 1 THEN 'INTERMEDIATE'
            ELSE 'ADVANCED'
        END as difficulty,
        CASE (seq % 4)
            WHEN 0 THEN 'DAILY'
            WHEN 1 THEN 'TAG_BASED'
            WHEN 2 THEN 'TOPIC_BASED'
            ELSE 'CUSTOM'
        END as quiz_type
    FROM generate_series(1, 5) seq
),
-- 퀴즈 생성
inserted_quizzes AS (
    INSERT INTO public.quizzes (
        id, created_at, updated_at, title, description, difficulty_level,
        is_public, question_count, quiz_type, time_limit,
        creator_id, attempt_count, avg_score, view_count
    )
    SELECT 
        qs.quiz_id,
        NOW(),
        NOW(),
        'JavaScript Basics ' || qs.seq,
        'Basic JavaScript concepts and fundamentals quiz #' || qs.seq,
        qs.difficulty,
        true,
        5,
        qs.quiz_type,
        30,
        (SELECT id FROM admin_user),
        floor(random() * 100),
        random() * 100,
        floor(random() * 1000)
    FROM quiz_sequence qs
    RETURNING id, question_count
),
-- 문제 생성을 위한 시퀀스 준비
question_sequence AS (
    SELECT 
        q.id as quiz_id,
        s.seq,
        gen_random_uuid() as question_id
    FROM inserted_quizzes q
    CROSS JOIN generate_series(1, 10) s(seq)
),
-- 문제 생성
inserted_questions AS (
    INSERT INTO public.questions (
        id, question_text, question_type, difficulty_level,
        correct_answer, options, points, created_at, updated_at,
        quiz_id
    )
    SELECT
        qs.question_id,
        'Sample Question ' || qs.seq,
        CASE (qs.seq % 5)
            WHEN 0 THEN 'MULTIPLE_CHOICE'
            WHEN 1 THEN 'TRUE_FALSE'
            WHEN 2 THEN 'SHORT_ANSWER'
            WHEN 3 THEN 'CODE_ANALYSIS'
            ELSE 'DIAGRAM_BASED'
        END,
        CASE (qs.seq % 3)
            WHEN 0 THEN 'BEGINNER'
            WHEN 1 THEN 'INTERMEDIATE'
            ELSE 'ADVANCED'
        END,
        'Correct Answer ' || qs.seq,
        '{"options": ["Option A", "Option B", "Option C", "Option D"]}'::jsonb,
        10,
        NOW(),
        NOW(),
        qs.quiz_id
    FROM question_sequence qs
    RETURNING id, quiz_id
),
-- 일반 사용자 ID 획득
normal_users AS (
    SELECT id, current_exp, level, required_exp 
    FROM initial_users 
    WHERE role = 'USER'
),
-- 퀴즈 시도 생성
inserted_quiz_attempts AS (
    INSERT INTO public.quiz_attempts (
        id, created_at, start_time, end_time,
        is_completed, score, time_taken,
        quiz_id, user_id, last_answered_question_index, remaining_time
    )
    SELECT
        gen_random_uuid(),
        NOW(),
        NOW() - interval '1 hour',
        NOW(),
        true,
        floor(random() * 100),
        floor(random() * 1800),
        q.id,
        u.id,
        4,
        0
    FROM inserted_quizzes q
    CROSS JOIN normal_users u
    LIMIT 10
    RETURNING id, quiz_id, user_id
),
-- 퀴즈 리뷰 생성
inserted_reviews AS (
    INSERT INTO public.quiz_reviews (
        id, content, created_at, rating,
        quiz_id, reviewer_id
    )
    SELECT
        gen_random_uuid(),
        'Great quiz! Very helpful for learning ' || seq,
        NOW(),
        floor(random() * 5) + 1,
        q.id,
        u.id
    FROM inserted_quizzes q
    CROSS JOIN normal_users u
    CROSS JOIN generate_series(1, 2) seq
    RETURNING id
),
-- 사용자 전투 통계 생성
inserted_battle_stats AS (
    INSERT INTO public.user_battle_stats (
        id, created_at, updated_at, current_streak, highest_score,
        highest_streak, total_battles, total_correct_answers,
        total_questions, total_score, wins, user_id
    )
    SELECT
        gen_random_uuid(),
        NOW(),
        NOW(),
        floor(random() * 5),
        floor(random() * 100),
        floor(random() * 10),
        floor(random() * 50),
        floor(random() * 200),
        floor(random() * 300),
        floor(random() * 1000),
        floor(random() * 20),
        id
    FROM initial_users
    RETURNING id, user_id
),
-- 사용자 레벨 생성
inserted_user_levels AS (
    INSERT INTO public.user_levels (
        id, created_at, updated_at, current_exp, level,
        required_exp, user_id
    )
    SELECT
        gen_random_uuid(),
        NOW(),
        NOW(),
        current_exp,  -- initial_users에서 가져온 현재 경험치
        level,        -- initial_users에서 가져온 레벨
        required_exp, -- initial_users에서 가져온 필요 경험치
        id
    FROM initial_users
    RETURNING id, user_id
),
-- 사용자 업적 생성
inserted_achievements AS (
    INSERT INTO public.user_achievements (
        user_level_id, achievements
    )
    SELECT 
        ul.id,
        achievement
    FROM inserted_user_levels ul
    CROSS JOIN (
        VALUES 
            ('FIRST_QUIZ_COMPLETED'),
            ('PERFECT_SCORE'),
            ('WINNING_STREAK_3'),
            ('WINNING_STREAK_5'),
            ('DAILY_QUIZ_MASTER')
    ) AS achievements(achievement)
)
-- 최종 결과 확인
SELECT 'Data insertion completed successfully.' as result;