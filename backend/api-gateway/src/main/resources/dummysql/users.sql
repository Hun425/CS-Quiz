-- 사용자 관련 데이터 준비
WITH user_level_info AS (
    SELECT
        unnest(ARRAY['admin', '김철수', '이영희', '박민준', '정수연', '최지훈']) as username,
        unnest(ARRAY[10, 5, 3, 7, 2, 4]) as level,
        unnest(ARRAY[800, 450, 250, 620, 150, 380]) as current_exp,
        unnest(ARRAY[1000, 600, 400, 800, 300, 500]) as required_exp,
        unnest(ARRAY[5000, 2200, 1500, 3500, 800, 1900]) as total_points,
        unnest(ARRAY['ADMIN', 'USER', 'USER', 'USER', 'USER', 'USER']) as role
)
-- 기본 사용자 데이터 생성
INSERT INTO public.users (
    created_at, updated_at, email, username, profile_image, is_active, provider, provider_id, role,
    total_points, level, experience, required_experience, last_login
)
SELECT
    NOW() - (random() * INTERVAL '365 days'),
    NOW(),
    username || '@example.com',
    username,
    'https://robohash.org/' || username || '?set=set4',
    true,
    CASE
        WHEN random() < 0.33 THEN 'GOOGLE'
        WHEN random() < 0.66 THEN 'GITHUB'
        ELSE 'KAKAO'
        END,
    username || '_id_' || floor(random() * 1000)::text,
    role,
    total_points,
    level,
    current_exp,
    required_exp,
    NOW() - (random() * INTERVAL '30 days')
FROM user_level_info;