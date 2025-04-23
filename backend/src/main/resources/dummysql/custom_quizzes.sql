-- 모든 사용자의 ID 가져오기
WITH user_ids AS (
    SELECT id FROM public.users
)
-- 커스텀 퀴즈 생성
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days'),
    '맞춤 퀴즈: ' || (
        CASE floor(random() * 10)
            WHEN 0 THEN '자바스크립트 트릭'
            WHEN 1 THEN '파이썬 원라이너'
            WHEN 2 THEN 'SQL 최적화'
            WHEN 3 THEN '자료구조 퍼즐'
            WHEN 4 THEN '리액트 컴포넌트 패턴'
            WHEN 5 THEN 'RESTful API 설계'
            WHEN 6 THEN '비동기 프로그래밍'
            WHEN 7 THEN '도커 컨테이너'
            WHEN 8 THEN '깃 워크플로우'
            ELSE '웹 성능 최적화'
            END
        ) || ' #' || seq,
    '사용자가 생성한 ' || (
        CASE floor(random() * 10)
            WHEN 0 THEN '자바스크립트 트릭과 패턴에 관한 퀴즈'
            WHEN 1 THEN '파이썬 원라이너와 효율적인 코드에 관한 퀴즈'
            WHEN 2 THEN 'SQL 쿼리 최적화 기법에 관한 퀴즈'
            WHEN 3 THEN '복잡한 자료구조 문제에 관한 퀴즈'
            WHEN 4 THEN '리액트 컴포넌트 디자인 패턴에 관한 퀴즈'
            WHEN 5 THEN 'RESTful API 설계 원칙에 관한 퀴즈'
            WHEN 6 THEN '비동기 프로그래밍 기법에 관한 퀴즈'
            WHEN 7 THEN '도커 컨테이너 모범 사례에 관한 퀴즈'
            WHEN 8 THEN '깃 워크플로우와 협업에 관한 퀴즈'
            ELSE '웹 성능 최적화 전략에 관한 퀴즈'
            END
        ),
    CASE seq % 3
        WHEN 0 THEN 'BEGINNER'
        WHEN 1 THEN 'INTERMEDIATE'
        ELSE 'ADVANCED'
        END,
    CASE
        WHEN seq % 5 = 0 THEN false  -- 20%는 비공개 퀴즈
        ELSE true                  -- 80%는 공개 퀴즈
        END,
    floor(random() * 5) + 3, -- 3-7개 문제
    'CUSTOM',
    20, -- 20분 제한시간
    (SELECT id FROM user_ids ORDER BY random() LIMIT 1), -- 랜덤 사용자
    floor(random() * 50 + 5), -- 5-55 시도 횟수
    random() * 30 + 65, -- 65-95 평균 점수
    floor(random() * 200 + 30), -- 30-230 조회수
    NULL -- 커스텀 퀴즈는 만료일 없음
FROM generate_series(0, 14) AS seq;