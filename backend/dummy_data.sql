-- 기본 사용자 데이터를 위한 레벨 정보 준비
WITH user_level_info AS (
    SELECT
        unnest(ARRAY['admin', 'user1', 'user2']) as username,
        unnest(ARRAY[5, 2, 1]) as level,
        unnest(ARRAY[500, 200, 150]) as current_exp,
        unnest(ARRAY[1000, 400, 200]) as required_exp,
        unnest(ARRAY[1000, 500, 300]) as total_points
),
-- 기본 사용자 데이터 생성 (id 컬럼은 auto increment로 생성)
     initial_users AS (
INSERT INTO public.users (
    created_at, email, is_active, provider, provider_id, role,
    total_points, updated_at, username, experience, level,
    required_experience, last_login
)
SELECT
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
         RETURNING id, username, role, level, experience, required_experience
),
-- 태그 생성 (id 자동 생성)
inserted_tags AS (
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), 'JavaScript', 'JavaScript programming language'),
    (NOW(), 'Python', 'Python programming language'),
    (NOW(), 'Database', 'Database concepts and SQL')
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
-- 퀴즈 생성을 위한 시퀀스 준비 (id는 제거)
    quiz_sequence AS (
SELECT
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
-- 퀴즈 생성 (id는 auto increment)
    inserted_quizzes AS (
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count
)
SELECT
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
-- 문제 생성을 위한 시퀀스 준비 (id 생성은 DB에 맡김)
    question_sequence AS (
SELECT
    q.id as quiz_id,
    s.seq
FROM inserted_quizzes q
    CROSS JOIN generate_series(1, 10) s(seq)
    ),
-- 문제 생성 (id는 auto increment)
    inserted_questions AS (
INSERT INTO public.questions (
    question_text, question_type, difficulty_level,
    correct_answer, options, points, created_at, updated_at,
    quiz_id
)
SELECT
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
SELECT id, experience, level, required_experience
FROM initial_users
WHERE role = 'USER'
    ),
-- 퀴즈 시도 생성 (id auto increment)
-- 테이블 정의에 없는 last_answered_question_index와 remaining_time은 제거함
    inserted_quiz_attempts AS (
INSERT INTO public.quiz_attempts (
    created_at, start_time, end_time,
    is_completed, score, time_taken,
    quiz_id, user_id
)
SELECT
    NOW(),
    NOW() - interval '1 hour',
    NOW(),
    true,
    floor(random() * 100),
    floor(random() * 1800),
    q.id,
    u.id
FROM inserted_quizzes q
    CROSS JOIN normal_users u
    LIMIT 10
    RETURNING id, quiz_id, user_id
    ),
-- 퀴즈 리뷰 생성 (id auto increment)
    inserted_reviews AS (
INSERT INTO public.quiz_reviews (
    content, created_at, rating,
    quiz_id, reviewer_id
)
SELECT
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
-- 사용자 전투 통계 생성 (id auto increment)
    inserted_battle_stats AS (
INSERT INTO public.user_battle_stats (
    created_at, updated_at, current_streak, highest_score,
    highest_streak, total_battles, total_correct_answers,
    total_questions, total_score, wins, user_id
)
SELECT
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
-- 사용자 레벨 생성 (id auto increment)
    inserted_user_levels AS (
INSERT INTO public.user_levels (
    created_at, updated_at, current_exp, "level",
    required_exp, user_id  -- required_experience를 required_exp로 수정
)
SELECT
    NOW(),
    NOW(),
    experience,
    "level",
    required_experience,
    id
FROM initial_users
    RETURNING id, user_id
    ),
-- 사용자 업적 생성 (id auto increment)
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
    ),
-- 배틀룸 생성을 위한 기초 데이터 준비 (퀴즈 ID와 일반 사용자 ID 미리 조회)
    room_base_data AS (
SELECT
    q.id as quiz_id,
    array_agg(u.id) as user_ids
FROM public.quizzes q
    CROSS JOIN (
    SELECT id FROM public.users WHERE role = 'USER'
    ) u
GROUP BY q.id
    LIMIT 3
    ),
-- 배틀룸 생성 (id 컬럼은 auto increment)
    inserted_battle_rooms AS (
INSERT INTO public.battle_rooms (
    created_at,
    updated_at,
    room_code,
    status,
    max_participants,
    current_question_index,
    start_time,
    end_time,
    "version",
    quiz_id
)
SELECT
    NOW() - interval '1 hour' * rn,
    NOW() - interval '1 hour' * rn + interval '5 minutes',
    'BATTLE_' || LPAD(rn::text, 4, '0'),
    CASE rn % 3
    WHEN 0 THEN 'WAITING'
    WHEN 1 THEN 'IN_PROGRESS'
    ELSE 'FINISHED'
    END,
    4,
    CASE rn % 3
    WHEN 0 THEN 0
    WHEN 1 THEN floor(random() * 3 + 1)
    ELSE 5
    END,
    CASE rn % 3
    WHEN 0 THEN NULL
    ELSE NOW() - interval '1 hour' * rn + interval '1 minute'
    END,
    CASE rn % 3
    WHEN 2 THEN NOW() - interval '1 hour' * rn + interval '30 minutes'
    ELSE NULL
    END,
    1,
    quiz_id
FROM room_base_data,
    generate_series(1, 3) as rn
    RETURNING *
    ),
-- 배틀 참가자 생성 (id 컬럼은 auto increment)
    inserted_participants AS (
INSERT INTO public.battle_participants (
    created_at,
    last_activity,
    current_score,
    is_ready,
    is_active,
    current_streak,
    battle_room_id,
    user_id
)
SELECT
    br.created_at + interval '10 seconds' * participant_num,
    CASE br.status
    WHEN 'WAITING' THEN br.created_at + interval '10 seconds' * participant_num
    WHEN 'IN_PROGRESS' THEN NOW()
    ELSE br.end_time
    END,
    CASE br.status
    WHEN 'WAITING' THEN 0
    WHEN 'IN_PROGRESS' THEN floor(random() * 200)
    ELSE floor(random() * 500)
    END,
    CASE br.status
    WHEN 'WAITING' THEN random() > 0.5
    ELSE true
    END,
    br.status != 'FINISHED',
    CASE br.status
    WHEN 'FINISHED' THEN floor(random() * 5)
    ELSE floor(random() * 3)
    END,
    br.id,
    u.id
FROM inserted_battle_rooms br
    CROSS JOIN generate_series(1, 3) as participant_num
    CROSS JOIN (
    SELECT id FROM public.users WHERE role = 'USER' ORDER BY random() LIMIT 1
    ) u
    RETURNING *
    ),
-- 배틀 답변 생성 (id 컬럼은 auto increment)
    inserted_battle_answers AS (
INSERT INTO public.battle_answers (
    created_at,
    -- answer_time 컬럼은 DDL에 정의되어 있지 않으므로 제거합니다
    answer,
    is_correct,
    earned_points,
    time_bonus,
    time_taken,
    participant_id,
    question_id
)
SELECT
    p.created_at + interval '1 minute' * answer_num,
    -- p.created_at + interval '1 minute' * answer_num + interval '10 seconds' * floor(random() * 6), -- answer_time 관련 값 제거
    CASE floor(random() * 4)
    WHEN 0 THEN 'Option A'
    WHEN 1 THEN 'Option B'
    WHEN 2 THEN 'Option C'
    ELSE 'Option D'
    END,
    random() > 0.4,
    CASE
    WHEN random() > 0.4 THEN floor(random() * 50) + 50
    ELSE 0
    END,
    floor(random() * 30),
    floor(random() * 20) + 5,
    p.id,
    q.id
FROM inserted_participants p
    CROSS JOIN generate_series(1, 5) as answer_num
    CROSS JOIN (
    SELECT id FROM public.questions ORDER BY random() LIMIT 1
    ) q
WHERE EXISTS (
    SELECT 1 FROM inserted_battle_rooms br
    WHERE br.id = p.battle_room_id
  AND br.status != 'WAITING'
    )
    ),
-- 승자 업데이트: 각 방의 승자 결정 후 배틀룸 업데이트
    winners_update AS (
WITH winner_selection AS (
    SELECT DISTINCT ON (bp.battle_room_id)
    bp.battle_room_id,
    bp.id AS winner_id
    FROM inserted_participants bp
    JOIN inserted_battle_rooms br ON br.id = bp.battle_room_id
    WHERE br.status = 'FINISHED'
    ORDER BY bp.battle_room_id, bp.current_score DESC
    )
UPDATE public.battle_rooms
SET winner_id = ws.winner_id
FROM winner_selection ws
WHERE battle_rooms.id = ws.battle_room_id
  AND battle_rooms.status = 'FINISHED'
    )
-- 사용자 업적 이력 데이터 생성
    , inserted_achievement_history AS (
INSERT INTO public.user_achievement_history (
    user_id,
    achievement,
    achievement_name,
    earned_at
)
SELECT
    u.id,
    achievement,
    CASE achievement
    WHEN 'FIRST_QUIZ_COMPLETED' THEN '첫 퀴즈 완료'
    WHEN 'PERFECT_SCORE' THEN '완벽한 점수'
    WHEN 'WINNING_STREAK_3' THEN '3연승'
    WHEN 'WINNING_STREAK_5' THEN '5연승'
    WHEN 'DAILY_QUIZ_MASTER' THEN '데일리 퀴즈 마스터'
    END as achievement_name,
    NOW() - (INTERVAL '1 day' * (random() * 30)::integer)
FROM public.users u
    CROSS JOIN (
    SELECT unnest(ARRAY[
    'FIRST_QUIZ_COMPLETED',
    'PERFECT_SCORE',
    'WINNING_STREAK_3',
    'WINNING_STREAK_5',
    'DAILY_QUIZ_MASTER'
    ]) as achievement
    ) a
WHERE u.role = 'USER'
    LIMIT 15 -- 각 사용자마다 몇 개의 업적을 생성할지 제한
    )

-- 사용자 레벨업 이력 데이터 생성
    , inserted_level_history AS (
INSERT INTO public.user_level_history (
    user_id,
    previous_level,
    level,
    updated_at
)
SELECT
    u.id,
    u.level - level_inc,
    u.level,
    NOW() - (INTERVAL '1 day' * ((5 - level_inc) * 5 + (random() * 5)::integer))
FROM public.users u
    CROSS JOIN generate_series(1, 3) as level_inc
WHERE u.level >= level_inc -- 현재 레벨보다 낮은 과거 레벨만 생성
ORDER BY u.id, level_inc DESC
    )

-- 최종 결과 확인
SELECT 'Battle system data insertion completed successfully.' as result;