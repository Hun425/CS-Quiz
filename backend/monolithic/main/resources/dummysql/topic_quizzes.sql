-- admin 사용자 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
)
-- 주제별 퀴즈 생성
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE
        WHEN (seq % 12) = 0 THEN '자바스크립트 기초'
        WHEN (seq % 12) = 1 THEN '파이썬 입문'
        WHEN (seq % 12) = 2 THEN '데이터베이스 설계 패턴'
        WHEN (seq % 12) = 3 THEN '알고리즘 도전'
        WHEN (seq % 12) = 4 THEN '자료구조 심화'
        WHEN (seq % 12) = 5 THEN '시스템 설계 인터뷰 준비'
        WHEN (seq % 12) = 6 THEN '네트워킹 기초'
        WHEN (seq % 12) = 7 THEN '운영체제 개념'
        WHEN (seq % 12) = 8 THEN '현대 웹 개발'
        WHEN (seq % 12) = 9 THEN '데브옵스 실전'
        WHEN (seq % 12) = 10 THEN '머신러닝 기초'
        ELSE '보안 모범 사례'
        END || ' Vol. ' || (seq / 12 + 1),
    CASE
        WHEN (seq % 12) = 0 THEN '자바스크립트 핵심 개념과 프로그래밍 기법에 관한 포괄적인 퀴즈'
        WHEN (seq % 12) = 1 THEN '초보 프로그래머를 위한 파이썬 기초 퀴즈'
        WHEN (seq % 12) = 2 THEN '데이터베이스 설계 원칙과 실용적인 패턴에 관한 퀴즈'
        WHEN (seq % 12) = 3 THEN '일반적인 알고리즘 문제와 문제 해결 접근법에 관한 퀴즈'
        WHEN (seq % 12) = 4 THEN '고급 자료구조와 그 응용에 관한 퀴즈'
        WHEN (seq % 12) = 5 THEN '기술 인터뷰를 위한 시스템 설계 원칙 퀴즈'
        WHEN (seq % 12) = 6 THEN '컴퓨터 네트워킹 기초와 프로토콜에 관한 퀴즈'
        WHEN (seq % 12) = 7 THEN '운영체제 내부 구조와 메커니즘에 관한 퀴즈'
        WHEN (seq % 12) = 8 THEN '현대 웹 개발 기술과 관행에 관한 퀴즈'
        WHEN (seq % 12) = 9 THEN '데브옵스 원칙과 CI/CD 구현에 관한 퀴즈'
        WHEN (seq % 12) = 10 THEN '머신러닝 알고리즘과 응용에 관한 퀴즈'
        ELSE '보안 취약점과 보호 메커니즘에 관한 퀴즈'
        END,
    CASE floor(seq/12)
        WHEN 0 THEN 'BEGINNER'
        WHEN 1 THEN 'INTERMEDIATE'
        ELSE 'ADVANCED'
        END,
    true,
    10, -- 각 주제별 퀴즈는 10개 문제
    'TOPIC_BASED',
    30, -- 30분 제한시간
    (SELECT id FROM admin_user),
    floor(random() * 80 + 20), -- 20-100 시도 횟수
    random() * 35 + 65, -- 65-100 평균 점수
    floor(random() * 400 + 100), -- 100-500 조회수
    NULL -- 주제별 퀴즈는 만료일 없음
FROM generate_series(0, 35) AS seq;