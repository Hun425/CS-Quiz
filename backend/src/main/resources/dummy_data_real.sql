-- CS-Quiz 플랫폼을 위한 향상된 더미 데이터
-- 이 스크립트는 애플리케이션 테스트를 위한 포괄적인 테스트 데이터를 제공합니다

-- 필요한 경우 기존 데이터 삭제 (주의: 필요한 경우에만 주석 해제)
-- TRUNCATE TABLE users, user_levels, user_battle_stats, user_achievements, quiz_attempts, question_attempts,
--              quizzes, questions, tags, quiz_tags, tag_synonyms, battle_rooms, battle_participants, battle_answers,
--              quiz_reviews, quiz_review_comments, user_achievement_history, user_level_history CASCADE;

-- 사용자 관련 데이터 준비
WITH user_level_info AS (
    SELECT
        unnest(ARRAY['admin', '김철수', '이영희', '박민준', '정수연', '최지훈', '한지민', '강동원', '임세희', '윤서준', '송지은', '조현우']) as username,
        unnest(ARRAY[10, 5, 3, 7, 2, 4, 6, 8, 3, 5, 4, 7]) as level,
        unnest(ARRAY[800, 450, 250, 620, 150, 380, 520, 750, 270, 430, 350, 680]) as current_exp,
        unnest(ARRAY[1000, 600, 400, 800, 300, 500, 700, 900, 400, 600, 500, 800]) as required_exp,
        unnest(ARRAY[5000, 2200, 1500, 3500, 800, 1900, 2800, 4000, 1200, 2500, 1800, 3200]) as total_points,
        unnest(ARRAY['ADMIN', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER', 'USER']) as role
),
-- 기본 사용자 데이터 생성
     initial_users AS (
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
             FROM user_level_info
             RETURNING id, username, role, level, experience, required_experience
     ),
-- 다양한 CS 주제에 대한 태그 생성
     inserted_tags AS (
         INSERT INTO public.tags (created_at, name, description)
             VALUES
                 (NOW(), '자바스크립트', '자바스크립트 프로그래밍 언어, 프레임워크 및 모범 사례'),
                 (NOW(), '파이썬', '파이썬 프로그래밍 언어, 라이브러리 및 응용 프로그램'),
                 (NOW(), '데이터베이스', '데이터베이스 개념, SQL, NoSQL, 최적화 및 설계'),
                 (NOW(), '알고리즘', '알고리즘 설계, 복잡도 분석 및 구현'),
                 (NOW(), '자료구조', '일반적인 자료구조, 관련 연산 및 구현'),
                 (NOW(), '시스템설계', '아키텍처, 분산 시스템 및 고수준 설계'),
                 (NOW(), '네트워크', '네트워킹 프로토콜, 모델 및 인프라'),
                 (NOW(), '운영체제', 'OS 개념, 프로세스, 메모리 관리 및 파일 시스템'),
                 (NOW(), '웹개발', '웹 기술, 프레임워크 및 디자인 패턴'),
                 (NOW(), '데브옵스', '지속적 통합, 배포 및 인프라 코드화'),
                 (NOW(), '머신러닝', 'ML 알고리즘, 신경망 및 응용'),
                 (NOW(), '보안', '보안 원칙, 취약점 및 모범 사례'),
                 (NOW(), '클라우드컴퓨팅', '클라우드 서비스, 아키텍처 및 배포 모델'),
                 (NOW(), '모바일개발', '모바일 앱 개발 및 플랫폼별 기술'),
                 (NOW(), '프론트엔드', '프론트엔드 기술, 프레임워크 및 라이브러리'),
                 (NOW(), '백엔드', '백엔드 기술, 서버 프로그래밍 및 API 설계'),
                 (NOW(), '데이터분석', '데이터 처리, 시각화 및 통계 분석'),
                 (NOW(), '컴퓨터비전', '이미지 처리, 객체 인식 및 시각적 분석'),
                 (NOW(), '자연어처리', '텍스트 분석, 언어 모델 및 챗봇'),
                 (NOW(), '블록체인', '분산 원장 기술, 암호화폐 및 스마트 계약')
             RETURNING id, name
     ),
-- 관리자 사용자 ID 가져오기
     admin_user AS (
         SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
     ),
-- 태그 관계 생성 (부모-자식)
     tag_relationships AS (
         -- 부모-자식 관계를 설정하기 위해 특정 태그의 ID 가져오기
         WITH tag_ids AS (
             SELECT id, name FROM inserted_tags
             )
             -- 웹개발 하위 태그들
             INSERT INTO public.tags (created_at, name, description, parent_id)
                 SELECT
                     NOW(),
                     name,
                     description,
                     (SELECT id FROM tag_ids WHERE name = '웹개발')
                 FROM (
                          VALUES
                              ('리액트', 'React.js 프론트엔드 라이브러리'),
                              ('앵귤러', 'Angular 프레임워크'),
                              ('뷰', 'Vue.js 프로그레시브 프레임워크'),
                              ('노드JS', 'Node.js 서버 사이드 JavaScript 환경'),
                              ('Next.js', 'React 기반 프레임워크'),
                              ('Express', 'Node.js를 위한 웹 프레임워크'),
                              ('GraphQL', '데이터 쿼리 및 조작 언어'),
                              ('RESTful API', 'REST 아키텍처 기반 API'),
                              ('웹표준', 'HTML, CSS, 접근성 관련 표준')
                      ) as subtags(name, description)
                 RETURNING id, name, parent_id
     ),
-- 프론트엔드 하위 태그 생성
     frontend_subtags AS (
         WITH tag_ids AS (
             SELECT id, name FROM inserted_tags
             )
             INSERT INTO public.tags (created_at, name, description, parent_id)
                 SELECT
                     NOW(),
                     name,
                     description,
                     (SELECT id FROM tag_ids WHERE name = '프론트엔드')
                 FROM (
                          VALUES
                              ('HTML', 'HyperText Markup Language'),
                              ('CSS', 'Cascading Style Sheets'),
                              ('TypeScript', '정적 타입이 있는 JavaScript'),
                              ('Redux', 'JavaScript 상태 관리 라이브러리'),
                              ('Webpack', '모듈 번들러'),
                              ('Jest', 'JavaScript 테스팅 프레임워크'),
                              ('UI/UX', '사용자 인터페이스와 경험 디자인'),
                              ('Sass', 'CSS 전처리기'),
                              ('Tailwind', '유틸리티-퍼스트 CSS 프레임워크')
                      ) as subtags(name, description)
                 RETURNING id, name, parent_id
     ),
-- 백엔드 하위 태그 생성
     backend_subtags AS (
         WITH tag_ids AS (
             SELECT id, name FROM inserted_tags
             )
             INSERT INTO public.tags (created_at, name, description, parent_id)
                 SELECT
                     NOW(),
                     name,
                     description,
                     (SELECT id FROM tag_ids WHERE name = '백엔드')
                 FROM (
                          VALUES
                              ('스프링', 'Java 기반 백엔드 프레임워크'),
                              ('Django', 'Python 웹 프레임워크'),
                              ('Flask', 'Python 마이크로 웹 프레임워크'),
                              ('FastAPI', '현대적인 고성능 Python 웹 프레임워크'),
                              ('PHP', 'PHP 서버 사이드 스크립팅 언어'),
                              ('ASP.NET', 'Microsoft의 웹 프레임워크'),
                              ('Ruby on Rails', 'Ruby 웹 애플리케이션 프레임워크'),
                              ('Hibernate', 'Java ORM 프레임워크'),
                              ('JWT', 'JSON Web Token 인증'),
                              ('OAuth', '권한 부여 표준 프로토콜')
                      ) as subtags(name, description)
                 RETURNING id, name, parent_id
     ),
-- 데이터베이스 하위 태그 생성
     db_subtags AS (
         WITH tag_ids AS (
             SELECT id, name FROM inserted_tags
             )
             INSERT INTO public.tags (created_at, name, description, parent_id)
                 SELECT
                     NOW(),
                     name,
                     description,
                     (SELECT id FROM tag_ids WHERE name = '데이터베이스')
                 FROM (
                          VALUES
                              ('SQL', '구조화 질의 언어'),
                              ('MySQL', '오픈소스 관계형 데이터베이스'),
                              ('PostgreSQL', '고급 오픈소스 관계형 데이터베이스'),
                              ('MongoDB', '문서 기반 NoSQL 데이터베이스'),
                              ('Redis', '인메모리 데이터 구조 저장소'),
                              ('Firebase', 'Google의 실시간 데이터베이스'),
                              ('ORM', '객체 관계 매핑'),
                              ('데이터 모델링', '데이터베이스 구조 설계'),
                              ('쿼리 최적화', '데이터베이스 쿼리 성능 개선'),
                              ('트랜잭션', '데이터베이스 트랜잭션 관리')
                      ) as subtags(name, description)
                 RETURNING id, name, parent_id
     ),
-- 태그 동의어 추가
     tag_synonyms_insert AS (
         WITH tag_map AS (
             SELECT id, name FROM inserted_tags
             )
             INSERT INTO public.tag_synonyms (tag_id, synonym)
                 VALUES
                     ((SELECT id FROM tag_map WHERE name = '자바스크립트'), 'JS'),
                     ((SELECT id FROM tag_map WHERE name = '자바스크립트'), '자스'),
                     ((SELECT id FROM tag_map WHERE name = '파이썬'), 'Python'),
                     ((SELECT id FROM tag_map WHERE name = '파이썬'), 'py'),
                     ((SELECT id FROM tag_map WHERE name = '데이터베이스'), 'DB'),
                     ((SELECT id FROM tag_map WHERE name = '알고리즘'), '알고'),
                     ((SELECT id FROM tag_map WHERE name = '자료구조'), '자구'),
                     ((SELECT id FROM tag_map WHERE name = '웹개발'), '웹'),
                     ((SELECT id FROM tag_map WHERE name = '머신러닝'), 'ML'),
                     ((SELECT id FROM tag_map WHERE name = '머신러닝'), '기계학습'),
                     ((SELECT id FROM tag_map WHERE name = '클라우드컴퓨팅'), 'Cloud'),
                     ((SELECT id FROM tag_map WHERE name = '모바일개발'), 'Mobile'),
                     ((SELECT id FROM tag_map WHERE name = '프론트엔드'), 'Frontend'),
                     ((SELECT id FROM tag_map WHERE name = '백엔드'), 'Backend'),
                     ((SELECT id FROM tag_map WHERE name = '데이터분석'), 'Data Analysis'),
                     ((SELECT id FROM tag_map WHERE name = '컴퓨터비전'), 'CV'),
                     ((SELECT id FROM tag_map WHERE name = '자연어처리'), 'NLP'),
                     ((SELECT id FROM tag_map WHERE name = '블록체인'), 'Blockchain')
     ),
-- 모든 사용자의 ID 가져오기
     user_ids AS (
         SELECT id FROM initial_users
     ),
-- 데일리 퀴즈 생성 부분
     daily_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             SELECT
                 NOW() - (seq * INTERVAL '1 day'),
                 NOW() - (seq * INTERVAL '1 day'),
                 '오늘의 CS 퀴즈 #' || (60-seq),
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
                     WHEN seq = 0 THEN (NOW() + INTERVAL '1 day') -- 오늘의 퀴즈는 내일까지 유효
                     ELSE (NOW() - ((seq-1) * INTERVAL '1 day')) -- 과거 퀴즈는 이미 만료됨
                     END
             FROM generate_series(0, 59) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count
     ),
     topic_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             SELECT
                 (NOW() - (random() * INTERVAL '180 days')),
                 (NOW() - (random() * INTERVAL '30 days')),
                 CASE
                     WHEN (seq % 20) = 0 THEN '자바스크립트 기초'
                     WHEN (seq % 20) = 1 THEN '파이썬 입문'
                     WHEN (seq % 20) = 2 THEN '데이터베이스 설계 패턴'
                     WHEN (seq % 20) = 3 THEN '알고리즘 도전'
                     WHEN (seq % 20) = 4 THEN '자료구조 심화'
                     WHEN (seq % 20) = 5 THEN '시스템 설계 인터뷰 준비'
                     WHEN (seq % 20) = 6 THEN '네트워킹 기초'
                     WHEN (seq % 20) = 7 THEN '운영체제 개념'
                     WHEN (seq % 20) = 8 THEN '현대 웹 개발'
                     WHEN (seq % 20) = 9 THEN '데브옵스 실전'
                     WHEN (seq % 20) = 10 THEN '머신러닝 기초'
                     WHEN (seq % 20) = 11 THEN '보안 모범 사례'
                     WHEN (seq % 20) = 12 THEN '클라우드 서비스 입문'
                     WHEN (seq % 20) = 13 THEN '모바일 앱 개발'
                     WHEN (seq % 20) = 14 THEN '프론트엔드 심화'
                     WHEN (seq % 20) = 15 THEN '백엔드 아키텍처'
                     WHEN (seq % 20) = 16 THEN '데이터 분석 실무'
                     WHEN (seq % 20) = 17 THEN '컴퓨터 비전 기초'
                     WHEN (seq % 20) = 18 THEN '자연어 처리 입문'
                     ELSE '블록체인 이해하기'
                     END || ' Vol. ' || (seq / 20 + 1),
                 CASE
                     WHEN (seq % 20) = 0 THEN '자바스크립트 핵심 개념과 프로그래밍 기법에 관한 포괄적인 퀴즈'
                     WHEN (seq % 20) = 1 THEN '초보 프로그래머를 위한 파이썬 기초 퀴즈'
                     WHEN (seq % 20) = 2 THEN '데이터베이스 설계 원칙과 실용적인 패턴에 관한 퀴즈'
                     WHEN (seq % 20) = 3 THEN '일반적인 알고리즘 문제와 문제 해결 접근법에 관한 퀴즈'
                     WHEN (seq % 20) = 4 THEN '고급 자료구조와 그 응용에 관한 퀴즈'
                     WHEN (seq % 20) = 5 THEN '기술 인터뷰를 위한 시스템 설계 원칙 퀴즈'
                     WHEN (seq % 20) = 6 THEN '컴퓨터 네트워킹 기초와 프로토콜에 관한 퀴즈'
                     WHEN (seq % 20) = 7 THEN '운영체제 내부 구조와 메커니즘에 관한 퀴즈'
                     WHEN (seq % 20) = 8 THEN '현대 웹 개발 기술과 관행에 관한 퀴즈'
                     WHEN (seq % 20) = 9 THEN '데브옵스 원칙과 CI/CD 구현에 관한 퀴즈'
                     WHEN (seq % 20) = 10 THEN '머신러닝 알고리즘과 응용에 관한 퀴즈'
                     WHEN (seq % 20) = 11 THEN '보안 취약점과 보호 메커니즘에 관한 퀴즈'
                     WHEN (seq % 20) = 12 THEN '클라우드 서비스 모델과 아키텍처에 관한 퀴즈'
                     WHEN (seq % 20) = 13 THEN '모바일 앱 개발 원칙과 패턴에 관한 퀴즈'
                     WHEN (seq % 20) = 14 THEN '프론트엔드 최적화와 패턴에 관한 퀴즈'
                     WHEN (seq % 20) = 15 THEN '백엔드 확장성과 성능에 관한 퀴즈'
                     WHEN (seq % 20) = 16 THEN '데이터 처리 파이프라인과 시각화에 관한 퀴즈'
                     WHEN (seq % 20) = 17 THEN '이미지 인식과 처리 알고리즘에 관한 퀴즈'
                     WHEN (seq % 20) = 18 THEN '텍스트 분석과 언어 모델에 관한 퀴즈'
                     ELSE '블록체인 기술과 암호화폐에 관한 퀴즈'
                     END,
                 CASE floor(seq/20)
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
             FROM generate_series(0, 99) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count, title
     ),

-- 커스텀 퀴즈 생성 (개수 증가: 15 -> 50)
     custom_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             SELECT
                 (NOW() - (random() * INTERVAL '90 days')),
                 (NOW() - (random() * INTERVAL '30 days')),
                 '맞춤 퀴즈: ' || (
                     CASE floor(random() * 20)
                         WHEN 0 THEN '자바스크립트 트릭'
                         WHEN 1 THEN '파이썬 원라이너'
                         WHEN 2 THEN 'SQL 최적화'
                         WHEN 3 THEN '자료구조 퍼즐'
                         WHEN 4 THEN '리액트 컴포넌트 패턴'
                         WHEN 5 THEN 'RESTful API 설계'
                         WHEN 6 THEN '비동기 프로그래밍'
                         WHEN 7 THEN '도커 컨테이너'
                         WHEN 8 THEN '깃 워크플로우'
                         WHEN 9 THEN '웹 성능 최적화'
                         WHEN 10 THEN '클라우드 아키텍처'
                         WHEN 11 THEN '모바일 UI 패턴'
                         WHEN 12 THEN '데이터 시각화'
                         WHEN 13 THEN '머신러닝 모델'
                         WHEN 14 THEN '보안 취약점'
                         WHEN 15 THEN '마이크로서비스 아키텍처'
                         WHEN 16 THEN '함수형 프로그래밍'
                         WHEN 17 THEN '테스트 자동화'
                         WHEN 18 THEN '디자인 패턴'
                         ELSE '애자일 방법론'
                         END
                     ) || ' #' || seq,
                 '사용자가 생성한 ' || (
                     CASE floor(random() * 20)
                         WHEN 0 THEN '자바스크립트 트릭과 패턴에 관한 퀴즈'
                         WHEN 1 THEN '파이썬 원라이너와 효율적인 코드에 관한 퀴즈'
                         WHEN 2 THEN 'SQL 쿼리 최적화 기법에 관한 퀴즈'
                         WHEN 3 THEN '복잡한 자료구조 문제에 관한 퀴즈'
                         WHEN 4 THEN '리액트 컴포넌트 디자인 패턴에 관한 퀴즈'
                         WHEN 5 THEN 'RESTful API 설계 원칙에 관한 퀴즈'
                         WHEN 6 THEN '비동기 프로그래밍 기법에 관한 퀴즈'
                         WHEN 7 THEN '도커 컨테이너 모범 사례에 관한 퀴즈'
                         WHEN 8 THEN '깃 워크플로우와 협업에 관한 퀴즈'
                         WHEN 9 THEN '웹 성능 최적화 전략에 관한 퀴즈'
                         WHEN 10 THEN '클라우드 아키텍처 설계에 관한 퀴즈'
                         WHEN 11 THEN '모바일 UI 패턴과 사용자 경험에 관한 퀴즈'
                         WHEN 12 THEN '데이터 시각화 기법과 도구에 관한 퀴즈'
                         WHEN 13 THEN '머신러닝 모델 선택과 평가에 관한 퀴즈'
                         WHEN 14 THEN '보안 취약점 식별과 대응에 관한 퀴즈'
                         WHEN 15 THEN '마이크로서비스 아키텍처 원칙에 관한 퀴즈'
                         WHEN 16 THEN '함수형 프로그래밍 패러다임에 관한 퀴즈'
                         WHEN 17 THEN '테스트 자동화와 CI/CD에 관한 퀴즈'
                         WHEN 18 THEN '소프트웨어 디자인 패턴에 관한 퀴즈'
                         ELSE '애자일 방법론과 프로젝트 관리에 관한 퀴즈'
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
             FROM generate_series(0, 49) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count, title
     ),
-- 배틀용 퀴즈 생성 (새로 추가: 40개)
     battle_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             SELECT
                 (NOW() - (random() * INTERVAL '60 days')),
                 (NOW() - (random() * INTERVAL '20 days')),
                 -- 제목만 일반 퀴즈처럼 변경
                 CASE
                     WHEN seq % 10 = 0 THEN 'CS 기본기'
                     WHEN seq % 10 = 1 THEN '코딩 실력'
                     WHEN seq % 10 = 2 THEN '데이터 구조'
                     WHEN seq % 10 = 3 THEN '알고리즘'
                     WHEN seq % 10 = 4 THEN '웹 개발'
                     WHEN seq % 10 = 5 THEN '네트워크 지식'
                     WHEN seq % 10 = 6 THEN '데이터베이스'
                     WHEN seq % 10 = 7 THEN '보안'
                     WHEN seq % 10 = 8 THEN '클라우드'
                     ELSE '프로그래밍 언어'
                     END || ' 퀴즈 #' || seq,
                 '다양한 컴퓨터 과학 주제에 대한 퀴즈입니다.',
                 CASE seq % 3
                     WHEN 0 THEN 'BEGINNER'
                     WHEN 1 THEN 'INTERMEDIATE'
                     ELSE 'ADVANCED'
                     END,
                 true, -- 공개 퀴즈
                 7, -- 7개 문제
                 'BATTLE', -- quiz_type은 BATTLE로 유지
                 15, -- 15분 제한시간
                 (SELECT id FROM admin_user),
                 floor(random() * 100 + 30), -- 30-130 시도 횟수
                 random() * 20 + 75, -- 75-95 평균 점수
                 floor(random() * 300 + 100), -- 100-400 조회수
                 NULL -- 만료일 없음
             FROM generate_series(0, 39) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count, title
     ),
-- 모든 퀴즈 통합
     all_quizzes AS (
         SELECT id, quiz_type, difficulty_level, creator_id, question_count, 'DAILY' as category FROM daily_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count,
                CASE
                    WHEN title LIKE '%자바스크립트%' THEN '자바스크립트'
                    WHEN title LIKE '%파이썬%' THEN '파이썬'
                    WHEN title LIKE '%데이터베이스%' THEN '데이터베이스'
                    WHEN title LIKE '%알고리즘%' THEN '알고리즘'
                    WHEN title LIKE '%자료구조%' THEN '자료구조'
                    WHEN title LIKE '%시스템%' THEN '시스템설계'
                    WHEN title LIKE '%네트워킹%' OR title LIKE '%네트워크%' THEN '네트워크'
                    WHEN title LIKE '%운영체제%' THEN '운영체제'
                    WHEN title LIKE '%웹%' THEN '웹개발'
                    WHEN title LIKE '%데브옵스%' THEN '데브옵스'
                    WHEN title LIKE '%머신러닝%' THEN '머신러닝'
                    WHEN title LIKE '%보안%' THEN '보안'
                    WHEN title LIKE '%클라우드%' THEN '클라우드컴퓨팅'
                    WHEN title LIKE '%모바일%' THEN '모바일개발'
                    WHEN title LIKE '%프론트엔드%' THEN '프론트엔드'
                    WHEN title LIKE '%백엔드%' THEN '백엔드'
                    WHEN title LIKE '%데이터 분석%' THEN '데이터분석'
                    WHEN title LIKE '%컴퓨터 비전%' THEN '컴퓨터비전'
                    WHEN title LIKE '%자연어%' THEN '자연어처리'
                    WHEN title LIKE '%블록체인%' THEN '블록체인'
                    ELSE '웹개발' -- 기본 카테고리
                    END as category
         FROM topic_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count,
                CASE
                    WHEN title LIKE '%자바스크립트%' THEN '자바스크립트'
                    WHEN title LIKE '%파이썬%' THEN '파이썬'
                    WHEN title LIKE '%SQL%' THEN '데이터베이스'
                    WHEN title LIKE '%자료구조%' THEN '자료구조'
                    WHEN title LIKE '%리액트%' THEN '프론트엔드'
                    WHEN title LIKE '%API%' THEN '백엔드'
                    WHEN title LIKE '%도커%' THEN '데브옵스'
                    WHEN title LIKE '%깃%' THEN '데브옵스'
                    WHEN title LIKE '%웹%' THEN '웹개발'
                    WHEN title LIKE '%클라우드%' THEN '클라우드컴퓨팅'
                    WHEN title LIKE '%모바일%' THEN '모바일개발'
                    WHEN title LIKE '%데이터%' THEN '데이터분석'
                    WHEN title LIKE '%머신러닝%' THEN '머신러닝'
                    WHEN title LIKE '%보안%' THEN '보안'
                    WHEN title LIKE '%마이크로서비스%' THEN '백엔드'
                    WHEN title LIKE '%함수형%' THEN '자바스크립트'
                    WHEN title LIKE '%테스트%' THEN '데브옵스'
                    WHEN title LIKE '%디자인%' THEN '웹개발'
                    WHEN title LIKE '%애자일%' THEN '데브옵스'
                    ELSE '웹개발' -- 기본 카테고리
                    END as category
         FROM custom_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count,
                CASE
                    WHEN title LIKE '%CS%' THEN '알고리즘'
                    WHEN title LIKE '%코딩%' THEN '자바스크립트'
                    WHEN title LIKE '%데이터 구조%' THEN '자료구조'
                    WHEN title LIKE '%알고리즘%' THEN '알고리즘'
                    WHEN title LIKE '%웹%' THEN '웹개발'
                    WHEN title LIKE '%네트워크%' THEN '네트워크'
                    WHEN title LIKE '%데이터베이스%' THEN '데이터베이스'
                    WHEN title LIKE '%보안%' THEN '보안'
                    WHEN title LIKE '%클라우드%' THEN '클라우드컴퓨팅'
                    WHEN title LIKE '%프로그래밍%' THEN '자바스크립트'
                    ELSE floor(random() * 12)::text -- 랜덤 카테고리
                    END as category
         FROM battle_quizzes
     ),
-- 태그 맵 가져오기
     tag_map AS (
         SELECT id, name FROM inserted_tags
     ),
-- 퀴즈와 태그 연결 (주 태그 배정)
     quiz_primary_tags AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT
                 q.id AS quiz_id,
                 COALESCE(
                         (SELECT id FROM tag_map WHERE name = q.category),
                         (SELECT id FROM tag_map WHERE name = '웹개발') -- 기본 태그
                 ) AS tag_id
             FROM all_quizzes q
             RETURNING quiz_id, tag_id
     ),
-- 모든 퀴즈에 추가 태그 배정 (다중 태그 지원, 50% 확률로 2-3개 추가 태그 배정)
     quiz_additional_tags AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT
                 q.id AS quiz_id,
                 t.id AS tag_id
             FROM all_quizzes q
                      CROSS JOIN LATERAL (
                 SELECT id FROM inserted_tags
                 WHERE random() < 0.03 -- 각 태그를 선택할 낮은 확률
                   AND id NOT IN (SELECT tag_id FROM quiz_primary_tags WHERE quiz_id = q.id)
                 ORDER BY random()
                 LIMIT 2 -- 최대 2개의 추가 태그
                 ) AS t
             WHERE random() < 0.5 -- 50% 확률로 추가 태그 할당
             ON CONFLICT DO NOTHING -- 중복 방지
     ),
-- 웹개발 퀴즈에 웹개발 하위 태그 추가
     web_quiz_subtags AS (
         WITH web_quizzes AS (
             SELECT q.id AS quiz_id
             FROM all_quizzes q
                      JOIN quiz_primary_tags pt ON q.id = pt.quiz_id
                      JOIN tag_map tm ON pt.tag_id = tm.id
             WHERE tm.name = '웹개발'
             ),
             web_subtags AS (
                 SELECT t.id AS tag_id
                 FROM public.tags t
                 WHERE t.parent_id = (SELECT id FROM tag_map WHERE name = '웹개발')
                 )
             INSERT INTO public.quiz_tags (quiz_id, tag_id)
                 SELECT
                     wq.quiz_id,
                     ws.tag_id
                 FROM web_quizzes wq
                          CROSS JOIN LATERAL (
                     SELECT tag_id FROM web_subtags
                     WHERE random() < 0.3 -- 30% 확률로 각 하위 태그 선택
                     ORDER BY random()
                     LIMIT 2 -- 최대 2개의 하위 태그
                     ) AS ws
                 ON CONFLICT DO NOTHING
     ),
-- 프론트엔드 퀴즈에 프론트엔드 하위 태그 추가
     frontend_quiz_subtags AS (
         WITH frontend_quizzes AS (
             SELECT q.id AS quiz_id
             FROM all_quizzes q
                      JOIN quiz_primary_tags pt ON q.id = pt.quiz_id
                      JOIN tag_map tm ON pt.tag_id = tm.id
             WHERE tm.name = '프론트엔드'
             ),
             frontend_subtags AS (
                 SELECT t.id AS tag_id
                 FROM public.tags t
                 WHERE t.parent_id = (SELECT id FROM tag_map WHERE name = '프론트엔드')
                 )
             INSERT INTO public.quiz_tags (quiz_id, tag_id)
                 SELECT
                     fq.quiz_id,
                     fs.tag_id
                 FROM frontend_quizzes fq
                          CROSS JOIN LATERAL (
                     SELECT tag_id FROM frontend_subtags
                     WHERE random() < 0.3 -- 30% 확률로 각 하위 태그 선택
                     ORDER BY random()
                     LIMIT 2 -- 최대 2개의 하위 태그
                     ) AS fs
                 ON CONFLICT DO NOTHING
     ),
-- 백엔드 퀴즈에 백엔드 하위 태그 추가
     backend_quiz_subtags AS (
         WITH backend_quizzes AS (
             SELECT q.id AS quiz_id
             FROM all_quizzes q
                      JOIN quiz_primary_tags pt ON q.id = pt.quiz_id
                      JOIN tag_map tm ON pt.tag_id = tm.id
             WHERE tm.name = '백엔드'
             ),
             backend_subtags AS (
                 SELECT t.id AS tag_id
                 FROM public.tags t
                 WHERE t.parent_id = (SELECT id FROM tag_map WHERE name = '백엔드')
                 )
             INSERT INTO public.quiz_tags (quiz_id, tag_id)
                 SELECT
                     bq.quiz_id,
                     bs.tag_id
                 FROM backend_quizzes bq
                          CROSS JOIN LATERAL (
                     SELECT tag_id FROM backend_subtags
                     WHERE random() < 0.3 -- 30% 확률로 각 하위 태그 선택
                     ORDER BY random()
                     LIMIT 2 -- 최대 2개의 하위 태그
                     ) AS bs
                 ON CONFLICT DO NOTHING
     ),
-- 데이터베이스 퀴즈에 데이터베이스 하위 태그 추가
     db_quiz_subtags AS (
         WITH db_quizzes AS (
             SELECT q.id AS quiz_id
             FROM all_quizzes q
                      JOIN quiz_primary_tags pt ON q.id = pt.quiz_id
                      JOIN tag_map tm ON pt.tag_id = tm.id
             WHERE tm.name = '데이터베이스'
             ),
             db_subtags AS (
                 SELECT t.id AS tag_id
                 FROM public.tags t
                 WHERE t.parent_id = (SELECT id FROM tag_map WHERE name = '데이터베이스')
                 )
             INSERT INTO public.quiz_tags (quiz_id, tag_id)
                 SELECT
                     dbq.quiz_id,
                     dbs.tag_id
                 FROM db_quizzes dbq
                          CROSS JOIN LATERAL (
                     SELECT tag_id FROM db_subtags
                     WHERE random() < 0.3 -- 30% 확률로 각 하위 태그 선택
                     ORDER BY random()
                     LIMIT 2 -- 최대 2개의 하위 태그
                     ) AS dbs
                 ON CONFLICT DO NOTHING
     ),
-- 자바스크립트 문제 생성 (개수 증가)
     js_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '자바스크립트에서 변수를 선언하는 키워드가 아닌 것은?'
                     WHEN 1 THEN '다음 중 자바스크립트의 원시 타입(Primitive Type)이 아닌 것은?'
                     WHEN 2 THEN '자바스크립트에서 함수를 선언하는 올바른 방법은?'
                     WHEN 3 THEN '다음 코드의 실행 결과는? console.log(typeof [])'
                     WHEN 4 THEN 'ES6에서 추가된 기능이 아닌 것은?'
                     WHEN 5 THEN '자바스크립트에서 클로저(Closure)란 무엇인가?'
                     WHEN 6 THEN '다음 중 자바스크립트의 이벤트 버블링에 대한 설명으로 올바른 것은?'
                     WHEN 7 THEN '자바스크립트에서 Promise 객체의 상태가 아닌 것은?'
                     WHEN 8 THEN '다음 중 자바스크립트 배열 메서드 중 원본 배열을 변경하는 것은?'
                     WHEN 9 THEN '자바스크립트에서 호이스팅(Hoisting)이란?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'switch'
                     WHEN 1 THEN 'array'
                     WHEN 2 THEN 'function myFunc() {}'
                     WHEN 3 THEN 'object'
                     WHEN 4 THEN 'class'
                     WHEN 5 THEN '자신의 외부 함수 스코프에 접근할 수 있는 내부 함수'
                     WHEN 6 THEN '이벤트가 DOM 트리의 하위 요소에서 상위 요소로 전파되는 현상'
                     WHEN 7 THEN 'canceled'
                     WHEN 8 THEN 'push()'
                     WHEN 9 THEN '변수와 함수 선언이 코드 실행 전에 메모리에 할당되는 현상'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["var", "let", "const", "switch"]'::jsonb
                     WHEN 1 THEN '["string", "number", "boolean", "array"]'::jsonb
                     WHEN 2 THEN '["function myFunc() {}", "let myFunc = function() {}", "const myFunc = () => {}", "myFunc: function() {}"]'::jsonb
                     WHEN 3 THEN '["undefined", "object", "array", "reference"]'::jsonb
                     WHEN 4 THEN '["let/const", "화살표 함수", "클래스", "switch문"]'::jsonb
                     WHEN 5 THEN '["자신의 외부 함수 스코프에 접근할 수 있는 내부 함수", "전역 변수를 지역 변수로 변환하는 기능", "비동기 함수를 동기적으로 처리하는 방법", "브라우저의 메모리 관리 방법"]'::jsonb
                     WHEN 6 THEN '["이벤트가 DOM 트리의 하위 요소에서 상위 요소로 전파되는 현상", "이벤트가 DOM 트리의 상위 요소에서 하위 요소로 전파되는 현상", "이벤트가 발생한 순간 캡처되어 처리되는 방식", "이벤트가 중첩될 때 가장 마지막에 등록된 이벤트만 실행되는 현상"]'::jsonb
                     WHEN 7 THEN '["pending", "fulfilled", "rejected", "canceled"]'::jsonb
                     WHEN 8 THEN '["map()", "filter()", "reduce()", "push()"]'::jsonb
                     WHEN 9 THEN '["변수와 함수 선언이 코드 실행 전에 메모리에 할당되는 현상", "실행 컨텍스트가 콜 스택에 추가되는 과정", "변수가 선언된 스코프 외부에서 사용될 때 발생하는 오류", "비동기 코드가 실행 대기열에 추가되는 메커니즘"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'var, let, const는 변수 선언 키워드이지만, switch는 조건문을 작성할 때 사용하는 키워드입니다.'
                     WHEN 1 THEN '자바스크립트의 원시 타입은 string, number, boolean, null, undefined, symbol, bigint입니다. Array는 객체(Object) 타입입니다.'
                     WHEN 2 THEN '자바스크립트에서 함수를 선언하는 방법은 함수 선언식, 함수 표현식, 화살표 함수 등이 있습니다.'
                     WHEN 3 THEN '자바스크립트에서 배열(Array)의 typeof 결과는 "object"입니다. 배열은 특수한 형태의 객체입니다.'
                     WHEN 4 THEN 'ES6에서는 let/const, 화살표 함수, 클래스, 템플릿 리터럴, 구조 분해 할당 등이 추가되었습니다. switch문은 ES6 이전부터 존재했습니다.'
                     WHEN 5 THEN '클로저는 자신이 생성될 때의 환경(lexical environment)을 기억하는 함수로, 외부 함수의 변수에 접근할 수 있는 내부 함수를 의미합니다.'
                     WHEN 6 THEN '이벤트 버블링은 특정 요소에서 이벤트가 발생했을 때 해당 이벤트가 상위 요소로 전파되는 현상입니다. 반대로 이벤트 캡처링은 상위 요소에서 하위 요소로 전파됩니다.'
                     WHEN 7 THEN 'Promise 객체의 상태는 pending(대기), fulfilled(이행), rejected(거부) 세 가지입니다. canceled는 Promise 상태가 아닙니다.'
                     WHEN 8 THEN 'push(), pop(), shift(), unshift(), splice() 등은 원본 배열을 변경하는 메서드입니다. map(), filter(), reduce()는 새로운 배열을 반환합니다.'
                     WHEN 9 THEN '호이스팅은 자바스크립트 엔진이 코드를 실행하기 전에 변수와 함수 선언을 메모리에 올리는 과정입니다. 변수는 선언만 호이스팅되고 초기화는 호이스팅되지 않습니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 3 THEN
                         '// 다음 코드의 실행 결과를 생각해보세요
                         let arr = [];
                         console.log(typeof arr);'
                     WHEN mod(seq, 20) = 7 THEN
                         '// 다음 코드의 실행 결과는?
                         const promise = new Promise((resolve, reject) => {
                           setTimeout(() => resolve("완료"), 1000);
                         });

                         console.log(promise);

                         setTimeout(() => {
                           console.log(promise);
                         }, 2000);'
                     WHEN mod(seq, 20) = 13 THEN
                         '// 다음 코드의 결과는?
                         function outer() {
                           const x = 10;
                           function inner() {
                             console.log(x);
                           }
                           return inner;
                         }

                         const closureFunc = outer();
                         closureFunc();'
                     WHEN mod(seq, 20) = 17 THEN
                         '// 다음 코드의 출력은?
                         console.log(1);
                         setTimeout(() => console.log(2), 0);
                         Promise.resolve().then(() => console.log(3));
                         console.log(4);'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '자바스크립트'
             )
             LIMIT 500
     ),
-- 파이썬 문제 생성 (개수 증가)
     py_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '파이썬에서 리스트를 생성하는 올바른 방법은?'
                     WHEN 1 THEN '파이썬에서 딕셔너리를 순회하는 올바른 방법은?'
                     WHEN 2 THEN '파이썬의 리스트 컴프리헨션으로 올바르게 작성된 것은?'
                     WHEN 3 THEN '파이썬에서 문자열을 포맷팅하는 방법이 아닌 것은?'
                     WHEN 4 THEN '파이썬 함수 정의에서 "*args"의 의미는?'
                     WHEN 5 THEN '파이썬에서 클래스를 정의하는 올바른 문법은?'
                     WHEN 6 THEN '파이썬에서 예외를 처리하는 기본 구문은?'
                     WHEN 7 THEN '파이썬에서 모듈을 임포트하는 올바른 방법이 아닌 것은?'
                     WHEN 8 THEN '파이썬에서 제네레이터를 만드는 키워드는?'
                     WHEN 9 THEN '파이썬 3.x에서 문자열과 정수를 함께 출력하는 올바른 방법은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'my_list = [1, 2, 3]'
                     WHEN 1 THEN 'for key, value in my_dict.items():'
                     WHEN 2 THEN '[x ** 2 for x in range(10)]'
                     WHEN 3 THEN 'print(f"{} is good")'
                     WHEN 4 THEN '가변 개수의 위치 인자를 받는 매개변수'
                     WHEN 5 THEN 'class MyClass:'
                     WHEN 6 THEN 'try: ... except: ...'
                     WHEN 7 THEN 'import module as'
                     WHEN 8 THEN 'yield'
                     WHEN 9 THEN 'print(f"값: {num}")'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["my_list = [1, 2, 3]", "my_list = list(1, 2, 3)", "my_list = array(1, 2, 3)", "my_list = (1, 2, 3)"]'::jsonb
                     WHEN 1 THEN '["for key, value in my_dict.items():", "for key, value in my_dict:", "for item in my_dict.items():", "for key in my_dict:"]'::jsonb
                     WHEN 2 THEN '["[x ** 2 for x in range(10)]", "[for x in range(10): x ** 2]", "[x ** 2 in range(10)]", "[x for x ** 2 in range(10)]"]'::jsonb
                     WHEN 3 THEN E'["print(f\\"{name} is good\\")", "print(\\"{} is good\\".format(name))", "print(\\"%s is good\\" % name)", "print(f\\"{} is good\\")"]'::jsonb
                     WHEN 4 THEN '["가변 개수의 위치 인자를 받는 매개변수", "가변 개수의 키워드 인자를 받는 매개변수", "기본값이 있는 매개변수", "위치 인자만 받는 매개변수"]'::jsonb
                     WHEN 5 THEN '["class MyClass:", "Class MyClass {", "class MyClass()", "define class MyClass:"]'::jsonb
                     WHEN 6 THEN '["try: ... except: ...", "try: ... catch: ...", "try { ... } catch { ... }", "if error: ... else: ..."]'::jsonb
                     WHEN 7 THEN '["import module as", "from module import function", "import module", "from module import *"]'::jsonb
                     WHEN 8 THEN '["yield", "generate", "return", "await"]'::jsonb
                     WHEN 9 THEN '["print(f\"값: {num}\")", "print(\"값: \" + num)", "print(\"값: %d\" % num)", "print(\"값: \" + str(num))"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '파이썬에서 리스트는 대괄호([])를 사용하여 생성합니다. list()는 이터러블 객체에서 리스트를 생성하는 함수입니다.'
                     WHEN 1 THEN '딕셔너리의 키와 값을 함께 순회하려면 items() 메서드를 사용해야 합니다.'
                     WHEN 2 THEN '리스트 컴프리헨션은 [표현식 for 변수 in 이터러블] 형태로 작성합니다.'
                     WHEN 3 THEN 'f-문자열에서는 중괄호 안에 변수나 표현식이 있어야 합니다. 빈 중괄호는 올바른 형식이 아닙니다.'
                     WHEN 4 THEN '*args는 가변 개수의 위치 인자를 받기 위한 구문입니다. 키워드 인자는 **kwargs로 받습니다.'
                     WHEN 5 THEN '파이썬에서 클래스는 class 키워드를 사용하여 정의하고 콜론(:)으로 끝납니다.'
                     WHEN 6 THEN '파이썬에서 예외 처리는 try-except 구문을 사용합니다. 다른 언어에서 사용하는 try-catch와는 다릅니다.'
                     WHEN 7 THEN '올바른 임포트 구문은 "import module", "from module import function", "from module import *", "import module as alias"입니다.'
                     WHEN 8 THEN '파이썬에서 제네레이터 함수는 yield 키워드를 사용하여 값을 하나씩 반환합니다.'
                     WHEN 9 THEN 'f-문자열(f"값: {num}")은 파이썬 3.6 이상에서 변수를 문자열에 삽입하는 가장 깔끔한 방법입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 2 THEN
                         '# 다음 코드의 결과는?
                         squares = [x ** 2 for x in range(5)]
                         print(squares)'
                     WHEN mod(seq, 20) = 8 THEN
                         '# 다음 코드의 출력은?
                         def my_generator():
                             yield 1
                             yield 2
                             yield 3

                         gen = my_generator()
                         print(next(gen))
                         print(next(gen))'
                     WHEN mod(seq, 20) = 14 THEN
                         '# 다음 코드의 결과는?
                         class Person:
                             def __init__(self, name):
                                 self.name = name

                             def say_hello(self):
                                 return f"Hello, my name is {self.name}"

                         p = Person("Alice")
                         print(p.say_hello())'
                     WHEN mod(seq, 20) = 18 THEN
                         '# 다음 코드의 출력은?
                         try:
                             x = 1 / 0
                         except ZeroDivisionError:
                             print("Division by zero!")
                         else:
                             print("No error")
                         finally:
                             print("Cleanup")'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '파이썬'
             )
             LIMIT 500
     ),
-- 알고리즘, 자료구조 관련 문제 생성 (개수 증가)
     algo_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '시간 복잡도가 O(n log n)인 정렬 알고리즘은?'
                     WHEN 1 THEN '이진 검색 트리에서 삽입 연산의 시간 복잡도는?'
                     WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는?'
                     WHEN 3 THEN '그래프 탐색에 사용되지 않는 알고리즘은?'
                     WHEN 4 THEN '다음 중 그리디 알고리즘을 사용하는 문제는?'
                     WHEN 5 THEN '최단 경로 알고리즘 중 가중치가 음수인 간선이 있어도 사용할 수 있는 것은?'
                     WHEN 6 THEN '스택(Stack)의 LIFO 특성이 유용한 응용 분야가 아닌 것은?'
                     WHEN 7 THEN '동적 프로그래밍의 핵심 아이디어는?'
                     WHEN 8 THEN '균형 이진 탐색 트리가 아닌 것은?'
                     WHEN 9 THEN '다익스트라 알고리즘의 시간 복잡도(우선순위 큐 사용)는?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '퀵 정렬'
                     WHEN 1 THEN 'O(log n)'
                     WHEN 2 THEN 'O(n)'
                     WHEN 3 THEN '삽입 정렬'
                     WHEN 4 THEN '다익스트라 최단 경로'
                     WHEN 5 THEN '벨만-포드'
                     WHEN 6 THEN '너비 우선 탐색'
                     WHEN 7 THEN '문제를 작은 하위 문제로 나누고 중복 계산을 피하는 것'
                     WHEN 8 THEN '링크드 리스트'
                     WHEN 9 THEN 'O(E log V)'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["버블 정렬", "삽입 정렬", "퀵 정렬", "계수 정렬"]'::jsonb
                     WHEN 1 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
                     WHEN 2 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
                     WHEN 3 THEN '["깊이 우선 탐색", "너비 우선 탐색", "다익스트라", "삽입 정렬"]'::jsonb
                     WHEN 4 THEN '["최단 경로 찾기", "최소 신장 트리", "다익스트라 최단 경로", "0/1 배낭 문제"]'::jsonb
                     WHEN 5 THEN '["다익스트라", "프림", "벨만-포드", "크루스칼"]'::jsonb
                     WHEN 6 THEN '["함수 호출 관리", "괄호 검사", "역순 문자열 출력", "너비 우선 탐색"]'::jsonb
                     WHEN 7 THEN '["문제를 작은 하위 문제로 나누고 중복 계산을 피하는 것", "항상 현재 최적의 선택을 하는 것", "가능한 모든 경우의 수를 탐색하는 것", "휴리스틱 함수를 사용하여 최적 해를 추정하는 것"]'::jsonb
                     WHEN 8 THEN '["AVL 트리", "레드-블랙 트리", "B-트리", "링크드 리스트"]'::jsonb
                     WHEN 9 THEN '["O(V + E)", "O(V²)", "O(E log V)", "O(V * E)"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '퀵 정렬, 병합 정렬, 힙 정렬은 평균적으로 O(n log n)의 시간 복잡도를 가집니다. 버블 정렬과 삽입 정렬은 O(n²)입니다.'
                     WHEN 1 THEN '이진 검색 트리에서 삽입은 트리의 높이에 비례하며, 균형 잡힌 트리의 경우 O(log n)입니다.'
                     WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는 충돌이 많이 발생할 경우 O(n)입니다.'
                     WHEN 3 THEN '삽입 정렬은 정렬 알고리즘이며, 그래프 탐색 알고리즘이 아닙니다. DFS, BFS, 다익스트라는 그래프 탐색 알고리즘입니다.'
                     WHEN 4 THEN '다익스트라 알고리즘은 그리디 알고리즘의 대표적인 예입니다. 항상 현재 가장 최적인 경로를 선택합니다.'
                     WHEN 5 THEN '벨만-포드 알고리즘은 음수 가중치가 있는 그래프에서도 최단 경로를 찾을 수 있습니다. 다익스트라는 음수 가중치에 대해 정확한 결과를 보장하지 않습니다.'
                     WHEN 6 THEN '너비 우선 탐색(BFS)은 큐(Queue)를 사용하여 구현합니다. 스택은 깊이 우선 탐색(DFS)에 사용됩니다.'
                     WHEN 7 THEN '동적 프로그래밍은 문제를 작은 하위 문제로 나누고, 중복 계산을 피하기 위해 이미 계산된 결과를 저장하여 재활용하는 기법입니다.'
                     WHEN 8 THEN '링크드 리스트는 트리 구조가 아닙니다. AVL 트리, 레드-블랙 트리, B-트리는 모두 균형 이진 탐색 트리의 종류입니다.'
                     WHEN 9 THEN '우선순위 큐를 사용한 다익스트라 알고리즘의 시간 복잡도는 O(E log V)입니다. E는 간선의 수, V는 정점의 수입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 4 THEN
                         '// 다음 알고리즘의 시간 복잡도는?
                         function search(arr, target) {
                             let left = 0;
                             let right = arr.length - 1;

                             while (left <= right) {
                                 let mid = Math.floor((left + right) / 2);
                                 if (arr[mid] === target) return mid;
                                 if (arr[mid] < target) left = mid + 1;
                                 else right = mid - 1;
                             }
                             return -1;
                         }'
                     WHEN mod(seq, 20) = 9 THEN
                         '// 다음 코드의 시간 복잡도는?
                         function fibonacci(n) {
                             if (n <= 1) return n;
                             return fibonacci(n - 1) + fibonacci(n - 2);
                         }'
                     WHEN mod(seq, 20) = 14 THEN
                         '// 다음 정렬 알고리즘을 식별하세요
                         function sort(arr) {
                             const n = arr.length;
                             for (let i = 0; i < n - 1; i++) {
                                 for (let j = 0; j < n - i - 1; j++) {
                                     if (arr[j] > arr[j + 1]) {
                                         [arr[j], arr[j + 1]] = [arr[j + 1], arr[j]];
                                     }
                                 }
                             }
                             return arr;
                         }'
                     WHEN mod(seq, 20) = 19 THEN
                         '// 다음 알고리즘을 식별하세요
                         function(graph, start) {
                             const distances = {};
                             const visited = {};
                             const nodes = Object.keys(graph);

                             nodes.forEach(node => {
                                 distances[node] = Infinity;
                                 visited[node] = false;
                             });

                             distances[start] = 0;

                             for (let i = 0; i < nodes.length; i++) {
                                 const closest = findClosestNode(distances, visited);
                                 visited[closest] = true;

                                 Object.keys(graph[closest]).forEach(neighbor => {
                                     const newDistance = distances[closest] + graph[closest][neighbor];
                                     if (newDistance < distances[neighbor]) {
                                         distances[neighbor] = newDistance;
                                     }
                                 });
                             }

                             return distances;
                         }'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND (tm.name = '알고리즘' OR tm.name = '자료구조')
             )
             LIMIT 500
     ),
-- 네트워크 관련 문제 생성 (개수 증가)
     network_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'HTTP 상태 코드 404는 무엇을 의미하는가?'
                     WHEN 1 THEN 'TCP/IP 모델의 계층 순서가 올바른 것은?'
                     WHEN 2 THEN '다음 중 라우팅 프로토콜이 아닌 것은?'
                     WHEN 3 THEN 'IPv4 주소의 클래스 A의 첫 번째 옥텟 범위는?'
                     WHEN 4 THEN 'DNS의 주요 기능은?'
                     WHEN 5 THEN 'HTTP와 HTTPS의 주요 차이점은?'
                     WHEN 6 THEN 'TCP와 UDP의 차이점이 아닌 것은?'
                     WHEN 7 THEN '서브넷 마스크의 역할은?'
                     WHEN 8 THEN 'OSI 7계층 모델에서 물리 계층의 역할은?'
                     WHEN 9 THEN 'WebSocket과 HTTP의 주요 차이점은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '리소스를 찾을 수 없음'
                     WHEN 1 THEN '링크 계층 - 인터넷 계층 - 전송 계층 - 응용 계층'
                     WHEN 2 THEN 'SMTP'
                     WHEN 3 THEN '0-127'
                     WHEN 4 THEN '도메인 이름을 IP 주소로 변환'
                     WHEN 5 THEN 'HTTPS는 암호화를 사용함'
                     WHEN 6 THEN 'TCP가 UDP보다 빠르다'
                     WHEN 7 THEN '네트워크와 호스트 부분을 구분'
                     WHEN 8 THEN '데이터를 전기 신호로 변환하여 전송'
                     WHEN 9 THEN 'WebSocket은 양방향 통신을 지원'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["리소스를 찾을 수 없음", "권한 없음", "서버 내부 오류", "요청 성공"]'::jsonb
                     WHEN 1 THEN '["링크 계층 - 인터넷 계층 - 전송 계층 - 응용 계층", "응용 계층 - 전송 계층 - 인터넷 계층 - 링크 계층", "전송 계층 - 인터넷 계층 - 링크 계층 - 응용 계층", "인터넷 계층 - 링크 계층 - 전송 계층 - 응용 계층"]'::jsonb
                     WHEN 2 THEN '["OSPF", "RIP", "BGP", "SMTP"]'::jsonb
                     WHEN 3 THEN '["0-127", "128-191", "192-223", "224-239"]'::jsonb
                     WHEN 4 THEN '["도메인 이름을 IP 주소로 변환", "데이터 패킷을 암호화", "네트워크 트래픽을 제어", "방화벽 규칙을 관리"]'::jsonb
                     WHEN 5 THEN '["HTTPS는 암호화를 사용함", "HTTP는 더 안정적임", "HTTPS는 더 빠름", "HTTP는 더 많은 기능을 제공함"]'::jsonb
                     WHEN 6 THEN '["TCP는 연결 지향적이다", "UDP는 비연결 지향적이다", "TCP가 UDP보다 빠르다", "TCP는 데이터 전달을 보장한다"]'::jsonb
                     WHEN 7 THEN '["네트워크와 호스트 부분을 구분", "IP 주소를 도메인 이름으로 변환", "라우터 간 통신 설정", "데이터 패킷 암호화"]'::jsonb
                     WHEN 8 THEN '["데이터를 전기 신호로 변환하여 전송", "데이터 패킷의 경로 결정", "세션 관리", "데이터 암호화"]'::jsonb
                     WHEN 9 THEN '["WebSocket은 양방향 통신을 지원", "WebSocket은 더 안전함", "HTTP는 더 빠름", "WebSocket은 더 오래된 프로토콜"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '404 상태 코드는 요청한 리소스를 서버에서 찾을 수 없다는 것을 의미합니다.'
                     WHEN 1 THEN 'TCP/IP 모델의 계층 순서는 링크(물리+데이터 링크) - 인터넷(네트워크) - 전송 - 응용 계층입니다.'
                     WHEN 2 THEN 'SMTP는 이메일 전송 프로토콜이며, 라우팅 프로토콜이 아닙니다. OSPF, RIP, BGP는 라우팅 프로토콜입니다.'
                     WHEN 3 THEN 'IPv4 클래스 A는 첫 비트가 0으로 시작하며, 첫 번째 옥텟 범위는 0-127입니다.'
                     WHEN 4 THEN 'DNS(Domain Name System)의 주요 기능은 사람이 읽을 수 있는 도메인 이름을 IP 주소로 변환하는 것입니다.'
                     WHEN 5 THEN 'HTTPS는 HTTP에 SSL/TLS 암호화 계층을 추가한 것으로, 데이터가 암호화되어 전송됩니다.'
                     WHEN 6 THEN 'TCP는 연결 지향적이고 신뢰성 있는 전송을 보장하지만, UDP보다 오버헤드가 더 커서 일반적으로 더 느립니다.'
                     WHEN 7 THEN '서브넷 마스크는 IP 주소에서 네트워크 부분과 호스트 부분을 구분하는 역할을 합니다.'
                     WHEN 8 THEN '물리 계층은 OSI 모델의 가장 하위 계층으로, 데이터를 전기 신호, 광신호 등으로 변환하여 전송 매체를 통해 전송하는 역할을 담당합니다.'
                     WHEN 9 THEN 'WebSocket은 HTTP와 달리 연결이 수립된 후 양방향 통신이 가능하며, 실시간 데이터 전송에 효율적입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 NULL
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '네트워크'
             )
             LIMIT 300
     ),
-- 웹개발 관련 문제 생성 (개수 증가)
     web_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'REST API에서 PATCH 메서드의 용도는?'
                     WHEN 1 THEN 'CORS란 무엇인가?'
                     WHEN 2 THEN '다음 중 프론트엔드 프레임워크가 아닌 것은?'
                     WHEN 3 THEN 'CSS 선택자 우선순위가 가장 높은 것은?'
                     WHEN 4 THEN 'localStorage와 sessionStorage의 차이점은?'
                     WHEN 5 THEN 'HTTP 쿠키의 주요 용도는?'
                     WHEN 6 THEN 'SPA(Single Page Application)의 특징이 아닌 것은?'
                     WHEN 7 THEN 'CSS 박스 모델의 구성 요소가 바깥쪽에서 안쪽 순서로 올바르게 나열된 것은?'
                     WHEN 8 THEN 'Webpack의 주요 기능은?'
                     WHEN 9 THEN 'MVC 아키텍처에서 Controller의 역할은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '리소스 부분 업데이트'
                     WHEN 1 THEN '교차 출처 리소스 공유'
                     WHEN 2 THEN 'Django'
                     WHEN 3 THEN '인라인 스타일'
                     WHEN 4 THEN 'sessionStorage는 브라우저 세션이 끝나면 데이터가 삭제됨'
                     WHEN 5 THEN '사용자 세션 관리'
                     WHEN 6 THEN '모든 페이지 요청마다 전체 HTML을 다시 로드'
                     WHEN 7 THEN '마진, 테두리, 패딩, 콘텐츠'
                     WHEN 8 THEN '모듈 번들링'
                     WHEN 9 THEN '모델과 뷰 사이의 중개자 역할'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["리소스 부분 업데이트", "리소스 전체 업데이트", "리소스 생성", "리소스 삭제"]'::jsonb
                     WHEN 1 THEN '["교차 출처 리소스 공유", "콘텐츠 전송 네트워크", "캐스케이딩 스타일 시트", "클라이언트 측 렌더링"]'::jsonb
                     WHEN 2 THEN '["React", "Angular", "Vue", "Django"]'::jsonb
                     WHEN 3 THEN '["인라인 스타일", "ID 선택자", "클래스 선택자", "태그 선택자"]'::jsonb
                     WHEN 4 THEN '["서로 다른 도메인 간 데이터 공유", "데이터 암호화 방식의 차이", "sessionStorage는 브라우저 세션이 끝나면 데이터가 삭제됨", "localStorage는 용량 제한이 더 큼"]'::jsonb
                     WHEN 5 THEN '["사용자 세션 관리", "서버 성능 모니터링", "네트워크 대역폭 제한", "데이터베이스 연결 관리"]'::jsonb
                     WHEN 6 THEN '["JavaScript를 사용하여 동적으로 콘텐츠 업데이트", "페이지 전환 시 부분적인 데이터만 로드", "클라이언트 측 라우팅 사용", "모든 페이지 요청마다 전체 HTML을 다시 로드"]'::jsonb
                     WHEN 7 THEN '["마진, 테두리, 패딩, 콘텐츠", "콘텐츠, 패딩, 테두리, 마진", "테두리, 마진, 패딩, 콘텐츠", "패딩, 콘텐츠, 테두리, 마진"]'::jsonb
                     WHEN 8 THEN '["모듈 번들링", "코드 실행", "데이터베이스 관리", "서버 배포"]'::jsonb
                     WHEN 9 THEN '["모델과 뷰 사이의 중개자 역할", "데이터베이스와 직접 상호작용", "사용자 인터페이스 렌더링", "비즈니스 로직 및 데이터 저장"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'PATCH 메서드는 리소스의 일부분만 업데이트하는 데 사용됩니다. PUT은 리소스 전체를 업데이트합니다.'
                     WHEN 1 THEN 'CORS(Cross-Origin Resource Sharing)는 다른 출처의 리소스에 대한 접근을 제어하는 HTTP 헤더 기반 메커니즘입니다.'
                     WHEN 2 THEN 'Django는 Python 기반의 백엔드 프레임워크입니다. React, Angular, Vue는 프론트엔드 프레임워크입니다.'
                     WHEN 3 THEN 'CSS 선택자 우선순위는 인라인 스타일 > ID 선택자 > 클래스 선택자 > 태그 선택자 순입니다.'
                     WHEN 4 THEN 'localStorage와 sessionStorage는 모두 클라이언트 측 데이터 저장소이지만, sessionStorage는 브라우저 세션이 종료되면 데이터가 삭제되는 반면, localStorage는 명시적으로 삭제할 때까지 데이터가 유지됩니다.'
                     WHEN 5 THEN 'HTTP 쿠키는 주로 세션 관리, 사용자 로그인 상태 유지, 사용자 선호도 추적 등에 사용됩니다.'
                     WHEN 6 THEN 'SPA는 초기 로드 후 페이지 전환 시 전체 HTML을 다시 로드하지 않고, 필요한 데이터만 비동기적으로 가져와 현재 페이지를 업데이트합니다.'
                     WHEN 7 THEN 'CSS 박스 모델은 바깥쪽에서 안쪽으로 마진(margin), 테두리(border), 패딩(padding), 콘텐츠(content)로 구성됩니다.'
                     WHEN 8 THEN 'Webpack은 주로 JavaScript 모듈을 번들링(bundling)하고, 자산(asset)을 관리하는 도구입니다.'
                     WHEN 9 THEN 'MVC 아키텍처에서 Controller는 Model과 View 사이의 중개자 역할을 하며, 사용자 입력을 처리하고 Model을 업데이트합니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 6 THEN
                         '// 다음 코드의 결과는?
                         const promise1 = Promise.resolve(3);
                         const promise2 = new Promise((resolve, reject) => {
                           setTimeout(() => resolve("foo"), 100);
                         });

                         Promise.all([promise1, promise2]).then(values => {
                           console.log(values);
                         });'
                     WHEN mod(seq, 20) = 11 THEN
                         '/* 다음 CSS의 결과는?
                         .box {
                           width: 100px;
                           height: 100px;
                           padding: 20px;
                           border: 5px solid black;
                           box-sizing: border-box;
                         }

                         div 요소의 실제 너비는? */
                         '
                     WHEN mod(seq, 20) = 16 THEN
                         '// 다음 React 컴포넌트의 문제점은?
                         function UserProfile({ user }) {
                           const [data, setData] = useState({});

                           useEffect(() => {
                             setData(user);
                           });

                           return (
                             <div>
                               <h1>{data.name}</h1>
                               <p>{data.email}</p>
                             </div>
                           );
                         }'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND (tm.name = '웹개발' OR tm.name = '프론트엔드' OR tm.name = '백엔드')
             )
             LIMIT 500
     ),
-- 운영체제 관련 문제 생성 (개수 증가)
     os_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '프로세스와 스레드의 주요 차이점은?'
                     WHEN 1 THEN '페이지 교체 알고리즘이 아닌 것은?'
                     WHEN 2 THEN '교착 상태(Deadlock)의 필요 조건이 아닌 것은?'
                     WHEN 3 THEN '선점형 스케줄링 알고리즘이 아닌 것은?'
                     WHEN 4 THEN '가상 메모리의 주요 목적은?'
                     WHEN 5 THEN '세마포어(Semaphore)와 뮤텍스(Mutex)의 차이점은?'
                     WHEN 6 THEN '다음 중 운영체제의 주요 기능이 아닌 것은?'
                     WHEN 7 THEN '인터럽트(Interrupt)의 목적은?'
                     WHEN 8 THEN '파일 시스템의 주요 역할은?'
                     WHEN 9 THEN '컨텍스트 스위칭(Context Switching)이란?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '스레드는 자원을 공유하지만 프로세스는 독립적이다'
                     WHEN 1 THEN 'Round Robin'
                     WHEN 2 THEN '선점 가능'
                     WHEN 3 THEN 'FCFS'
                     WHEN 4 THEN '물리적 메모리보다 큰 프로그램 실행 허용'
                     WHEN 5 THEN '세마포어는 여러 스레드가 접근할 수 있지만 뮤텍스는 하나의 스레드만 가능'
                     WHEN 6 THEN '데이터베이스 쿼리 최적화'
                     WHEN 7 THEN 'CPU에게 중요한 이벤트 발생을 알리는 것'
                     WHEN 8 THEN '데이터를 파일로 구성하여 저장하고 관리'
                     WHEN 9 THEN '실행 중인 프로세스나 스레드를 변경하는 과정'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["스레드는 자원을 공유하지만 프로세스는 독립적이다", "프로세스는 경량이고 스레드는 중량이다", "스레드는 컨텍스트 스위칭 비용이 더 높다", "프로세스는 하나의 스레드만 가질 수 있다"]'::jsonb
                     WHEN 1 THEN '["LRU", "FIFO", "Round Robin", "Optimal"]'::jsonb
                     WHEN 2 THEN '["상호 배제", "점유와 대기", "비선점", "선점 가능"]'::jsonb
                     WHEN 3 THEN '["FCFS", "SJF(선점형)", "Round Robin", "Priority Scheduling(선점형)"]'::jsonb
                     WHEN 4 THEN '["물리적 메모리보다 큰 프로그램 실행 허용", "디스크 입출력 시간 단축", "CPU 사용률 감소", "데이터 보안 향상"]'::jsonb
                     WHEN 5 THEN '["세마포어는 여러 스레드가 접근할 수 있지만 뮤텍스는 하나의 스레드만 가능", "뮤텍스는 이진 세마포어보다 빠르다", "세마포어는 시스템 범위에서 작동하지 않는다", "뮤텍스는 프로세스 간 동기화에 사용할 수 없다"]'::jsonb
                     WHEN 6 THEN '["메모리 관리", "프로세스 관리", "데이터베이스 쿼리 최적화", "장치 관리"]'::jsonb
                     WHEN 7 THEN '["CPU에게 중요한 이벤트 발생을 알리는 것", "메모리 사용량 최적화", "프로세스 생성 요청", "사용자 입력 처리"]'::jsonb
                     WHEN 8 THEN '["데이터를 파일로 구성하여 저장하고 관리", "네트워크 패킷 라우팅", "메모리 할당 및 해제", "CPU 스케줄링"]'::jsonb
                     WHEN 9 THEN '["실행 중인 프로세스나 스레드를 변경하는 과정", "메모리 주소 변환 과정", "파일 시스템 내의 데이터 이동", "인터럽트 처리 방식"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '프로세스는 독립적인 메모리 공간과 자원을 가지지만, 스레드는 프로세스 내에서 메모리와 자원을 공유합니다. 스레드는 "경량 프로세스"라고도 불립니다.'
                     WHEN 1 THEN 'Round Robin은 페이지 교체 알고리즘이 아니라 CPU 스케줄링 알고리즘입니다. LRU, FIFO, Optimal은 페이지 교체 알고리즘입니다.'
                     WHEN 2 THEN '교착 상태의 4가지 필요 조건은 상호 배제, 점유와 대기, 비선점, 순환 대기입니다. "선점 가능"은 오히려 교착 상태를 방지하는 조건입니다.'
                     WHEN 3 THEN 'FCFS(First-Come, First-Served)는 비선점형 스케줄링 알고리즘입니다. 나머지는 모두 선점형 스케줄링 알고리즘입니다.'
                     WHEN 4 THEN '가상 메모리의 주요 목적은 물리적 메모리 크기보다 큰 프로그램을 실행할 수 있게 하는 것입니다. 필요한 부분만 물리 메모리에 로드하고 나머지는 디스크에 유지합니다.'
                     WHEN 5 THEN '세마포어는 여러 프로세스/스레드가 공유 자원에 접근할 수 있는 숫자를 가진 신호 메커니즘이고, 뮤텍스는 하나의 프로세스/스레드만 자원에 접근할 수 있게 하는 잠금 메커니즘입니다.'
                     WHEN 6 THEN '운영체제의 주요 기능은 프로세스 관리, 메모리 관리, 파일 시스템 관리, 장치 관리 등입니다. 데이터베이스 쿼리 최적화는 DBMS의 역할입니다.'
                     WHEN 7 THEN '인터럽트는 CPU에게 중요한 이벤트(I/O 완료, 타이머 만료, 오류 발생 등)를 알리고 처리를 요청하는 메커니즘입니다.'
                     WHEN 8 THEN '파일 시스템은 데이터를 체계적으로 파일로 구성하여 저장, 조직, 검색, 관리하는 방법을 제공합니다.'
                     WHEN 9 THEN '컨텍스트 스위칭은 현재 실행 중인 프로세스나 스레드의 상태를 저장하고 다른 프로세스나 스레드를 실행하기 위해 상태를 복원하는 과정입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 5 THEN
                         '// 다음 코드는 무엇을 보여주는가?
                         #include <stdio.h>
                         #include <pthread.h>

                         pthread_mutex_t mutex;

                         void* thread_function(void* arg) {
                             pthread_mutex_lock(&mutex);
                             // 임계 영역 (Critical Section)
                             pthread_mutex_unlock(&mutex);
                             return NULL;
                         }'
                     WHEN mod(seq, 20) = 15 THEN
                         '// 다음 코드의 문제점은?
                         void process1() {
                             acquire(resource1);
                             acquire(resource2);
                             // 작업 수행
                             release(resource2);
                             release(resource1);
                         }

                         void process2() {
                             acquire(resource2);
                             acquire(resource1);
                             // 작업 수행
                             release(resource1);
                             release(resource2);
                         }'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '운영체제'
             )
             LIMIT 300
     ),
-- 데이터베이스 관련 문제 생성 (개수 증가)
     db_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'SQL에서 DELETE와 TRUNCATE의 차이점은?'
                     WHEN 1 THEN '정규화의 주요 목적은?'
                     WHEN 2 THEN 'ACID 속성에 포함되지 않는 것은?'
                     WHEN 3 THEN '다음 중 비관계형 데이터베이스는?'
                     WHEN 4 THEN 'GROUP BY 절과 함께 사용할 수 없는 집계 함수는?'
                     WHEN 5 THEN '데이터베이스 인덱스의 주요 목적은?'
                     WHEN 6 THEN '다음 중 SQL 조인 유형이 아닌 것은?'
                     WHEN 7 THEN 'SQL에서 트랜잭션을 종료하는 명령어는?'
                     WHEN 8 THEN '다음 중 관계형 데이터베이스 시스템이 아닌 것은?'
                     WHEN 9 THEN 'SQL 서브쿼리의 실행 순서는?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'TRUNCATE는 롤백이 불가능하다'
                     WHEN 1 THEN '데이터 중복 최소화'
                     WHEN 2 THEN '확장성'
                     WHEN 3 THEN 'MongoDB'
                     WHEN 4 THEN 'TOP'
                     WHEN 5 THEN '검색 속도 향상'
                     WHEN 6 THEN 'COMBINE JOIN'
                     WHEN 7 THEN 'COMMIT'
                     WHEN 8 THEN 'Redis'
                     WHEN 9 THEN '내부 서브쿼리부터 외부 쿼리 순으로 실행'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["DELETE는 조건절을 사용할 수 없다", "TRUNCATE는 인덱스를 삭제한다", "DELETE는 트리거를 발생시킨다", "TRUNCATE는 롤백이 불가능하다"]'::jsonb
                     WHEN 1 THEN '["데이터 중복 최소화", "쿼리 성능 향상", "데이터베이스 크기 증가", "외래 키 제약 완화"]'::jsonb
                     WHEN 2 THEN '["원자성", "일관성", "격리성", "확장성"]'::jsonb
                     WHEN 3 THEN '["MySQL", "Oracle", "PostgreSQL", "MongoDB"]'::jsonb
                     WHEN 4 THEN '["COUNT", "AVG", "TOP", "SUM"]'::jsonb
                     WHEN 5 THEN '["검색 속도 향상", "데이터 무결성 보장", "트랜잭션 관리", "데이터 압축"]'::jsonb
                     WHEN 6 THEN '["INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "COMBINE JOIN"]'::jsonb
                     WHEN 7 THEN '["FINISH", "COMMIT", "SAVE", "END"]'::jsonb
                     WHEN 8 THEN '["MySQL", "SQLite", "PostgreSQL", "Redis"]'::jsonb
                     WHEN 9 THEN '["외부 쿼리부터 내부 서브쿼리 순으로 실행", "내부 서브쿼리부터 외부 쿼리 순으로 실행", "모든 쿼리가 동시에 실행", "쿼리 최적화기가 결정한 순서대로 실행"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'DELETE는 트랜잭션 로그에 각 행의 삭제를 기록하여 롤백이 가능하지만, TRUNCATE는 테이블의 데이터 페이지를 할당 해제하는 방식으로 빠르게 모든 데이터를 제거하며 로그를 거의 기록하지 않아 롤백이 불가능합니다.'
                     WHEN 1 THEN '정규화의 주요 목적은 데이터 중복을 최소화하여 데이터 무결성을 유지하고 저장 공간을 효율적으로 사용하는 것입니다.'
                     WHEN 2 THEN 'ACID 속성은 원자성(Atomicity), 일관성(Consistency), 격리성(Isolation), 지속성(Durability)입니다. 확장성(Scalability)은 ACID 속성에 포함되지 않습니다.'
                     WHEN 3 THEN 'MongoDB는 문서 기반 NoSQL 데이터베이스입니다. MySQL, Oracle, PostgreSQL은 모두 관계형 데이터베이스입니다.'
                     WHEN 4 THEN 'TOP은 SQL Server에서 결과 집합의 행 수를 제한하는 키워드로, 집계 함수가 아닙니다. COUNT, AVG, SUM은 집계 함수입니다.'
                     WHEN 5 THEN '데이터베이스 인덱스는 검색 쿼리의 성능을 향상시키기 위해 사용됩니다. 테이블의 열에 대한 포인터를 생성하여 데이터를 빠르게 찾을 수 있게 합니다.'
                     WHEN 6 THEN 'SQL의 주요 조인 유형은 INNER JOIN, LEFT JOIN, RIGHT JOIN, FULL JOIN, CROSS JOIN입니다. COMBINE JOIN은 존재하지 않습니다.'
                     WHEN 7 THEN 'SQL에서 트랜잭션을 종료하고 변경사항을 저장하려면 COMMIT 명령어를 사용합니다. 취소하려면 ROLLBACK을 사용합니다.'
                     WHEN 8 THEN 'Redis는 키-값 저장소 형태의 NoSQL 데이터베이스입니다. MySQL, SQLite, PostgreSQL은 모두 관계형 데이터베이스 시스템입니다.'
                     WHEN 9 THEN 'SQL 서브쿼리는 일반적으로 내부(중첩된) 서브쿼리부터 외부 쿼리 순으로 실행됩니다. 단, 쿼리 최적화기가 실행 계획을 변경할 수 있습니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 8 THEN
                         '-- 다음 SQL 쿼리의 결과는?
                         SELECT department, COUNT(*) as emp_count
                         FROM employees
                         WHERE salary > 50000
                         GROUP BY department
                         HAVING COUNT(*) > 5
                         ORDER BY emp_count DESC;'
                     WHEN mod(seq, 20) = 12 THEN
                         '-- 다음 두 테이블에 대한 쿼리의 결과는?
                         -- Table: orders
                         -- id | customer_id | order_date | total_amount
                         -- ----------------------------------
                         -- 1  | 101         | 2023-01-01 | 150.00
                         -- 2  | 102         | 2023-01-02 | 200.00
                         -- 3  | 101         | 2023-01-03 | 100.00

                         -- Table: customers
                         -- id | name    | email
                         -- -----------------------
                         -- 101 | Alice   | alice@example.com
                         -- 102 | Bob     | bob@example.com
                         -- 103 | Charlie | charlie@example.com

                         SELECT c.name, SUM(o.total_amount) as total_spent
                         FROM customers c
                         LEFT JOIN orders o ON c.id = o.customer_id
                         GROUP BY c.id, c.name
                         ORDER BY total_spent DESC;'
                     WHEN mod(seq, 20) = 16 THEN
                         '-- 다음 테이블과 쿼리의 문제점은?
                         CREATE TABLE users (
                           id INT PRIMARY KEY,
                           username VARCHAR(50),
                           email VARCHAR(100)
                         );

                         SELECT * FROM users WHERE username LIKE "%John%";'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '데이터베이스'
             )
             LIMIT 300
     ),
     -- Java 관련 태그 생성 (존재하지 않는 경우에만)
     java_tag_insert AS (
         INSERT INTO public.tags (created_at, name, description)
             SELECT
                 NOW(),
                 'Java',
                 'Java 프로그래밍 언어, 프레임워크, 개념 및 모범 사례'
             WHERE NOT EXISTS (
                 SELECT 1 FROM public.tags WHERE name = 'Java'
             )
             RETURNING id, name
     ),
-- Java 태그 ID 가져오기 (새로 생성되었거나 이미 존재하는 경우)
     java_tag AS (
         SELECT id FROM java_tag_insert
         UNION ALL
         SELECT id FROM public.tags WHERE name = 'Java' AND NOT EXISTS (SELECT 1 FROM java_tag_insert)
     ),
-- 백엔드 태그 ID 가져오기
     backend_tag AS (
         SELECT id FROM public.tags WHERE name = '백엔드'
     ),

-- Java 하위 태그 생성
     java_subtags AS (
         INSERT INTO public.tags (created_at, name, description, parent_id)
             SELECT
                 NOW(),
                 subtag.name,
                 subtag.description,
                 (SELECT id FROM java_tag)
             FROM (
                      VALUES
                          ('Java 기초', 'Java 언어 기본 문법과 개념'),
                          ('Java 객체지향', 'Java의 객체지향 프로그래밍 개념'),
                          ('Java 컬렉션', 'Java 컬렉션 프레임워크와 자료구조'),
                          ('Java 스레드', '멀티스레딩과 동시성 프로그래밍'),
                          ('Java 스프링', 'Spring 프레임워크 기초와 응용'),
                          ('Java JVM', 'JVM 구조와 메모리 관리'),
                          ('Java 람다', '람다 표현식과 함수형 프로그래밍'),
                          ('Java 디자인패턴', 'Java 디자인 패턴 구현 및 응용')
                  ) as subtag(name, description)
             ON CONFLICT DO NOTHING
             RETURNING id, name
     ),
-- Java 태그 동의어 추가
     java_synonyms AS (
         INSERT INTO public.tag_synonyms (tag_id, synonym)
             SELECT (SELECT id FROM java_tag), synonym
             FROM (
                      VALUES ('자바'), ('JAVA'), ('java')
                  ) as s(synonym)
             ON CONFLICT DO NOTHING
     ),
-- Java 면접 퀴즈 생성
     java_quizzes AS (
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
             FROM generate_series(1, 2) AS seq

             UNION ALL

             -- 스프링 프레임워크 면접 퀴즈
             SELECT
                 (NOW() - (random() * INTERVAL '60 days')),
                 (NOW() - (random() * INTERVAL '30 days')),
                 '스프링 프레임워크 면접 질문 Vol.' || seq,
                 '스프링 프레임워크에 관한 면접 질문과 실전 문제들을 다룹니다.',
                 CASE
                     WHEN seq = 1 THEN 'BEGINNER'
                     WHEN seq = 2 THEN 'INTERMEDIATE'
                     ELSE 'ADVANCED'
                     END,
                 true,
                 10, -- 각 퀴즈는 10개 문제
                 'TOPIC_BASED',
                 40, -- 40분 제한시간
                 (SELECT id FROM admin_user),
                 floor(random() * 70 + 25), -- 25-95 시도 횟수
                 random() * 20 + 65, -- 65-85 평균 점수
                 floor(random() * 450 + 180), -- 180-630 조회수
                 NULL -- 만료일 없음
             FROM generate_series(1, 3) AS seq

             RETURNING id, quiz_type, difficulty_level, creator_id, question_count, title
     ),
-- 퀴즈와 태그 연결
     java_quiz_tags AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT q.id, (SELECT id FROM java_tag)
             FROM java_quizzes q

             UNION ALL

             -- 스프링 관련 퀴즈에는 백엔드 태그도 추가
             SELECT q.id, (SELECT id FROM backend_tag)
             FROM java_quizzes q
             WHERE q.title LIKE '%스프링%'
     ),
-- 자바 기초 면접 질문 (초급)
     java_basic_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             WITH quiz_ids AS (
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'BEGINNER' AND title LIKE '%기초%'
             )
             SELECT
                 NOW() - (random() * INTERVAL '60 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 questions.question,
                 'MULTIPLE_CHOICE',
                 'BEGINNER',
                 questions.answer,
                 questions.options,
                 questions.explanation,
                 5, -- 초급 문제 5점
                 45, -- 45초 제한시간
                 (SELECT id FROM quiz_ids ORDER BY random() LIMIT 1),
                 questions.code
             FROM (
                      VALUES
                          ('JVM이란 무엇이며 어떤 역할을 하는가?',
                           'Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다',
                           '["Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다", "Java Variable Method로, 변수와 메소드를 관리하는 시스템이다", "Java Visual Monitor로, 화면 출력을 담당하는 시스템이다", "Java Version Manager로, 자바 버전을 관리하는 도구이다"]'::jsonb,
                           'JVM(Java Virtual Machine)은 자바 바이트코드(.class 파일)를 각 운영체제에 맞게 해석하고 실행하는 가상 머신입니다. 덕분에 Java는 "Write Once, Run Anywhere"라는 특징을 갖습니다.',
                           NULL),

                          ('자바의 기본 데이터 타입(Primitive Type)이 아닌 것은?',
                           'String',
                           '["int", "char", "boolean", "String"]'::jsonb,
                           'String은 기본 데이터 타입이 아닌 참조 타입(Reference Type)입니다. 자바의 기본 데이터 타입은 byte, short, int, long, float, double, char, boolean의 8가지입니다.',
                           NULL),

                          ('다음 중 자바의 객체지향 특성이 아닌 것은?',
                           '포인터 조작',
                           '["상속", "캡슐화", "다형성", "포인터 조작"]'::jsonb,
                           '자바는 포인터를 직접 조작할 수 없으며, 메모리 관리를 자동으로 처리합니다. 자바의 주요 객체지향 특성은 상속, 캡슐화, 다형성, 추상화입니다.',
                           NULL),

                          ('자바에서 main 메소드의 올바른 시그니처는?',
                           'public static void main(String[] args)',
                           '["public void main(String[] args)", "public static void main()", "static void main(String[] args)", "public static void main(String[] args)"]'::jsonb,
                           '자바 애플리케이션의 시작점인 main 메소드는 반드시 public static void main(String[] args) 형태로 선언해야 합니다. JVM이 이 시그니처를 찾아 프로그램을 실행합니다.',
                           NULL),

                          ('다음 중 자바의 접근 제한자(Access Modifier)를 가장 제한적인 것부터 나열한 것은?',
                           'private → default → protected → public',
                           '["private → default → protected → public", "public → protected → default → private", "private → protected → default → public", "default → private → protected → public"]'::jsonb,
                           '자바의 접근 제한자는 private(해당 클래스 내에서만 접근 가능), default(같은 패키지 내에서만 접근 가능), protected(같은 패키지 및 상속받은 클래스에서 접근 가능), public(어디서든 접근 가능) 순으로 제한 범위가 넓어집니다.',
                           NULL),

                          ('자바에서 "=="와 "equals()" 메소드의 차이점은?',
                           '== 연산자는 참조 값을 비교하고, equals()는 객체의 내용을 비교한다',
                           '["== 연산자는 참조 값을 비교하고, equals()는 객체의 내용을 비교한다", "== 연산자는 객체의 내용을 비교하고, equals()는 참조 값을 비교한다", "== 연산자와 equals() 메소드는 모두 객체의 내용을 비교한다", "== 연산자와 equals() 메소드는 모두 참조 값을 비교한다"]'::jsonb,
                           '== 연산자는 기본 타입일 경우 값을 비교하고, 참조 타입일 경우 객체의 주소를 비교합니다. equals() 메소드는 Object 클래스에서 상속받은 메소드로, 재정의하지 않으면 == 연산자와 같이 동작하지만, String과 같은 클래스에서는 내용 비교를 위해 오버라이딩되어 있습니다.',
                           NULL),

                          ('다음 중 자바의 예외 처리 구문으로 올바른 것은?',
                           'try { ... } catch (Exception e) { ... } finally { ... }',
                           '["try { ... } catch { ... } finally { ... }", "try { ... } exception (Exception e) { ... }", "try { ... } catch (Exception e) { ... } finally { ... }", "try { ... } except (Exception e) { ... } finally { ... }"]'::jsonb,
                           '자바에서 예외 처리는 try-catch-finally 블록을 사용합니다. try 블록에서 예외가 발생할 수 있는 코드를 작성하고, catch 블록에서 예외를 처리하며, finally 블록은 예외 발생 여부와 상관없이 항상 실행됩니다.',
                           NULL),

                          ('다음 코드의 출력 결과는?',
                           '15',
                           '["10", "15", "20", "컴파일 에러"]'::jsonb,
                           '삼항 연산자 조건 ? 참일 때 값 : 거짓일 때 값 형태로 사용합니다. 5 > 2는 참이므로 5 + 10인 15가 출력됩니다.',
                           'int result = 5 > 2 ? 5 + 10 : 20;\nSystem.out.println(result);'),

                          ('자바에서 String 클래스가 불변(immutable)인 이유는?',
                           '보안, 스레드 안전성, 해시코드 캐싱 등의 이점 때문',
                           '["메모리를 절약하기 위해", "보안, 스레드 안전성, 해시코드 캐싱 등의 이점 때문", "가비지 컬렉션을 효율적으로 하기 위해", "문자열 연산을 빠르게 하기 위해"]'::jsonb,
                           'String이 불변인 이유는 여러 가지가 있습니다. 보안 측면에서 중요한 데이터(비밀번호 등)가 변경되지 않도록 보장하고, 여러 스레드에서 동시에 접근해도 안전하며, String이 HashMap이나 HashSet의 키로 자주 사용되기 때문에 해시코드를 캐싱할 수 있어 성능상 이점이 있습니다.',
                           NULL),

                          ('Java SE와 Java EE의 차이점은?',
                           'Java SE는 표준 에디션으로 기본 기능을 제공하고, Java EE는 기업 환경을 위한 확장 기능을 제공한다',
                           '["Java SE는 보안 에디션, Java EE는 확장 에디션이다", "Java SE는 표준 에디션으로 기본 기능을 제공하고, Java EE는 기업 환경을 위한 확장 기능을 제공한다", "Java SE는 서버 에디션, Java EE는 임베디드 에디션이다", "Java SE는 단일 스레드만 지원하고, Java EE는 멀티 스레딩을 지원한다"]'::jsonb,
                           'Java SE(Standard Edition)는 자바 언어의 핵심 기능을 포함하는 기본 플랫폼입니다. Java EE(Enterprise Edition)는 SE를 기반으로 대규모 기업 환경에서 필요한 웹 서비스, 분산 컴퓨팅, 트랜잭션 관리 등의 기능을 추가로 제공합니다.',
                           NULL),

                          ('자바에서 가비지 컬렉션(Garbage Collection)이란?',
                           '더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제하는 기능',
                           '["프로그램이 종료될 때 모든 객체를 삭제하는 기능", "더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제하는 기능", "개발자가 명시적으로 호출하여 메모리를 정리하는 기능", "사용하지 않는 변수를 제거하는 컴파일러 최적화 기술"]'::jsonb,
                           '가비지 컬렉션은 JVM의 중요한 기능으로, 프로그래머가 명시적으로 메모리를 해제하지 않아도 더 이상 사용되지 않는(참조되지 않는) 객체를 탐지하고 자동으로 메모리에서 제거합니다. 이를 통해 메모리 누수를 방지하고 개발자의 부담을 줄입니다.',
                           NULL),

                          ('자바에서 final 키워드의 용도는?',
                           '클래스, 메소드, 변수가 더 이상 변경될 수 없음을 나타냄',
                           '["예외 처리를 위한 키워드", "클래스, 메소드, 변수가 더 이상 변경될 수 없음을 나타냄", "객체 생성을 위한 키워드", "스레드 동기화를 위한 키워드"]'::jsonb,
                           'final 키워드는 다양한 상황에서 사용됩니다. 변수에 사용하면 상수가 되어 값을 변경할 수 없고, 메소드에 사용하면 오버라이딩할 수 없으며, 클래스에 사용하면 상속할 수 없게 됩니다.',
                           NULL),

                          ('자바에서 인터페이스(Interface)와 추상 클래스(Abstract Class)의 주요 차이점은?',
                           '인터페이스는 다중 상속이 가능하지만, 추상 클래스는 단일 상속만 가능하다',
                           '["인터페이스는 메소드 구현이 불가능하지만, 추상 클래스는 일부 메소드 구현이 가능하다", "인터페이스는 다중 상속이 가능하지만, 추상 클래스는 단일 상속만 가능하다", "인터페이스는 필드를 가질 수 없지만, 추상 클래스는 필드를 가질 수 있다", "모든 위의 설명이 맞다"]'::jsonb,
                           '인터페이스와 추상 클래스의 차이점은 여러 가지가 있습니다. 인터페이스는 다중 구현이 가능하지만 추상 클래스는 단일 상속만 가능합니다. 인터페이스는 Java 8 이전에는 추상 메소드만 가질 수 있었으나(Java 8부터는 default, static 메소드 가능), 추상 클래스는 추상 메소드와 일반 메소드를 모두 가질 수 있습니다. 인터페이스는 상수만 가질 수 있지만, 추상 클래스는 인스턴스 변수를 가질 수 있습니다. 사실 정답은 "모든 위의 설명이 맞다"이지만, 가장 핵심적인 차이점은 다중 상속/구현의 가능 여부입니다.',
                           NULL),

                          ('자바에서 오버로딩(Overloading)과 오버라이딩(Overriding)의 차이점은?',
                           '오버로딩은 같은 이름의 메소드를 다른 매개변수로 정의하는 것이고, 오버라이딩은 상속받은 메소드를 재정의하는 것이다',
                           '["오버로딩은 상속 관계에서만 가능하고, 오버라이딩은 같은 클래스 내에서만 가능하다", "오버로딩은 같은 이름의 메소드를 다른 매개변수로 정의하는 것이고, 오버라이딩은 상속받은 메소드를 재정의하는 것이다", "오버로딩은 컴파일 타임에 결정되고, 오버라이딩은 프로그램 실행 중에 결정된다", "오버로딩은 성능 최적화를 위한 것이고, 오버라이딩은 보안을 위한 것이다"]'::jsonb,
                           '오버로딩은 한 클래스 내에서 같은 이름의 메소드를 매개변수의 개수나 타입을 다르게 하여 여러 개 정의하는 것입니다. 오버라이딩은 상속 관계에서 자식 클래스가 부모 클래스의 메소드를 같은 시그니처로 재정의하는 것입니다. 오버로딩은 컴파일 시점에 결정되는 정적 바인딩이고, 오버라이딩은 런타임에 결정되는 동적 바인딩입니다.',
                           NULL),

                          ('다음 코드의 출력 결과는?',
                           'Child Method',
                           '["Parent Method", "Child Method", "컴파일 에러", "런타임 에러"]'::jsonb,
                           '이 코드는 메소드 오버라이딩의 예입니다. Parent 타입 변수로 Child 객체를 참조하고 있지만, 호출되는 메소드는 실제 객체의 타입인 Child 클래스의 메소드입니다. 이것은 자바의 동적 바인딩(Dynamic Binding) 특성 때문입니다.',
                           'class Parent {\n    void method() {\n        System.out.println("Parent Method");\n    }\n}\n\nclass Child extends Parent {\n    @Override\n    void method() {\n        System.out.println("Child Method");\n    }\n}\n\npublic class Test {\n    public static void main(String[] args) {\n        Parent p = new Child();\n        p.method();\n    }\n}'),

                          ('Java 컬렉션 프레임워크에서 List, Set, Map의 주요 차이점은?',
                           'List는 순서가 있고 중복 허용, Set은 순서가 없고 중복 불허, Map은 키-값 쌍으로 저장하며 키는 중복 불허',
                           '["List, Set, Map 모두 순서를 유지하고 중복을 허용한다", "List는 순서가 있고 중복 허용, Set은 순서가 없고 중복 불허, Map은 키-값 쌍으로 저장하며 키는 중복 불허", "List, Set, Map 모두 인덱스로 요소에 접근할 수 있다", "List는 키-값 구조, Set은 순서 있는 컬렉션, Map은 중복을 허용하지 않는 컬렉션이다"]'::jsonb,
                           '컬렉션 프레임워크의 주요 인터페이스인 List, Set, Map은 각각 다른 특성을 가집니다. List는 순서가 있는 컬렉션으로 같은 요소의 중복을 허용합니다(예: ArrayList, LinkedList). Set은 순서가 없고 중복을 허용하지 않는 컬렉션입니다(예: HashSet, TreeSet). Map은 키-값 쌍으로 이루어진 컬렉션으로 키는 중복될 수 없지만 값은 중복될 수 있습니다(예: HashMap, TreeMap).',
                           NULL),

                          ('자바에서 제네릭(Generics)을 사용하는 주된 이유는?',
                           '컴파일 시점에 타입 안정성을 보장하고 불필요한 타입 캐스팅을 줄이기 위해',
                           '["다형성을 구현하기 위해", "컴파일 시점에 타입 안정성을 보장하고 불필요한 타입 캐스팅을 줄이기 위해", "코드 실행 속도를 높이기 위해", "메모리 사용량을 줄이기 위해"]'::jsonb,
                           '제네릭은 클래스, 인터페이스, 메소드를 정의할 때 타입 매개변수를 사용하여 다양한 타입에 대응할 수 있게 하는 기능입니다. 주요 목적은 컴파일 시점에 타입 체크를 하여 타입 안정성을 보장하고, 런타임에 발생할 수 있는 ClassCastException을 방지하며, 타입 캐스팅 코드를 줄여 코드의 가독성을 높이는 것입니다.',
                           NULL)
                  ) AS questions(question, answer, options, explanation, code)
     ),
-- 자바 중급 면접 질문
     java_intermediate_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             WITH quiz_ids AS (
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'INTERMEDIATE' AND title LIKE '%중급%'
             )
             SELECT
                 NOW() - (random() * INTERVAL '60 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 questions.question,
                 'MULTIPLE_CHOICE',
                 'INTERMEDIATE',
                 questions.answer,
                 questions.options,
                 questions.explanation,
                 10, -- 중급 문제 10점
                 60, -- 60초 제한시간
                 (SELECT id FROM quiz_ids ORDER BY random() LIMIT 1),
                 questions.code
             FROM (
                      VALUES
                          ('자바의 실행 과정을 올바르게 설명한 것은?',
                           '소스 코드(.java) → 바이트 코드(.class) → JVM에서 실행',
                           '["소스 코드(.java) → 네이티브 코드(.exe) → 운영체제에서 실행", "소스 코드(.java) → 바이트 코드(.class) → JVM에서 실행", "바이트 코드(.class) → 소스 코드(.java) → JVM에서 실행", "소스 코드(.java) → 직접 OS에서 실행"]'::jsonb,
                           '자바 프로그램의 실행 과정은 다음과 같습니다: 개발자가 .java 파일을 작성 → 자바 컴파일러(javac)가 이를 컴파일하여 바이트 코드(.class 파일)로 변환 → JVM이 바이트 코드를 로드하고 실행합니다. 이 과정 덕분에 자바는 "한 번 작성하면 어디서나 실행"(WORA) 특성을 갖게 됩니다.',
                           NULL),

                          ('다음 중 Thread-safe하지 않은 컬렉션은?',
                           'ArrayList',
                           '["ArrayList", "Vector", "ConcurrentHashMap", "CopyOnWriteArrayList"]'::jsonb,
                           'ArrayList는 Thread-safe하지 않습니다. 다중 스레드 환경에서 안전하게 사용하려면 Collections.synchronizedList()로 래핑하거나, Vector, CopyOnWriteArrayList 같은 Thread-safe한 컬렉션을 사용해야 합니다. Vector는 메소드에 synchronized 키워드가 붙어있어 Thread-safe하며, ConcurrentHashMap과 CopyOnWriteArrayList는 Java 5부터 추가된 동시성 컬렉션입니다.',
                           NULL),

                          ('자바에서 Checked Exception과 Unchecked Exception의 차이점은?',
                           'Checked Exception은 컴파일 시점에 확인되고 명시적인 처리가 필요하지만, Unchecked Exception은 런타임에 발생하고 명시적인 처리가 강제되지 않는다',
                           '["Checked Exception은 심각한 오류를 나타내고, Unchecked Exception은 경미한 오류를 나타낸다", "Checked Exception은 복구 가능한 오류이고, Unchecked Exception은 복구 불가능한 오류이다", "Checked Exception은 컴파일 시점에 확인되고 명시적인 처리가 필요하지만, Unchecked Exception은 런타임에 발생하고 명시적인 처리가 강제되지 않는다", "Checked Exception은 JVM에 의해 발생하고, Unchecked Exception은 프로그래머에 의해 발생한다"]'::jsonb,
                           'Checked Exception은 Exception 클래스를 상속하며, 컴파일 시점에 확인됩니다. 메소드에서 throws 절로 선언하거나 try-catch로 처리해야 합니다(예: IOException, SQLException). Unchecked Exception은 RuntimeException을 상속하며, 컴파일러가 예외 처리를 강제하지 않습니다(예: NullPointerException, ArrayIndexOutOfBoundsException). 일반적으로 프로그램 오류를 나타내며, 미리 방지하는 것이 좋습니다.',
                           NULL),

                          ('다음 코드의 실행 결과는?',
                           '종료 블록\n예외 발생: java.lang.ArithmeticException: / by zero',
                           '["예외 발생: java.lang.ArithmeticException: / by zero", "결과: 0\n종료 블록", "종료 블록\n예외 발생: java.lang.ArithmeticException: / by zero", "0"]'::jsonb,
                           '이 코드에서는 try 블록에서 10/0으로 ArithmeticException이 발생합니다. 발생한 예외는 catch 블록에서 잡히고 메시지가 출력됩니다. finally 블록은 예외 발생 여부와 상관없이 항상 실행되므로 "종료 블록"이 먼저 출력되고, 그 다음 catch 블록의 내용이 출력됩니다.',
                           'public class ExceptionTest {\n    public static void main(String[] args) {\n        try {\n            int result = 10 / 0; // ArithmeticException 발생\n            System.out.println("결과: " + result);\n        } catch (ArithmeticException e) {\n            System.out.println("예외 발생: " + e);\n        } finally {\n            System.out.println("종료 블록");\n        }\n    }\n}'),

                          ('자바에서 synchronized 키워드의 역할은?',
                           '여러 스레드가 동시에 접근하는 것을 방지하여 스레드 안전성을 보장한다',
                           '["메소드의 실행 속도를 높인다", "여러 스레드가 동시에 접근하는 것을 방지하여 스레드 안전성을 보장한다", "메모리 사용량을 최적화한다", "예외 처리를 자동화한다"]'::jsonb,
                           'synchronized 키워드는 멀티스레드 환경에서 여러 스레드가 공유 자원에 동시에 접근하는 것을 방지하는 동기화 메커니즘입니다. 메소드나 블록에 synchronized를 사용하면 한 시점에 하나의 스레드만 해당 코드를 실행할 수 있어 데이터 일관성과 스레드 안전성을 보장합니다. 다만, 과도한 사용은 성능 저하를 가져올 수 있습니다.',
                           NULL),

                          ('다음 중 불변 객체(Immutable Object)의 특징이 아닌 것은?',
                           '객체 생성 후 상태를 변경할 수 있다',
                           '["객체 생성 후 상태를 변경할 수 있다", "모든 필드가 final이다", "클래스가 상속되지 않도록 설계된다", "getter는 있지만 setter는 없다"]'::jsonb,
                           '불변 객체는 생성 후에 그 상태가 변경되지 않는 객체입니다. 일반적인 특징으로는 1) 모든 필드가 final로 선언됨 2) 클래스가 final로 선언되거나 다른 방법으로 상속 방지 3) 상태 변경 메소드(setter 등)가 없음 4) 가변 객체를 참조하는 필드가 있다면, 그것이 외부로 노출되지 않도록 방어적 복사를 사용함 등이 있습니다. String, Integer 같은 래퍼 클래스가 대표적인 불변 객체입니다.',
                           NULL),

                          ('자바에서 equals()와 hashCode() 메소드를 함께 오버라이딩해야 하는 이유는?',
                           'equals()가 true를 반환하는 두 객체는 반드시 같은 hashCode를 반환해야 하기 때문이다',
                           '["자바 문법상 두 메소드는 항상 함께 구현해야 하기 때문이다", "equals()가 true를 반환하는 두 객체는 반드시 같은 hashCode를 반환해야 하기 때문이다", "hashCode()는 객체 비교 시 항상 먼저 호출되기 때문이다", "두 메소드 모두 성능 최적화에 필수적이기 때문이다"]'::jsonb,
                           'equals()와 hashCode()는 함께 오버라이딩하는 것이 중요한 이유는 자바의 일반 규약 때문입니다. 이 규약에 따르면, equals() 메소드로 비교했을 때 동등한 두 객체는 반드시 같은 hashCode 값을 반환해야 합니다. 만약 이 규약을 지키지 않으면 HashMap, HashSet과 같은 해시 기반 컬렉션에서 객체가 예상대로 동작하지 않게 됩니다. 예를 들어, equals()로는 같다고 판단되는 객체가 서로 다른 hashCode를 반환하면, HashMap에서 검색 시 원하는 객체를 찾지 못할 수 있습니다.',
                           NULL),

                          ('자바에서 static 키워드의 특징으로 올바른 것은?',
                           'static 멤버는 클래스가 로드될 때 메모리에 할당되며, 모든 인스턴스가 공유한다',
                           '["static 메소드 내에서 this 키워드를 사용할 수 있다", "static 블록은 객체 생성 시마다 실행된다", "static 멤버는 클래스가 로드될 때 메모리에 할당되며, 모든 인스턴스가 공유한다", "static 메소드는 오버라이딩이 가능하다"]'::jsonb,
                           'static 키워드는 클래스 수준의 멤버를 정의할 때 사용합니다. static 멤버(변수나 메소드)는 클래스가 메모리에 로드될 때 생성되어 프로그램이 종료될 때까지 유지되며, 모든 인스턴스가 이를 공유합니다. static 메소드 내에서는 this를 사용할 수 없고(인스턴스가 없을 수 있으므로), static 메소드는 오버라이딩되지 않습니다(클래스에 바인딩되므로). static 블록은 클래스가 로드될 때 한 번만 실행됩니다.',
                           NULL),

                          ('다음 코드의 출력 결과는?',
                           'Value1: 10\nValue2: 20',
                           '["Value1: 10\nValue2: 10", "Value1: 10\nValue2: 20", "Value1: 20\nValue2: 20", "컴파일 에러"]'::jsonb,
                           '이 코드에서 changeValue 메소드는 기본 타입(int)을 매개변수로 받습니다. 자바에서 기본 타입은 값이 복사되어 전달되므로(pass by value), 메소드 내에서 값을 변경해도 원본에는 영향을 주지 않습니다. 따라서 value1은 변경되지 않고 10을 유지합니다. 반면, changeReferenceValue 메소드는 참조 타입(StringBuilder)을 매개변수로 받습니다. 참조 타입도 값으로 전달되지만, 그 값이 객체의 참조이므로 메소드 내에서 같은 객체를 참조하고 그 객체의 상태를 변경할 수 있습니다. 따라서 sb 객체의 내용이 "10"에서 "20"으로 변경됩니다.',
                           'public class PassByValueTest {\n    public static void main(String[] args) {\n        int value1 = 10;\n        StringBuilder sb = new StringBuilder("10");\n        \n        changeValue(value1);\n        changeReferenceValue(sb);\n        \n        System.out.println("Value1: " + value1);\n        System.out.println("Value2: " + sb);\n    }\n    \n    public static void changeValue(int value) {\n        value = 20;\n    }\n    \n    public static void changeReferenceValue(StringBuilder value) {\n        value.delete(0, value.length());\n        value.append("20");\n    }\n}'),

                          ('자바에서 String, StringBuilder, StringBuffer의 주요 차이점은?',
                           'String은 불변, StringBuffer는 Thread-safe하고 가변적, StringBuilder는 Thread-safe하지 않고 가변적이다',
                           '["String, StringBuilder, StringBuffer 모두 불변(immutable)이다", "String은 불변, StringBuffer는 Thread-safe하고 가변적, StringBuilder는 Thread-safe하지 않고 가변적이다", "String은 가변적, StringBuilder와 StringBuffer는 불변이다", "String은 Thread-safe하지 않고, StringBuilder와 StringBuffer는 Thread-safe하다"]'::jsonb,
                           'String은 불변 클래스로, 한 번 생성된 문자열은 변경할 수 없습니다. 따라서 문자열 연산이 많은 경우 성능 저하가 발생할 수 있습니다. StringBuffer와 StringBuilder는 모두 가변적이며 내부 버퍼를 사용하여 문자열 조작이 효율적입니다. 주요 차이점은 StringBuffer는 Thread-safe하지만(synchronized 메소드 사용), StringBuilder는 Thread-safe하지 않아 단일 스레드 환경에서 더 빠릅니다. JDK 1.5부터 추가된 StringBuilder가 성능 면에서 유리해 단일 스레드 환경에서는 주로 StringBuilder를 사용합니다.',
                           NULL),

                          ('try-with-resources 구문의 목적은?',
                           '자동으로 리소스를 닫아주어 리소스 누수를 방지한다',
                           '["예외 발생을 방지한다", "자동으로 리소스를 닫아주어 리소스 누수를 방지한다", "코드의 가독성만 향상시킨다", "실행 속도를 개선한다"]'::jsonb,
                           'try-with-resources는 Java 7에서 도입된 구문으로, AutoCloseable 인터페이스를 구현한 자원(파일, 데이터베이스 연결, 네트워크 연결 등)을 사용한 후 자동으로 close() 메소드를 호출하여 닫아줍니다. 이를 통해 개발자가 명시적으로 finally 블록에서 리소스를 닫지 않아도 되므로 코드가 간결해지고, 예외가 발생하더라도 리소스 누수를 방지할 수 있습니다.',
                           NULL),

                          ('자바의 직렬화(Serialization)와 역직렬화(Deserialization)란?',
                           '직렬화는 객체를 바이트 스트림으로 변환하는 과정이고, 역직렬화는 바이트 스트림을 다시 객체로 변환하는 과정이다',
                           '["직렬화는 클래스를 컴파일하는 과정이고, 역직렬화는 바이트코드를 실행하는 과정이다", "직렬화는 객체를 바이트 스트림으로 변환하는 과정이고, 역직렬화는 바이트 스트림을 다시 객체로 변환하는 과정이다", "직렬화는 객체의 메모리 주소를 저장하는 것이고, 역직렬화는 주소로부터 객체를 복원하는 것이다", "직렬화는 데이터베이스에 객체를 저장하는 과정이고, 역직렬화는 데이터베이스에서 객체를 가져오는 과정이다"]'::jsonb,
                           '직렬화(Serialization)는 객체의 상태를 바이트 스트림으로 변환하는 과정으로, 객체를 파일로 저장하거나 네트워크를 통해 전송할 때 사용됩니다. 역직렬화(Deserialization)는 바이트 스트림을 다시 객체로 복원하는 과정입니다. 자바에서 직렬화를 지원하려면 클래스가 Serializable 인터페이스를 구현해야 합니다. 직렬화는 객체의 완전한 복제본을 만들거나 객체의 상태를 영속화할 때 유용하지만, 보안 및 버전 관리 문제에 주의해야 합니다.',
                           NULL),

                          ('자바의 리플렉션(Reflection) API의 주요 용도는?',
                           '런타임에 클래스의 정보를 검사하고 조작하는 것',
                           '["컴파일 시간을 단축시키는 것", "런타임에 클래스의 정보를 검사하고 조작하는 것", "메모리 사용량을 최적화하는 것", "네트워크 통신을 간소화하는 것"]'::jsonb,
                           '리플렉션(Reflection)은 실행 중인 자바 프로그램이 자체적으로 검사하거나 내부 속성을 조작할 수 있게 하는 API입니다. 이를 통해 런타임에 클래스, 인터페이스, 필드, 메소드 등에 접근하여 정보를 가져오거나, 메소드를 호출하거나, 객체를 생성할 수 있습니다. 주로 프레임워크나 라이브러리에서 사용되며, 스프링의 의존성 주입, ORM의 객체-테이블 매핑, 직렬화 등에 활용됩니다. 그러나 타입 안전성이 손상되고, 성능 저하가 있을 수 있으며, 접근 제한을 우회할 수 있어 주의해서 사용해야 합니다.',
                           NULL),

                          ('자바에서 volatile 키워드의 역할은?',
                           '변수의 값이 스레드의 로컬 캐시가 아닌 항상 메인 메모리에서 읽고 쓰도록 보장한다',
                           '["변수의 값이 스레드의 로컬 캐시가 아닌 항상 메인 메모리에서 읽고 쓰도록 보장한다", "변수의 값이 변경되지 않도록 상수로 만든다", "메소드가 오버라이딩되지 않도록 한다", "객체의 직렬화를 가능하게 한다"]'::jsonb,
                           'volatile 키워드는 멀티스레드 환경에서 변수의 가시성(visibility) 문제를 해결하기 위해 사용됩니다. 멀티 코어 시스템에서 각 스레드는 CPU 캐시에 변수의 복사본을 유지할 수 있어, 한 스레드가 변수를 변경해도 다른 스레드는 이를 인식하지 못할 수 있습니다. volatile로 선언된 변수는 항상 메인 메모리에서 읽고 쓰도록 보장되어, 모든 스레드가 최신 값을 볼 수 있습니다. 그러나 volatile은 원자성(atomicity)을 보장하지 않으므로, 복합 연산(예: i++)에는 synchronized나 AtomicInteger 같은 다른 동기화 메커니즘이 필요합니다.',
                           NULL),

                          ('자바 메모리 관리에서 "세대별 가비지 컬렉션(Generational Garbage Collection)"이란?',
                           '객체를 Young 영역과 Old 영역으로 나누어 관리하는 방식',
                           '["모든 객체를 동일한 우선순위로 처리하는 방식", "객체를 Young 영역과 Old 영역으로 나누어 관리하는 방식", "사용자가 직접 가비지 컬렉션을 호출하는 방식", "가비지 컬렉션을 여러 스레드로 병렬 처리하는 방식"]'::jsonb,
                           '세대별 가비지 컬렉션은 객체의 수명에 따라 힙 메모리를 여러 영역으로 나누어 관리하는 방식입니다. 이는 "대부분의 객체는 생성 후 짧은 시간 내에 사용되지 않게 된다"는 약한 세대 가설(Weak Generational Hypothesis)에 기반합니다. 일반적으로 Young 영역(Eden, Survivor 0, Survivor 1)과 Old 영역으로 나뉘며, 객체는 Young 영역에 생성된 후 일정 시간 살아남으면 Old 영역으로 이동합니다. Young 영역에서의 가비지 컬렉션(Minor GC)은 빠르게 자주 일어나고, Old 영역에서의 가비지 컬렉션(Major GC 또는 Full GC)은 덜 자주 발생하도록 설계되어 전체적인 성능을 개선합니다.',
                           NULL),

                          ('Java의 람다 표현식(Lambda Expression)과 함수형 인터페이스(Functional Interface)의 관계는?',
                           '람다 표현식은 함수형 인터페이스의 구현체로 사용된다',
                           '["람다 표현식은 모든 인터페이스를 구현할 수 있다", "람다 표현식은 함수형 인터페이스의 구현체로 사용된다", "람다 표현식은 함수형 인터페이스와 관계가 없다", "함수형 인터페이스는 람다 표현식 없이는 사용할 수 없다"]'::jsonb,
                           '람다 표현식은 Java 8에 도입된 기능으로, 함수형 프로그래밍 방식을 지원합니다. 함수형 인터페이스는 단 하나의 추상 메소드만 가진 인터페이스로, @FunctionalInterface 어노테이션으로 표시할 수 있습니다(예: Runnable, Comparator, Consumer 등). 람다 표현식은 이러한 함수형 인터페이스의 구현체를 간결하게 작성할 수 있게 해줍니다. 예를 들어, `(a, b) -> a + b`와 같은 람다 표현식은 두 매개변수를 받아 그 합을 반환하는 함수를 나타내며, 이는 BinaryOperator와 같은 함수형 인터페이스의 구현체로 사용될 수 있습니다.',
                           NULL),

                          ('자바에서 제네릭 타입 소거(Type Erasure)란?',
                           '컴파일 시 제네릭 타입 정보가 제거되어 런타임에는 원시 타입만 남는 현상',
                           '["컴파일 시 제네릭 타입 정보가 제거되어 런타임에는 원시 타입만 남는 현상", "제네릭 클래스의 타입 매개변수를 자동으로 유추하는 기능", "제네릭을 사용하지 않는 코드로 자동 변환하는 기능", "컴파일러가 제네릭 관련 오류를 무시하는 설정"]'::jsonb,
                           '타입 소거(Type Erasure)는 Java에서 제네릭이 구현된 방식을 나타냅니다. 컴파일 시점에 제네릭 타입 정보가 제거되고, 필요한 곳에 형변환이 자동으로 삽입됩니다. 예를 들어, List<String>은 컴파일 후 바이트코드에서는 그냥 List가 됩니다. 이는 Java 5에서 제네릭이 도입될 때 이전 버전과의 호환성을 유지하기 위해 채택된 방식입니다. 타입 소거로 인해 런타임에는 제네릭 타입 정보를 얻을 수 없고, 제네릭 배열 생성 등에 제약이 생기는 단점이 있습니다.',
                           NULL)
                  ) AS questions(question, answer, options, explanation, code)
     ),
-- 자바 고급 면접 질문
     java_advanced_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             WITH quiz_ids AS (
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'ADVANCED' AND title LIKE '%심화%'
             )
             SELECT
                 NOW() - (random() * INTERVAL '60 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 questions.question,
                 'MULTIPLE_CHOICE',
                 'ADVANCED',
                 questions.answer,
                 questions.options,
                 questions.explanation,
                 15, -- 고급 문제 15점
                 75, -- 75초 제한시간
                 (SELECT id FROM quiz_ids ORDER BY random() LIMIT 1),
                 questions.code
             FROM (
                      VALUES
                          ('자바에서 G1 가비지 컬렉터(G1 Garbage Collector)의 주요 특징은?',
                           '힙 메모리를 균등한 크기의 영역(Region)으로 나누어 관리하고, 사용자가 지정한 최대 중지 시간(pause time) 목표를 달성하기 위해 최적화된다',
                           '["힙 메모리를 균등한 크기의 영역(Region)으로 나누어 관리하고, 사용자가 지정한 최대 중지 시간(pause time) 목표를 달성하기 위해 최적화된다", "항상 단일 스레드로 실행되어 CPU 사용량을 최소화한다", "전체 힙 메모리를 한 번에 처리하여 파편화를 방지한다", "Young 영역과 Old 영역의 구분 없이 모든 객체를 동일하게 처리한다"]'::jsonb,
                           'G1(Garbage First) GC는 Java 7에서 도입되고 Java 9부터 기본 GC로 설정된 고성능 가비지 컬렉터입니다. G1 GC의 주요 특징은 힙을 균등한 크기의 영역(Region)으로 나누어 관리하며, 이를 통해 전체 힙이 아닌 일부 영역만 수집할 수 있어 중지 시간을 줄일 수 있습니다. 또한 사용자가 지정한 최대 중지 시간(pause time) 목표에 맞추어 GC 작업을 수행하며, 영역별로 가비지가 많은 영역(Garbage First)부터 수집하여 효율성을 높입니다. G1은 전체적으로는 Young과 Old 영역의 개념을 유지하지만, 물리적으로 인접해 있지 않고 논리적으로만 구분됩니다.',
                           NULL),

                          ('자바 NIO(New Input/Output)의 주요 특징과 기존 IO와의 차이점은?',
                           'NIO는 버퍼 기반이고 논블로킹 IO를 지원하며, 채널 개념을 도입하여 양방향 데이터 전송이 가능하다',
                           '["NIO는 항상 기존 IO보다 빠르게 동작한다", "NIO는 버퍼 기반이고 논블로킹 IO를 지원하며, 채널 개념을 도입하여 양방향 데이터 전송이 가능하다", "NIO는 스트림만 사용하고 블로킹 방식으로만 작동한다", "NIO는 멀티스레딩을 지원하지 않는다"]'::jsonb,
                           'Java NIO(New Input/Output)는 Java 1.4에서 도입된 IO API로, 기존 IO 패키지와 비교해 몇 가지 주요 차이점이 있습니다. 1) 버퍼 지향: NIO는 버퍼(Buffer) 기반으로 작동하여 데이터를 일시적으로 저장하고 처리할 수 있습니다. 2) 논블로킹 IO: NIO는 논블로킹 모드를 지원하여 한 스레드가 여러 채널을 관리할 수 있습니다. 3) 채널: NIO는 채널(Channel)을 통해 데이터를 읽고 쓰며, 양방향 통신이 가능합니다. 4) 셀렉터: 하나의 스레드로 여러 채널을 모니터링할 수 있는 Selector를 제공합니다. 기존 IO는 스트림 지향적이고 블로킹 방식이며, 대용량 데이터나 많은 연결을 처리할 때 NIO가 더 효율적일 수 있습니다. 그러나 NIO가 항상 더 빠른 것은 아니며, 단순한 작업에는 기존 IO가 더 적합할 수 있습니다.',
                           NULL),

                          ('자바에서 메모리 누수(Memory Leak)가 발생할 수 있는 상황은?',
                           '정적 필드에 컬렉션 객체를 저장하고 요소를 계속 추가하기만 하는 경우',
                           '["모든 지역 변수는 메모리 누수를 일으킨다", "모든 정적 변수는 메모리 누수를 일으킨다", "정적 필드에 컬렉션 객체를 저장하고 요소를 계속 추가하기만 하는 경우", "모든 익명 클래스는 메모리 누수를 일으킨다"]'::jsonb,
                           '자바는 가비지 컬렉션을 통해 메모리를 자동으로 관리하지만, 여전히 메모리 누수가 발생할 수 있습니다. 주요 원인으로는 1) 정적 필드: 정적 필드는 애플리케이션 수명 동안 유지되므로, 컬렉션이나 큰 객체를 정적 필드에 저장하고 계속 추가만 한다면 메모리가 계속 증가합니다. 2) 캐시: 캐시에 객체를 넣고 제거하지 않으면 메모리가 소진될 수 있습니다. 3) 리스너 등록 해제 누락: 리스너나 콜백을 등록하고 해제하지 않으면 참조가 남아 메모리 누수가 발생합니다. 4) 클로저와 내부 클래스: 내부 클래스는 외부 클래스 인스턴스를 참조하므로, 필요 이상으로 오래 유지되면 메모리 누수를 일으킬 수 있습니다. 5) 스트림, 연결 등 리소스 미반환: try-with-resources 등을 사용하지 않고 명시적으로 닫지 않으면 메모리 누수가 발생할 수 있습니다.',
                           NULL),

                          ('다음 중 Java의 ClassLoader 시스템에 대한 설명으로 올바른 것은?',
                           'Bootstrap, Extension, Application ClassLoader가 계층 구조를 이루며 위임 모델(delegation model)에 따라 작동한다',
                           '["클래스는 항상 하나의 ClassLoader에 의해서만 로드된다", "Bootstrap, Extension, Application ClassLoader가 계층 구조를 이루며 위임 모델(delegation model)에 따라 작동한다", "ClassLoader는 Java 애플리케이션 시작 시 한 번만 사용된다", "모든 클래스는 동일한 ClassLoader에 의해 로드된다"]'::jsonb,
                           'Java의 ClassLoader 시스템은 계층적 구조로 이루어져 있으며, 클래스를 JVM으로 로드하는 역할을 합니다. 1) Bootstrap ClassLoader: 가장 기본적인 클래스로더로, JVM의 일부이며 네이티브 코드로 구현됩니다. java.lang 패키지 등 Java API의 핵심 클래스를 로드합니다. 2) Extension(Platform) ClassLoader: Bootstrap ClassLoader의 자식으로, 자바 확장 API(ext 디렉토리의 클래스)를 로드합니다. 3) Application(System) ClassLoader: 사용자가 정의한 클래스패스 상의 클래스를 로드합니다. 이 클래스로더들은 위임 모델(delegation model)에 따라 작동합니다. 즉, 클래스 로드 요청이 오면 먼저 부모 클래스로더에게 위임하고, 부모가 로드할 수 없을 때만 자신이 로드를 시도합니다. 이를 통해 클래스의 유일성과 안전성을 보장합니다. 사용자는 또한 자신만의 커스텀 ClassLoader를 만들어 특별한 클래스 로딩 동작을 구현할 수도 있습니다.',
                           NULL),

                          ('다음 코드에서 발생할 수 있는 문제점은?',
                           '경쟁 상태(race condition)가 발생할 수 있다',
                           '["경쟁 상태(race condition)가 발생할 수 있다", "데드락(deadlock)이 발생할 수 있다", "메모리 누수(memory leak)가 발생할 수 있다", "스택 오버플로우(stack overflow)가 발생할 수 있다"]'::jsonb,
                           '이 코드에서는 counter 변수를 두 개의 스레드에서 동시에 접근하여 증가시키고 있습니다. counter++는 원자적 연산이 아니라 읽기, 증가, 쓰기의 세 단계로 이루어지는 복합 연산입니다. 따라서 한 스레드가 counter 값을 읽고 아직 증가된 값을 쓰기 전에 다른 스레드가 같은 counter 값을 읽게 되면, 예상보다 적은 횟수만 증가하는 경쟁 상태(race condition)가 발생할 수 있습니다. 이 문제를 해결하려면 synchronized 키워드를 사용하거나, java.util.concurrent.atomic 패키지의 AtomicInteger 같은 원자적 변수 타입을 사용해야 합니다.',
                           'public class CounterTest {\n    private static int counter = 0;\n    \n    public static void main(String[] args) throws InterruptedException {\n        Thread t1 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                counter++;\n            }\n        });\n        \n        Thread t2 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                counter++;\n            }\n        });\n        \n        t1.start();\n        t2.start();\n        \n        t1.join();\n        t2.join();\n        \n        System.out.println("Counter value: " + counter);\n    }\n}'),

                          ('Java의 CompletableFuture와 기존 Future의 주요 차이점은?',
                           'CompletableFuture는 비동기 작업을 조합하고 콜백을 지원하는 기능을 제공한다',
                           '["CompletableFuture는 항상 단일 스레드로 실행된다", "CompletableFuture는 비동기 작업을 조합하고 콜백을 지원하는 기능을 제공한다", "CompletableFuture는 동기 방식으로만 작동한다", "CompletableFuture는 결과를 반환할 수 없다"]'::jsonb,
                           'CompletableFuture는 Java 8에서 도입된 Future 인터페이스의 구현체로, 기존 Future보다 더 많은 기능을 제공합니다. 주요 차이점으로는 1) 작업 조합: thenApply, thenCompose, thenCombine 등의 메소드를 통해 비동기 작업을 순차적으로 조합하거나 병렬로 실행할 수 있습니다. 2) 콜백 지원: thenAccept, thenRun 등으로 결과가 준비되었을 때 실행할 콜백을 등록할 수 있습니다. 3) 예외 처리: exceptionally, handle 등으로 예외 처리 로직을 추가할 수 있습니다. 4) 완료 처리: complete 메소드로 외부에서 결과를 설정할 수 있습니다. 5) 다양한 팩토리 메소드: completedFuture, supplyAsync, runAsync 등 다양한 생성 방법을 제공합니다. 이러한 기능들은 복잡한 비동기 작업 흐름을 더 명확하고 유연하게 표현할 수 있게 해줍니다.',
                           NULL),

                          ('자바에서 메소드 참조(Method Reference)를 사용하는 올바른 예는?',
                           'list.forEach(System.out::println);',
                           '["list.forEach(System.out.println);", "list.forEach(System.out::println);", "list.forEach(::System.out.println);", "list.forEach(System::out::println);"]'::jsonb,
                           '메소드 참조(Method Reference)는 Java 8에서 도입된 기능으로, 이미 정의된 메소드를 람다 표현식 대신 사용할 수 있게 해줍니다. 메소드 참조는 :: 연산자를 사용하여 표현하며, 코드를 더 간결하게 만들 수 있습니다. 메소드 참조의 종류로는 1) 정적 메소드 참조: ClassName::staticMethodName 2) 인스턴스 메소드 참조: instance::instanceMethodName 3) 특정 타입의 인스턴스 메소드 참조: ClassName::instanceMethodName 4) 생성자 참조: ClassName::new 가 있습니다. 예시에서 System.out::println은 System.out 객체의 println 메소드를 참조하는 인스턴스 메소드 참조입니다.',
                           NULL),

                          ('자바의 Virtual Thread(가상 스레드)의 주요 특징은?',
                           '운영체제 스레드보다 가볍고, 블로킹 작업에서도 효율적으로 리소스를 사용한다',
                           '["물리적 CPU 코어마다 하나씩만 생성될 수 있다", "운영체제 스레드보다 가볍고, 블로킹 작업에서도 효율적으로 리소스를 사용한다", "기존 스레드보다 느리지만 더 안정적이다", "Java 7부터 도입된 기능이다"]'::jsonb,
                           'Virtual Thread(가상 스레드)는 Java 19에서 프리뷰로 도입되고 Java 21에서 정식 기능으로 포함된 경량 스레드 구현입니다. 주요 특징으로는 1) 경량성: 운영체제 스레드에 비해 매우 적은 메모리를 사용하여 수백만 개의 가상 스레드를 생성할 수 있습니다. 2) 효율적인 블로킹: 가상 스레드가 블로킹 작업을 수행할 때 캐리어 스레드(플랫폼 스레드)를 점유하지 않고 양보하여 다른 가상 스레드가 실행될 수 있게 합니다. 3) 플랫폼 스레드와 동일한 API: 기존 Thread API와 호환되어 사용하기 쉽습니다. 4) 동시성 모델 개선: 많은 동시 요청을 처리하는 서버 애플리케이션에서 확장성을 크게 향상시킬 수 있습니다. 가상 스레드는 "스레드 당 요청" 모델을 효율적으로 구현할 수 있게 하여, 복잡한 비동기 프로그래밍 없이도 높은 처리량을 달성할 수 있습니다.',
                           NULL),

                          ('Java 모듈 시스템(JPMS, Java Platform Module System)의 주요 목적은?',
                           '강력한 캡슐화, 명시적 의존성 선언, 모듈화된 JDK를 제공하여 플랫폼 확장성과 보안을 향상시킨다',
                           '["메모리 사용량을 줄이는 것", "애플리케이션 실행 속도를 높이는 것", "강력한 캡슐화, 명시적 의존성 선언, 모듈화된 JDK를 제공하여 플랫폼 확장성과 보안을 향상시킨다", "기존 패키지 시스템을 완전히 대체하는 것"]'::jsonb,
                           'Java 모듈 시스템(JPMS)은 Java 9에서 Project Jigsaw의 일부로 도입되었습니다. 주요 목적과 특징으로는 1) 강력한 캡슐화: 모듈은 명시적으로 외부에 노출할 패키지만 공개하고, 나머지는 모듈 내부로 숨길 수 있습니다. 이는 패키지 수준의 접근 제한보다 더 강력한 캡슐화를 제공합니다. 2) 명시적 의존성: 모듈은 module-info.java 파일에 자신이 필요로 하는 의존성을 명시적으로 선언합니다. 이로 인해 런타임 전에 의존성 문제를 탐지할 수 있습니다. 3) 모듈화된 JDK: JDK 자체가 여러 모듈로 나뉘어, 애플리케이션에 필요한 모듈만 포함할 수 있게 되었습니다. 이는 더 작은 런타임과 배포 크기를 가능하게 합니다. 4) 보안 향상: 명시적으로 허용하지 않은 내부 API에 대한 접근이 차단되어 보안이 향상됩니다. 5) 플랫폼 무결성: JDK의 내부 API를 보호하여 플랫폼 진화를 더 용이하게 합니다. JPMS는 기존 패키지 시스템을 대체하는 것이 아니라 그 위에 구축되어 대규모 애플리케이션의 구조와 의존성을 더 잘 관리할 수 있게 해줍니다.',
                           NULL),

                          ('자바에서 패턴 매칭(Pattern Matching)을 사용한 올바른 예는?',
                           'if (obj instanceof String s) { System.out.println(s.length()); }',
                           '["if (obj instanceof String) { System.out.println(((String)obj).length()); }", "if (obj instanceof String s) { System.out.println(s.length()); }", "if (obj matches String) { System.out.println(obj.length()); }", "if (obj instanceof String s && s.isEmpty()) { System.out.println(obj); }"]'::jsonb,
                           '패턴 매칭은 Java 16에서 정식으로 도입된 기능으로, 객체의 타입과 구조를 검사하고 조건이 일치하면 변수에 바인딩할 수 있습니다. 패턴 매칭은 instanceof 연산자와 함께 사용되며, 기존에 instanceof 검사 후 별도로 타입 캐스팅을 하던 코드를 더 간결하게 만들 수 있습니다. `if (obj instanceof String s) { System.out.println(s.length()); }`에서 객체가 String 타입이면 s 변수에 자동으로 캐스팅되어 바인딩됩니다. Java 17에서는 switch 문에서도 패턴 매칭을 사용할 수 있게 되었고, Java의 후속 버전에서는 레코드 패턴, 배열 패턴 등 더 다양한 패턴 매칭 기능이 추가될 예정입니다.',
                           NULL),

                          ('Java에서 레코드(Record)의 주요 특징이 아닌 것은?',
                           '필드 값을 변경할 수 있는 setter 메소드가 자동 생성된다',
                           '["불변(immutable) 데이터 클래스를 간결하게 정의할 수 있다", "equals(), hashCode(), toString() 메소드가 자동 생성된다", "필드 값을 변경할 수 있는 setter 메소드가 자동 생성된다", "각 필드에 접근할 수 있는 getter 메소드가 자동 생성된다"]'::jsonb,
                           'Record는 Java 16에서 정식 기능으로 도입된 새로운 유형의 클래스로, 데이터를 저장하기 위한 목적으로 설계되었습니다. 주요 특징으로는 1) 불변성: 레코드는 불변(immutable) 객체로, 생성 후 내부 상태를 변경할 수 없습니다. 따라서 setter 메소드가 자동 생성되지 않습니다. 2) 간결한 구문: `record Point(int x, int y) {}`와 같이 매우 간결하게 정의할 수 있습니다. 3) 자동 생성 메소드: 생성자, 각 필드의 접근자(예: x(), y()), equals(), hashCode(), toString() 메소드가 자동으로 생성됩니다. 4) 투명성: 레코드는 그 내용이 공개적으로 접근 가능하고 표현 가능합니다. 레코드는 주로 DTO(Data Transfer Object), 값 객체(Value Object) 등 데이터 전달 목적의 클래스를 간결하게 정의할 때 유용합니다.',
                           NULL),

                          ('자바에서 Heap과 Stack 메모리의 차이점으로 올바른 것은?',
                           'Stack은 각 스레드마다 하나씩 할당되고 주로 메소드 호출과 지역 변수를 저장하며, Heap은 모든 스레드가 공유하고 객체를 저장한다',
                           '["Heap은 각 스레드마다 하나씩 할당되고, Stack은 모든 스레드가 공유한다", "Stack은 각 스레드마다 하나씩 할당되고 주로 메소드 호출과 지역 변수를 저장하며, Heap은 모든 스레드가 공유하고 객체를 저장한다", "Heap은 항상 Stack보다 큰 메모리 공간을 갖는다", "Stack은 자동으로 관리되지만, Heap은 명시적으로 메모리를 해제해야 한다"]'::jsonb,
                           'Java 메모리 구조에서 Stack과 Heap은 서로 다른 목적과 특성을 가집니다. Stack 메모리는 1) 각 스레드마다 하나씩 할당됩니다. 2) 주로 메소드 호출 정보(스택 프레임), 지역 변수, 부분 결과, 메소드 매개변수 등을 저장합니다. 3) 메소드 호출이 완료되면 해당 프레임이 자동으로 제거됩니다(LIFO 구조). 4) 크기가 제한적이며, 초과하면 StackOverflowError가 발생합니다. Heap 메모리는 1) 모든 스레드가 공유하는 메모리 영역입니다. 2) 객체(인스턴스)와 배열이 저장됩니다. 3) 가비지 컬렉션에 의해 관리되며, 더 이상 참조되지 않는 객체는 자동으로 메모리가 해제됩니다. 4) JVM 시작 시 생성되고, 필요에 따라 크기가 조정될 수 있습니다. 5) 메모리가 부족하면 OutOfMemoryError가 발생합니다.',
                           NULL),

                          ('다음 코드의 출력 결과는?',
                           'Child',
                           '["Parent", "Child", "컴파일 에러", "런타임 에러"]'::jsonb,
                           '이 코드는 변수의 정적 타입과 동적 타입에 관한 개념을 보여줍니다. 정적 타입(컴파일 타임에 결정되는 타입)에 따라 어떤 메소드를 호출할 수 있는지 결정되고, 동적 타입(런타임에 결정되는 실제 객체 타입)에 따라 오버라이딩된 메소드 중 어떤 것이 실행될지 결정됩니다. 이 예제에서 p는 Parent 타입 변수이므로 정적 타입은 Parent입니다. 그러나 실제로 가리키는 객체는 Child 타입이므로 동적 타입은 Child입니다. test() 메소드는 Parent 클래스에 정의되어 있으므로 호출 가능하며, 이 메소드는 Child 클래스에서 오버라이딩되었습니다. 메소드가 호출되면 동적 타입에 따라 Child 클래스의 메소드가 실행되고 "Child"가 출력됩니다. 반면, childOnlyMethod()는 Child 클래스에만 정의되어 있고 Parent 클래스에는 없으므로, Parent 타입 변수로는 컴파일 단계에서 호출할 수 없습니다.',
                           'class Parent {\n    public void test() {\n        System.out.println("Parent");\n    }\n}\n\nclass Child extends Parent {\n    @Override\n    public void test() {\n        System.out.println("Child");\n    }\n    \n    public void childOnlyMethod() {\n        System.out.println("Child only");\n    }\n}\n\npublic class Test {\n    public static void main(String[] args) {\n        Parent p = new Child();\n        p.test();\n        // p.childOnlyMethod(); // 컴파일 에러 발생\n    }\n}'),

                          ('자바의 Record, Sealed Classes, Pattern Matching의 공통된 목적은?',
                           '데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 것',
                           '["성능 최적화", "다형성 제한", "데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 것", "함수형 프로그래밍 패러다임으로의 전환"]'::jsonb,
                           'Record, Sealed Classes, Pattern Matching은 최근 Java에 추가된 기능들로, 모두 데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 공통 목적을 가지고 있습니다. 1) Record는 불변 데이터 객체를 간결하게 정의할 수 있게 해줍니다. 2) Sealed Classes는 클래스 계층을 명시적으로 제한하여 타입 안전성을 높입니다. 3) Pattern Matching은 데이터 구조를 분해하고 조건에 따라 처리하는 코드를 간결하게 작성할 수 있게 해줍니다. 이 세 기능을 함께 사용하면 대수적 데이터 타입(Algebraic Data Types)과 유사한 패턴을 자바에서도 안전하고 표현력 있게 구현할 수 있어, 함수형 언어에서 흔히 볼 수 있는 데이터 중심 프로그래밍 스타일을 지원합니다.',
                           NULL),

                          ('자바에서 ThreadLocal의 주요 사용 사례는?',
                           '스레드 안전성을 유지하면서 스레드별로 독립적인 상태를 관리해야 할 때',
                           '["단일 스레드 애플리케이션의 성능 향상", "스레드 안전성을 유지하면서 스레드별로 독립적인 상태를 관리해야 할 때", "여러 스레드 간 데이터 공유를 위해", "스레드 풀을 효율적으로 관리하기 위해"]'::jsonb,
                           'ThreadLocal은 각 스레드가 자신만의 독립적인 변수 사본을 가질 수 있게 해주는 클래스입니다. 주요 사용 사례로는 1) 사용자 인증 정보: 웹 애플리케이션에서 요청을 처리하는 스레드에 사용자 인증 정보를 저장합니다. 2) 트랜잭션 컨텍스트: 스프링과 같은 프레임워크에서 트랜잭션 관리를 위해 현재 트랜잭션 정보를 스레드에 바인딩합니다. 3) 날짜 포맷터: SimpleDateFormat과 같은 스레드 불안전 객체를 스레드마다 독립적으로 생성합니다. 4) 스레드 단위 캐싱: 특정 연산의 결과를 스레드별로 캐싱합니다. ThreadLocal은 스레드 안전성 문제 없이 스레드별 상태를 관리할 수 있어 유용하지만, 스레드 풀 환경에서는 ThreadLocal 변수가 재사용되는 스레드에 그대로 남아 메모리 누수나 예상치 못한 동작을 일으킬 수 있으므로, 사용 후 반드시 remove() 메소드로 값을 제거해야 합니다.',
                           NULL),

                          ('다음 코드에서 발생할 수 있는 문제점은?',
                           'HashMap이 동시에 수정될 때 ConcurrentModificationException이 발생하거나 무한 루프에 빠질 수 있다',
                           '["NullPointerException이 발생한다", "ClassCastException이 발생한다", "HashMap이 동시에 수정될 때 ConcurrentModificationException이 발생하거나 무한 루프에 빠질 수 있다", "메모리 누수가 발생한다"]'::jsonb,
                           '이 코드는 멀티스레드 환경에서 HashMap을 안전하지 않게 사용하고 있습니다. HashMap은 스레드 안전하지 않은 컬렉션으로, 여러 스레드가 동시에 수정하면 문제가 발생할 수 있습니다. 가능한 문제점으로는 1) ConcurrentModificationException: 한 스레드가 반복하는 동안 다른 스레드가 맵을 수정하면 발생합니다. 2) 데이터 손실: 동시에 put 작업이 발생하면 일부 데이터가 손실될 수 있습니다. 3) 무한 루프: 내부 해시 테이블 구조가 손상되면 무한 루프에 빠질 수 있습니다. 4) 불일치 상태: 맵이 일시적으로 불일치 상태에 있을 수 있습니다. 이러한 문제를 해결하기 위해서는 ConcurrentHashMap을 사용하거나, Collections.synchronizedMap()으로 맵을 래핑하거나, 명시적인 동기화를 사용해야 합니다.',
                           'public class SharedMapTest {\n    private static Map<String, String> map = new HashMap<>();\n    \n    public static void main(String[] args) {\n        // 여러 스레드에서 맵에 접근\n        Thread t1 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                map.put("key" + i, "value" + i);\n            }\n        });\n        \n        Thread t2 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                map.put("key" + i, "thread2value" + i);\n            }\n        });\n        \n        Thread t3 = new Thread(() -> {\n            for(String key : map.keySet()) {\n                System.out.println(map.get(key));\n            }\n        });\n        \n        t1.start();\n        t2.start();\n        t3.start();\n    }\n}'),

                          ('자바에서 Annotation Processing의 주요 용도는?',
                           '컴파일 타임에 소스 코드를 분석하고 생성하여 보일러플레이트 코드를 줄이는 데 사용된다',
                           '["런타임에 메소드 호출을 가로채는 데 사용된다", "컴파일 타임에 소스 코드를 분석하고 생성하여 보일러플레이트 코드를 줄이는 데 사용된다", "JVM 최적화를 위해 사용된다", "바이트코드를 직접 수정하는 데 사용된다"]'::jsonb,
                           'Annotation Processing은 컴파일 타임에 어노테이션이 달린 요소를 처리하는 기법으로, 주로 소스 코드 분석과 생성에 사용됩니다. 주요 용도로는 1) 보일러플레이트 코드 생성: Lombok 라이브러리는 @Getter, @Setter 같은 어노테이션으로 getter/setter 메소드를 자동 생성합니다. 2) 메타데이터 처리: JPA의 @Entity 어노테이션은 데이터베이스 매핑 정보를 정의합니다. 3) 컴파일 타임 검증: 컴파일 시점에 코드의 정확성을 검증합니다. 4) 문서 생성: Javadoc처럼 어노테이션 기반으로 문서를 생성할 수 있습니다. 대표적인 예로 Dagger, Butterknife, Room, MapStruct 등의 라이브러리가 있으며, 이들은 모두 컴파일 타임에 코드를 생성하여 런타임 성능에 영향을 주지 않고 개발자의 생산성을 향상시킵니다. Annotation Processing은 javax.annotation.processing 패키지를 사용하여 구현됩니다.',
                           NULL),

                          ('자바의 GraalVM Native Image와 전통적인 JVM 실행 방식의 주요 차이점은?',
                           'Native Image는 애플리케이션을 기계어로 사전 컴파일하여 시작 시간과 메모리 사용량을 줄이는 반면, 동적 최적화 기회는 줄어든다',
                           '["Native Image는 항상 JVM보다 실행 속도가 빠르다", "Native Image는 애플리케이션을 기계어로 사전 컴파일하여 시작 시간과 메모리 사용량을 줄이는 반면, 동적 최적화 기회는 줄어든다", "Native Image는 JVM과 동일한 방식으로 동작하지만 C로 재작성되었다", "Native Image는 인터프리터 방식으로만 실행된다"]'::jsonb,
                           'GraalVM Native Image와 전통적인 JVM 실행 방식은 근본적으로 다른 접근법을 사용합니다. 전통적인 JVM 방식에서는 1) 자바 소스 코드가 바이트코드로 컴파일되고, 2) JVM이 실행 시점에 이를 해석하고 JIT(Just-In-Time) 컴파일러로 기계어로 변환합니다. 반면 GraalVM Native Image는 1) 애플리케이션과 그 의존성을 미리(ahead-of-time) 분석하고, 2) 실행 가능한 네이티브 바이너리로 컴파일합니다. 주요 차이점으로는 1) 시작 시간: Native Image는 JVM 초기화 과정이 없어 시작이 매우 빠릅니다. 2) 메모리 사용량: Native Image는 JVM보다 훨씬 적은 메모리를 사용합니다. 3) 최적화: JVM은 런타임에 동적 최적화를 수행할 수 있지만, Native Image는 정적 최적화에 의존합니다. 4) 리플렉션 및 동적 기능: Native Image는 리플렉션, JNI, 동적 클래스 로딩 등의 사용을 제한하거나 추가 설정이 필요합니다. 5) 배포: Native Image는 독립 실행 파일로 배포되어 JVM 설치가 필요 없습니다. Native Image는 마이크로서비스, CLI 도구, 서버리스 함수와 같이 빠른 시작과 적은 메모리 사용이 중요한 애플리케이션에 적합합니다.',
                           NULL)
                  ) AS questions(question, answer, options, explanation, code)
     ),
-- 스프링 프레임워크 면접 질문
     spring_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             WITH quiz_ids AS (
                 SELECT id FROM java_quizzes WHERE title LIKE '%스프링%'
             )
             SELECT
                 NOW() - (random() * INTERVAL '60 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 questions.question,
                 'MULTIPLE_CHOICE',
                 CASE
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'BEGINNER') THEN 'BEGINNER'
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'INTERMEDIATE') THEN 'INTERMEDIATE'
                     ELSE 'ADVANCED'
                     END,
                 questions.answer,
                 questions.options,
                 questions.explanation,
                 CASE
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'BEGINNER') THEN 5
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'INTERMEDIATE') THEN 10
                     ELSE 15
                     END,
                 CASE
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'BEGINNER') THEN 45
                     WHEN quiz_id IN (SELECT id FROM java_quizzes WHERE difficulty_level = 'INTERMEDIATE') THEN 60
                     ELSE 75
                     END,
                 quiz_id,
                 questions.code
             FROM (
                      SELECT *,
                             (SELECT id FROM quiz_ids ORDER BY random() LIMIT 1) as quiz_id
                      FROM (
                               VALUES
                                   ('스프링 프레임워크의 핵심 개념인 IoC(Inversion of Control)란 무엇인가?',
                                    '객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것',
                                    '["MVC 패턴을 구현하는 방법", "객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것", "데이터베이스 연결을 자동화하는 기술", "비동기 처리를 간소화하는 기법"]'::jsonb,
                                    'IoC(Inversion of Control, 제어의 역전)는 스프링의 핵심 개념으로, 전통적인 프로그래밍에서 개발자가 직접 객체를 생성하고 의존 관계를 설정하던 제어 흐름을 역전시켜 프레임워크가 이를 대신 담당하는 것을 의미합니다. 스프링에서는 IoC 컨테이너가 빈(Bean)을 생성, 관리하고 필요한 곳에 주입합니다. 이를 통해 객체 간의 결합도를 낮추고, 코드의 재사용성과 테스트 용이성을 높이며, 객체 지향 설계 원칙을 더 쉽게 적용할 수 있습니다.',
                                    NULL),

                                   ('스프링에서 의존성 주입(Dependency Injection)의 방법이 아닌 것은?',
                                    '메소드 오버라이딩 주입',
                                    '["생성자 주입(Constructor Injection)", "세터 주입(Setter Injection)", "필드 주입(Field Injection)", "메소드 오버라이딩 주입"]'::jsonb,
                                    '스프링에서 의존성 주입(DI)은 주로 세 가지 방법으로 이루어집니다: 1) 생성자 주입(Constructor Injection): 생성자를 통해 의존성을 주입받는 방식으로, 필수적인 의존성을 명확히 하고 불변성을 보장합니다. 2) 세터 주입(Setter Injection): setter 메소드를 통해 의존성을 주입받는 방식으로, 선택적인 의존성이나 런타임에 의존성을 변경해야 할 때 유용합니다. 3) 필드 주입(Field Injection): @Autowired 어노테이션을 필드에 직접 사용하는 방식으로, 코드는 간결하지만 테스트하기 어렵고 순환 의존성을 감지하기 어려워 권장되지 않습니다. "메소드 오버라이딩 주입"은 존재하지 않는 의존성 주입 방법입니다.',
                                    NULL),

                                   ('스프링 빈(Bean)의 기본 스코프는?',
                                    'singleton',
                                    '["singleton", "prototype", "request", "session"]'::jsonb,
                                    '스프링 빈의 기본 스코프는 singleton입니다. 이는 스프링 IoC 컨테이너당 하나의 인스턴스만 생성되어 공유됨을 의미합니다. 다른 스코프로는 1) prototype: 요청할 때마다 새로운 인스턴스 생성 2) request: HTTP 요청마다 새로운 인스턴스 생성 3) session: HTTP 세션마다 새로운 인스턴스 생성 4) application: ServletContext 생명주기 동안 하나의 인스턴스 5) websocket: WebSocket 생명주기 동안 하나의 인스턴스 등이 있습니다. 대부분의 경우 상태를 유지하지 않는 서비스 객체는 싱글톤으로 관리하는 것이 메모리 효율성과 성능 면에서 유리합니다.',
                                    NULL),

                                   ('스프링 MVC에서 @Controller와 @RestController의 차이점은?',
                                    '@Controller는 주로 뷰를 반환하고, @RestController는 데이터(JSON/XML)를 직접 반환한다',
                                    '["@Controller는 싱글톤이고, @RestController는 프로토타입이다", "@Controller는 주로 뷰를 반환하고, @RestController는 데이터(JSON/XML)를 직접 반환한다", "@Controller는 스프링 3 이상에서만 사용 가능하고, @RestController는 스프링 4 이상에서만 사용 가능하다", "@Controller는 동기 방식만 지원하고, @RestController는 비동기 방식도 지원한다"]'::jsonb,
                                    '@Controller와 @RestController의 주요 차이점은 반환 값의 처리 방식입니다. @Controller는 전통적인 스프링 MVC 컨트롤러로, 주로 뷰 이름을 반환하고 ViewResolver를 통해 해당 뷰를 찾아 렌더링합니다. 메소드에 @ResponseBody를 추가하면 뷰 대신 데이터를 직접 반환할 수도 있습니다. @RestController는 @Controller + @ResponseBody의 조합으로, 모든 메소드가 기본적으로 데이터를 직접 HTTP 응답 본문으로 반환합니다. 주로 RESTful 웹 서비스에서 JSON이나 XML 형태의 데이터를 반환할 때 사용됩니다.',
                                    NULL),

                                   ('스프링에서 AOP(Aspect-Oriented Programming)의 주요 용도는?',
                                    '트랜잭션 관리, 로깅, 보안과 같은 횡단 관심사를 모듈화하는 것',
                                    '["객체 생성을 자동화하는 것", "데이터베이스 연결을 관리하는 것", "트랜잭션 관리, 로깅, 보안과 같은 횡단 관심사를 모듈화하는 것", "MVC 패턴을 구현하는 것"]'::jsonb,
                                    'AOP(Aspect-Oriented Programming, 관점 지향 프로그래밍)는 OOP를 보완하는 프로그래밍 패러다임으로, 애플리케이션 전반에 걸쳐 나타나는 횡단 관심사(cross-cutting concerns)를 모듈화하는 방법을 제공합니다. 스프링에서 AOP의 주요 용도로는 1) 트랜잭션 관리: @Transactional 어노테이션을 통해 메소드 실행 전후로 트랜잭션을 시작하고 커밋/롤백합니다. 2) 로깅: 메소드 호출과 리턴 값을 로깅합니다. 3) 보안: 메소드 실행 전에 권한을 확인합니다. 4) 캐싱: 메소드 결과를 캐시하고 재사용합니다. 5) 에러 처리: 예외 발생 시 일관된 방식으로 처리합니다. AOP를 사용하면 이러한 관심사를 비즈니스 로직에서 분리하여 코드의 재사용성과 모듈성을 높일 수 있습니다.',
                                    NULL),

                                   ('스프링 부트(Spring Boot)의 주요 장점이 아닌 것은?',
                                    '복잡한 XML 설정이 필요하다',
                                    '["내장 서버를 제공하여 별도의 웹 서버 설정이 필요 없다", "자동 설정(Auto Configuration)을 통해 설정을 간소화한다", "스타터 의존성으로 라이브러리 관리가 쉽다", "복잡한 XML 설정이 필요하다"]'::jsonb,
                                    '스프링 부트는 기존 스프링 프레임워크의 복잡성을 해소하고 더 빠른 개발을 가능하게 하는 프로젝트입니다. 주요 장점으로는 1) 내장 서버(Tomcat, Jetty, Undertow 등)를 제공하여 독립 실행 가능한 애플리케이션을 만들 수 있습니다. 2) 자동 설정(Auto Configuration)을 통해 classpath와 설정에 기반하여 필요한 빈들을 자동으로 구성합니다. 3) 스타터 의존성(Spring Boot Starters)으로 필요한 라이브러리를 쉽게 추가할 수 있습니다. 4) 프로덕션 준비 기능(actuator, metrics, health checks 등)을 제공합니다. 5) XML 설정 없이 Java 기반 설정이나 properties/YAML 파일로 간단하게 설정할 수 있습니다. "복잡한 XML 설정이 필요하다"는 스프링 부트의 장점이 아니라 오히려 스프링 부트가 해결하고자 하는 기존 스프링의 단점입니다.',
                                    NULL),

                                   ('스프링에서 @Transactional 어노테이션의 전파 속성(propagation) 중 기본값은?',
                                    'REQUIRED',
                                    '["REQUIRED", "REQUIRES_NEW", "SUPPORTS", "MANDATORY"]'::jsonb,
                                    '@Transactional 어노테이션의 전파 속성(propagation)은 트랜잭션 경계에서 이미 진행 중인 트랜잭션이 있을 때 어떻게 동작할지 결정합니다. 기본값은 REQUIRED로, 현재 진행 중인 트랜잭션이 있으면 그 트랜잭션을 사용하고, 없으면 새 트랜잭션을 시작합니다. 다른 속성으로는 1) REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하고, 진행 중이던 트랜잭션은 일시 중단합니다. 2) SUPPORTS: 진행 중인 트랜잭션이 있으면 참여하고, 없으면
                    트랜잭션 없이 실행합니다. 3) MANDATORY: 진행 중인 트랜잭션이 반드시 있어야 하며, 없으면 예외가 발생합니다. 4) NEVER: 트랜잭션 없이 실행되어야 하며, 진행 중인 트랜잭션이 있으면 예외가 발생합니다. 5) NOT_SUPPORTED: 트랜잭션 없이 실행하며, 진행 중인 트랜잭션이 있으면 일시 중단합니다. 6) NESTED: 진행 중인 트랜잭션이 있으면 중첩 트랜잭션을 생성하고, 없으면 REQUIRED처럼 동작합니다.',
                                    NULL),

                                   ('스프링 시큐리티(Spring Security)의 인증(Authentication)과 인가(Authorization)의 차이점은?',
                                    '인증은 사용자가 누구인지 확인하는 과정이고, 인가는 인증된 사용자가 특정 자원에 접근할 권한이 있는지 확인하는 과정이다',
                                    '["인증은 스프링 전용 기능이고, 인가는 Java EE 표준 기능이다", "인증은 사용자가 누구인지 확인하는 과정이고, 인가는 인증된 사용자가 특정 자원에 접근할 권한이 있는지 확인하는 과정이다", "인증은 정적 리소스에 대한 접근을 관리하고, 인가는 동적 리소스에 대한 접근을 관리한다", "인증은 서버 측에서만 수행되고, 인가는 클라이언트 측에서도 수행될 수 있다"]'::jsonb,
                                    '스프링 시큐리티에서 인증(Authentication)과 인가(Authorization)는 보안의 두 가지 핵심 개념입니다. 인증은 "당신이 누구인지 증명하는 과정"으로, 사용자의 신원을 확인합니다. 일반적으로 사용자 이름과 비밀번호, 토큰, 인증서 등을 통해 이루어집니다. 인가는 "당신이 무엇을 할 수 있는지 결정하는 과정"으로, 인증된 사용자가 특정 리소스에 접근하거나 작업을 수행할 권한이 있는지 확인합니다. 스프링 시큐리티에서는 인증은 AuthenticationManager를 통해 처리되며, 인가는 AccessDecisionManager를 통해 처리됩니다. 간단히 말해, 인증은 로그인 과정이고, 인가는 로그인 후 특정 페이지나 기능에 접근할 수 있는지 결정하는 과정입니다.',
                                    NULL),

                                   ('스프링에서 JPA와 Hibernate의 관계는?',
                                    'JPA는 자바 ORM 표준 스펙이고, Hibernate는 JPA의 구현체 중 하나이다',
                                    '["JPA와 Hibernate는 동일한 것이다", "JPA는 Hibernate의 발전된 버전이다", "JPA는 자바 ORM 표준 스펙이고, Hibernate는 JPA의 구현체 중 하나이다", "Hibernate는 JPA의 상위 개념이다"]'::jsonb,
                                    'JPA(Java Persistence API)는 자바 애플리케이션에서 관계형 데이터베이스를 사용하는 방식을 정의한 인터페이스 모음인 자바 ORM 표준 스펙입니다. JPA는 객체와 테이블 간의 매핑, 엔티티의 생명주기 관리, JPQL(객체 지향 쿼리 언어) 등을 정의합니다. Hibernate는 JPA 명세의 구현체 중 하나로, 가장 널리 사용되는 ORM 프레임워크입니다. Hibernate는 JPA의 모든 기능을 구현할 뿐 아니라, JPA 표준을 넘어서는 추가 기능(예: 독자적인 HQL, 두 번째 수준 캐시 등)도 제공합니다. 스프링에서는 Spring Data JPA를 통해 JPA를 더 쉽게 사용할 수 있으며, 기본적으로 Hibernate를 JPA 구현체로 사용합니다.',
                                    NULL),

                                   ('스프링에서 @Autowired 어노테이션을 사용할 때, 동일한 타입의 빈이 여러 개 있을 경우 해결 방법이 아닌 것은?',
                                    '두 빈 모두 사용하기 위해 @MixedAutowired 어노테이션 사용',
                                    '["@Primary 어노테이션으로 기본 빈 지정", "@Qualifier 어노테이션으로 특정 빈 선택", "이름으로 자동 매칭", "두 빈 모두 사용하기 위해 @MixedAutowired 어노테이션 사용"]'::jsonb,
                                    '스프링에서 @Autowired를 사용할 때 동일한 타입의 빈이 여러 개 있으면 "NoUniqueBeanDefinitionException" 예외가 발생할 수 있습니다. 이를 해결하는 방법으로는 1) @Primary: 동일 타입의 빈 중 하나에 @Primary를 지정하여 우선적으로 주입되게 합니다. 2) @Qualifier: 주입 지점에서 @Qualifier("빈이름")으로 특정 빈을 명시적으로 선택합니다. 3) 필드/메소드 이름으로 매칭: 필드나 메소드 매개변수 이름을 빈의 이름과 일치시켜 자동으로 매칭되게 합니다. 4) 컬렉션 주입: List<Interface>나 Map<String, Interface> 타입으로 주입받아 모든 구현체를 사용합니다. "@MixedAutowired"는 존재하지 않는 어노테이션입니다.',
                                    NULL),

                                   ('스프링 부트에서 외부 설정 값을 가져오는 방법이 아닌 것은?',
                                    '@ConfigurationValue 어노테이션 사용',
                                    '["application.properties 또는 application.yml 파일 사용", "@Value 어노테이션으로 프로퍼티 값 주입", "@ConfigurationProperties 어노테이션으로 프로퍼티 클래스 바인딩", "@ConfigurationValue 어노테이션 사용"]'::jsonb,
                                    '스프링 부트에서 외부 설정을 가져오는 일반적인 방법으로는 1) application.properties 또는 application.yml 파일을 사용하여 설정 값을 정의합니다. 2) @Value 어노테이션으로 개별 프로퍼티 값을 주입합니다(예: @Value("${app.name}")). 3) @ConfigurationProperties 어노테이션을 사용하여 프로퍼티 그룹을 자바 클래스에 바인딩합니다. 4) Environment 객체를 통해 프로그래밍 방식으로 프로퍼티에 접근합니다. 5) 명령행 인자, 환경 변수, OS 환경변수 등 다양한 소스에서 설정을 가져올 수 있습니다. "@ConfigurationValue"는 존재하지 않는 어노테이션입니다.',
                                    NULL),

                                   ('스프링에서 싱글톤 빈이 상태를 가질 때 발생할 수 있는 문제는?',
                                    '멀티스레드 환경에서 동시성 문제가 발생할 수 있다',
                                    '["빈 생성 시간이 길어진다", "메모리 사용량이 증가한다", "멀티스레드 환경에서 동시성 문제가 발생할 수 있다", "다른 빈과 결합도가 높아진다"]'::jsonb,
                                    '스프링의 기본 스코프인 싱글톤 빈이 상태를 가질 때(즉, 인스턴스 변수를 사용할 때) 발생할 수 있는 주요 문제는 멀티스레드 환경에서의 동시성 문제입니다. 싱글톤 빈은 애플리케이션 컨텍스트에서 하나의 인스턴스만 생성되어 모든 요청이 이를 공유하기 때문에, 여러 스레드가 동시에 같은 인스턴스 변수에 접근하면 경쟁 상태(race condition), 데이터 불일치, 예측 불가능한 동작 등이 발생할 수 있습니다. 이러한 문제를 해결하기 위해서는 1) 싱글톤 빈을 상태가 없는(stateless) 방식으로 설계하고, 상태가 필요한 경우 메소드 지역 변수를 사용합니다. 2) 꼭 상태가 필요하다면 스레드 로컬 변수(ThreadLocal)를 사용하거나 동기화 메커니즘을 적용합니다. 3) prototype, request, session 등 다른 스코프를 사용하여 각 요청이나 세션마다 별도의 인스턴스를 사용하도록 합니다.',
                                    NULL),

                                   ('스프링 부트 애플리케이션을 프로덕션 환경에 배포할 때 고려해야 할 사항이 아닌 것은?',
                                    '모든 로깅을 비활성화하여 성능 향상',
                                    '["적절한 프로파일 설정", "보안 설정 강화", "모니터링 및 헬스 체크 설정", "모든 로깅을 비활성화하여 성능 향상"]'::jsonb,
                                    '스프링 부트 애플리케이션을 프로덕션 환경에 배포할 때 고려해야 할 주요 사항으로는 1) 프로파일 관리: 개발, 테스트, 프로덕션 환경에 맞는 설정을 프로파일로 분리하여 관리합니다. 2) 보안 설정: HTTPS 적용, 민감한 정보(비밀번호, API 키 등) 암호화, 적절한 인증/인가 설정을 합니다. 3) 모니터링 및 관찰성: 액추에이터(Actuator) 엔드포인트를 활용하여 애플리케이션 상태, 메트릭, 헬스 체크를 설정하고, 모니터링 도구와 통합합니다. 4) 로깅 설정: 적절한 로그 레벨 설정과 로그 관리 전략이 필요합니다. 5) 성능 최적화: JVM 옵션, 캐싱, 커넥션 풀 설정 등을 통해 성능을 최적화합니다. 6) 무중단 배포 전략: 블루-그린 배포, 카나리 배포 등을 고려합니다. "모든 로깅을 비활성화"하는 것은 문제 발생 시 진단을 어렵게 만들어 좋은 방법이 아닙니다. 대신 필요한 정보만 적절한 레벨로 로깅하는 것이 중요합니다.',
                                    NULL),

                                   ('스프링에서 @RequestBody와 @ResponseBody 어노테이션의 역할은?',
                                    '@RequestBody는 HTTP 요청 본문을 자바 객체로 변환하고, @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환한다',
                                    '["@RequestBody는 URL 파라미터를 자바 객체로 변환하고, @ResponseBody는 자바 객체를 JSON으로 변환한다", "@RequestBody는 HTTP 요청 본문을 자바 객체로 변환하고, @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환한다", "@RequestBody는 HTTP 요청을 검증하고, @ResponseBody는 HTTP 응답을 압축한다", "@RequestBody와 @ResponseBody는 모두 RESTful 서비스에서만 사용할 수 있다"]'::jsonb,
                                    '@RequestBody와 @ResponseBody는 HTTP 메시지 변환과 관련된 스프링 MVC 어노테이션입니다. @RequestBody는 HTTP 요청 본문(body)을 자바 객체로 변환합니다. 주로 POST나 PUT 요청에서 JSON이나 XML 같은 데이터를 자바 객체로 역직렬화할 때 사용합니다. 내부적으로 HttpMessageConverter를 사용하여 변환이 이루어집니다. @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환합니다. 컨트롤러 메소드의 반환 값을 뷰를 통해 렌더링하지 않고, 직접 HTTP 응답 본문으로 변환하여 클라이언트에게 전송합니다. 마찬가지로 HttpMessageConverter를 사용하여 객체를 JSON, XML 등으로 직렬화합니다. @RestController를 사용하면 모든 메소드에 @ResponseBody가 자동으로 적용됩니다.',
                                    NULL),

                                   ('스프링에서 DispatcherServlet의 역할은?',
                                    '프론트 컨트롤러로서 모든 웹 요청을 받아 적절한 핸들러로 분배한다',
                                    '["빈의 생명주기를 관리한다", "프론트 컨트롤러로서 모든 웹 요청을 받아 적절한 핸들러로 분배한다", "데이터베이스 연결을 관리한다", "뷰를 렌더링한다"]'::jsonb,
                                    'DispatcherServlet은 스프링 MVC의 핵심 컴포넌트로, 프론트 컨트롤러(Front Controller) 패턴을 구현합니다. 주요 역할은 1) 모든 웹 요청을 중앙에서 받아들입니다. 2) 요청을 처리할 적절한 핸들러(컨트롤러)를 찾아 요청을 위임합니다. 3) 핸들러가 반환한 결과를 적절한 뷰에 전달하거나 직접 응답을 생성합니다. 4) 예외 처리, 지역화, 테마 결정 등의 작업을 처리합니다. DispatcherServlet의 동작 흐름은 다음과 같습니다: 요청 접수 → HandlerMapping으로 핸들러 결정 → HandlerAdapter를 통해 핸들러 실행 → 핸들러가 ModelAndView 반환 → ViewResolver로 뷰 결정 → 뷰 렌더링 → 응답 반환. 이러한 아키텍처는 웹 요청 처리 과정을 모듈화하고 확장성을 높이는 데 기여합니다.',
                                    NULL),

                                   ('스프링 부트의 자동 설정(Auto-configuration) 원리는?',
                                    '@Conditional 어노테이션을 기반으로 classpath의 라이브러리, 기존 설정, 환경 등을 고려하여 자동으로 빈을 구성한다',
                                    '["모든 가능한 빈을 무조건 생성한 후 필요 없는 것을 제거한다", "@Conditional 어노테이션을 기반으로 classpath의 라이브러리, 기존 설정, 환경 등을 고려하여 자동으로 빈을 구성한다", "사용자가 작성한 XML 설정 파일을 분석한다", "모든 설정을 런타임에 결정한다"]'::jsonb,
                                    '스프링 부트의 자동 설정(Auto-configuration)은 개발자가 최소한의 설정으로 애플리케이션을 실행할 수 있도록 하는 핵심 기능입니다. 주요 원리는 다음과 같습니다: 1) @SpringBootApplication 어노테이션에 포함된 @EnableAutoConfiguration이 자동 설정을 활성화합니다. 2) 스프링 부트는 classpath에 있는 spring.factories 파일에서 AutoConfiguration 클래스 목록을 로드합니다. 3) 각 AutoConfiguration 클래스는 @Conditional 계열 어노테이션(@ConditionalOnClass, @ConditionalOnBean, @ConditionalOnProperty 등)을 사용하여 특정 조건이 충족될 때만 설정이 적용되도록 합니다. 4) 조건에는 특정 클래스의 존재 여부, 특정 빈의 존재 여부, 특정 프로퍼티 값 등이 포함됩니다. 5) 사용자가 명시적으로 빈을 정의하면 자동 설정보다 우선 적용됩니다. 이 방식을 통해 스프링 부트는 개발자가 필요한 라이브러리만 추가하면, 해당 라이브러리가 동작하는 데 필요한 빈들을 자동으로 구성해줍니다.',
                                    NULL),

                                   ('스프링에서 Bean Validation API(@Valid, @NotNull 등)를 사용하는 위치로 적절하지 않은 것은?',
                                    'private 메소드 매개변수 검증',
                                    '["컨트롤러 메소드의 @RequestBody 매개변수 검증", "폼 제출 데이터 검증", "JPA 엔티티 속성 검증", "private 메소드 매개변수 검증"]'::jsonb,
                                    'Bean Validation API는 자바 빈 검증을 위한 표준 API로, 스프링에서는 주로 다음 위치에서 사용됩니다: 1) 컨트롤러 메소드의 매개변수 검증: @Valid/@Validated를 @RequestBody, @ModelAttribute 등과 함께 사용하여 입력 데이터를 검증합니다. 2) 폼 제출 데이터 검증: 웹 폼에서 제출된 데이터를 검증합니다. 3) JPA 엔티티 속성 검증: 엔티티가 저장되기 전에 속성을 검증합니다. 그러나 private 메소드 매개변수 검증은 Bean Validation API로 직접 지원되지 않습니다. Bean Validation은 기본적으로 public 메소드와 필드를 대상으로 동작하며, 메소드 검증을 위해서는 @Validated 어노테이션이 적용된 클래스의 public 메소드에만 적용됩니다. private 메소드 매개변수 검증은 개발자가 직접 코드로 구현해야 합니다.',
                                    NULL),

                                   ('스프링의 @Transactional 어노테이션이 동작하지 않을 수 있는 경우는?',
                                    '같은 클래스 내에서 @Transactional 메소드 호출',
                                    '["public 메소드에 적용한 경우", "외부에서 해당 메소드를 호출한 경우", "같은 클래스 내에서 @Transactional 메소드 호출", "DataSource가 제대로 설정된 경우"]'::jsonb,
                                    '@Transactional 어노테이션이 동작하지 않을 수 있는 주요 경우들은 다음과 같습니다: 1) 같은 클래스 내의 메소드 호출: 스프링의 트랜잭션은 프록시 기반으로 동작하기 때문에, 같은 클래스 내에서 @Transactional 메소드를 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않습니다. 2) private, protected, 또는 default 가시성 메소드: @Transactional은 public 메소드에만 기본적으로 적용됩니다. 3) 런타임 예외가 아닌 체크 예외 발생: 기본적으로 런타임 예외(unchecked exception)에서만 롤백이 발생합니다. 4) 트랜잭션 관리자 미설정: 적절한 PlatformTransactionManager가 설정되지 않은 경우. 5) @EnableTransactionManagement 미설정: XML 설정이나 자바 설정에서 트랜잭션 관리를 활성화하지 않은 경우. 이러한 문제를 해결하기 위해서는 별도의 서비스 클래스로 분리하거나, 자기 주입(self-injection), 또는 AopContext.currentProxy()를 사용하는 방법 등이 있습니다.',
                                    NULL),

                                   ('스프링 애플리케이션 컨텍스트(Application Context)가 로드될 때의 단계가 올바른 순서로 나열된 것은?',
                                    '빈 정의 로딩 → 빈 정의 검증 → 빈 전처리 → 빈 인스턴스화 및 의존성 주입 → 초기화 콜백 호출',
                                    '["빈 인스턴스화 → 빈 정의 로딩 → 의존성 주입 → 초기화 콜백 호출", "빈 정의 로딩 → 빈 정의 검증 → 빈 전처리 → 빈 인스턴스화 및 의존성 주입 → 초기화 콜백 호출", "의존성 주입 → 빈 인스턴스화 → 초기화 콜백 호출 → 빈 정의 검증", "빈 정의 로딩 → 빈 인스턴스화 → 초기화 콜백 호출 → 의존성 주입"]'::jsonb,
                                    '스프링 애플리케이션 컨텍스트 로딩 과정은 여러 단계로 이루어집니다: 1) 빈 정의 로딩: XML, 어노테이션, Java Config 등에서 빈 정의를 읽어옵니다. 2) 빈 정의 검증: 로드된 빈 정의의 유효성을 검사합니다. 3) 빈 전처리: BeanFactoryPostProcessor를 실행하여 빈 정의를 수정할 기회를 제공합니다(예: PropertyPlaceholderConfigurer가 이 단계에서 프로퍼티 값을 해석합니다). 4) 빈 인스턴스화: 빈 인스턴스를 생성합니다. 5) 의존성 주입: 생성자, 세터, 필드 주입을 통해 의존성을 설정합니다. 6) BeanPostProcessor 적용: 빈 후처리기를 실행하여 빈 인스턴스를 수정합니다. 7) 초기화 콜백 호출: InitializingBean의 afterPropertiesSet() 메소드나 @PostConstruct, init-method 등의 초기화 메소드를 호출합니다. 8) 빈 사용 준비 완료: 이제 빈을 사용할 수 있습니다. 컨텍스트가 종료될 때는 소멸 전 콜백(@PreDestroy, DisposableBean의 destroy() 등)이 호출됩니다.',
                                    NULL),

                                   ('스프링 부트 액추에이터(Spring Boot Actuator)의 주요 기능이 아닌 것은?',
                                    '자동으로 데이터베이스 스키마 생성',
                                    '["애플리케이션 상태 및 헬스 체크", "메트릭 수집 및 모니터링", "자동으로 데이터베이스 스키마 생성", "환경 정보 및 구성 속성 조회"]'::jsonb,
                                    '스프링 부트 액추에이터(Spring Boot Actuator)는 프로덕션 환경에서 애플리케이션을 모니터링하고 관리하기 위한 기능을 제공합니다. 주요 기능으로는 1) 애플리케이션 상태 및 헬스 체크: /actuator/health 엔드포인트를 통해 애플리케이션의 상태를 확인할 수 있습니다. 2) 메트릭 수집 및 모니터링: /actuator/metrics 엔드포인트로 JVM 메모리, CPU 사용량, HTTP 요청 등의 메트릭을 제공합니다. 3) 환경 정보 및 구성 속성: /actuator/env, /actuator/configprops 등으로 현재 환경 및 설정 정보를 조회할 수 있습니다. 4) 로깅 레벨 조회 및 변경: /actuator/loggers를 통해 로그 레벨을 실시간으로 조정할 수 있습니다. 5) 스레드 덤프, 힙 덤프: 디버깅에 필요한 정보를 제공합니다. 6) HTTP 추적: 최근 HTTP 요청/응답 정보를 조회할 수 있습니다. "자동으로 데이터베이스 스키마 생성"은 액추에이터의 기능이 아니라, 스프링 부트의 데이터 관련 기능(spring.jpa.hibernate.ddl-auto 등)에 해당합니다.',
                                    NULL),

                                   ('스프링 MVC에서 @ModelAttribute 어노테이션의 용도는?',
                                    '요청 파라미터를 객체에 바인딩하거나, 모든 컨트롤러 메소드에서 사용할 모델 속성을 추가한다',
                                    '["HTTP 응답 형식을 지정한다", "컨트롤러 클래스를 모델로 등록한다", "요청 파라미터를 객체에 바인딩하거나, 모든 컨트롤러 메소드에서 사용할 모델 속성을 추가한다", "비동기 요청을 처리한다"]'::jsonb,
                                    '@ModelAttribute 어노테이션은 스프링 MVC에서 두 가지 주요 용도로 사용됩니다: 1) 메소드 매개변수로 사용: HTTP 요청 파라미터(쿼리 파라미터, 폼 데이터)를 자바 객체에 바인딩합니다. 스프링은 요청 파라미터 이름과 객체의 프로퍼티 이름을 매칭하여 자동으로 값을 설정합니다. 데이터 검증과 함께 사용되는 경우가 많습니다. 2) 메소드 레벨에 사용: 해당 컨트롤러의 모든 요청 처리 메소드가 호출되기 전에 실행되어, 반환 값을 모델에 추가합니다. 이를 통해 여러 뷰에서 공통으로 사용하는 데이터를 한 번에 모델에 추가할 수 있습니다. @ModelAttribute는 주로 폼 처리, 데이터 바인딩, 검증 등에 유용하게 사용되며, RESTful API에서는 @RequestBody가 더 일반적으로 사용됩니다.',
                                    NULL),

                                   ('스프링 부트에서 CORS(Cross-Origin Resource Sharing) 설정 방법이 아닌 것은?',
                                    '@CrossOriginDisabled 어노테이션 사용',
                                    '["WebMvcConfigurer 구현 클래스에서 addCorsMappings 메소드 오버라이드", "@CrossOrigin 어노테이션을 컨트롤러 클래스 또는 메소드에 사용", "application.properties/yml에 spring.mvc.cors.* 속성 설정", "@CrossOriginDisabled 어노테이션 사용"]'::jsonb,
                                    'CORS(Cross-Origin Resource Sharing)는 다른 출처(도메인)에서 리소스를 요청할 수 있게 허용하는 메커니즘입니다. 스프링 부트에서 CORS를 설정하는 주요 방법으로는 1) 글로벌 CORS 설정: WebMvcConfigurer 인터페이스를 구현하고 addCorsMappings 메소드를 오버라이드하여 모든 컨트롤러에 적용할 CORS 설정을 정의합니다. 2) 컨트롤러 레벨 설정: @CrossOrigin 어노테이션을 컨트롤러 클래스나 메소드에 적용하여 특정 엔드포인트에 대한 CORS 설정을 합니다. 3) 속성 기반 설정: application.properties/yml 파일에 spring.mvc.cors 관련 속성을 설정합니다. 4) CorsFilter 등록: FilterRegistrationBean을 사용하여 CorsFilter를 빈으로 등록합니다. "@CrossOriginDisabled"는 존재하지 않는 어노테이션입니다. CORS를 비활성화하려면 단순히 CORS 설정을 하지 않거나, 필요한 경우 특정 출처만 허용하도록 설정하면 됩니다.',
                                    NULL),

                                   ('스프링 WebFlux와 스프링 MVC의 주요 차이점은?',
                                    'WebFlux는 비동기, 논블로킹 I/O를 활용한 리액티브 스트림 기반 웹 프레임워크이고, MVC는 서블릿 API 기반의 동기식 웹 프레임워크이다',
                                    '["WebFlux는 XML 설정만 지원하고, MVC는 어노테이션 기반 설정을 지원한다", "WebFlux는 비동기, 논블로킹 I/O를 활용한 리액티브 스트림 기반 웹 프레임워크이고, MVC는 서블릿 API 기반의 동기식 웹 프레임워크이다", "WebFlux는 REST API만 지원하고, MVC는 웹 페이지 렌더링만 지원한다", "WebFlux는 스프링 부트 1.0부터 지원되고, MVC는 스프링 부트 2.0부터 지원된다"]'::jsonb,
                                    '스프링 WebFlux와 스프링 MVC는 스프링 프레임워크에서 제공하는 두 가지 웹 개발 스택으로, 근본적인 차이점이 있습니다. 스프링 MVC는 1) 서블릿 API 기반으로 동작하며, 동기식, 블로킹 방식의 I/O를 사용합니다. 2) 각 요청은 하나의 스레드에서 처리되며, 요청 처리 중에는 스레드가 블로킹됩니다. 3) 전통적인 명령형 프로그래밍 모델을 사용합니다. 4) 대규모 동시 연결 처리에는 많은 스레드가 필요합니다. 반면, 스프링 WebFlux는 1) 서블릿 API에 의존하지 않고, Netty, Undertow 등의 비동기 웹 서버 위에서 동작합니다. 2) 논블로킹 I/O를 활용하여 적은 수의 스레드로 많은 요청을 처리할 수 있습니다. 3) 리액티브 스트림(Reactor, RxJava)을 기반으로 하는 함수형, 선언적 프로그래밍 모델을 사용합니다. 4) 백프레셔(backpressure)를 지원하여 시스템 과부하를 방지합니다. WebFlux는 마이크로서비스, 실시간 스트리밍, 대규모 동시 연결이 필요한 애플리케이션에 적합하며, MVC는 전통적인 웹 애플리케이션에 더 적합합니다.',
                                    NULL),

                                   ('스프링 스케줄링(@Scheduled)의 fixedRate와 fixedDelay의 차이점은?',
                                    'fixedRate는 이전 실행 시작 시간으로부터 일정 시간 간격으로 실행하고, fixedDelay는 이전 실행 완료 시간으로부터 일정 시간 후에 실행한다',
                                    '["fixedRate는 고정된 횟수로 실행하고, fixedDelay는 무한히 실행한다", "fixedRate는 이전 실행 시작 시간으로부터 일정 시간 간격으로 실행하고, fixedDelay는 이전 실행 완료 시간으로부터 일정 시간 후에 실행한다", "fixedRate는 밀리초 단위로 지정하고, fixedDelay는 초 단위로 지정한다", "fixedRate는 동기적으로 실행하고, fixedDelay는 비동기적으로 실행한다"]'::jsonb,
                                    '스프링의 @Scheduled 어노테이션을 사용한 스케줄링에서 fixedRate와 fixedDelay는 태스크 실행 간격을 제어하는 방식에 차이가 있습니다. fixedRate는 이전 태스크의 시작 시간을 기준으로 일정 시간 간격으로 태스크를 실행합니다. 만약 태스크 실행 시간이 지정된 간격보다 길면, 다음 실행은 현재 태스크가 완료되는 즉시 시작됩니다. 이는 일정한 빈도로 태스크를 실행해야 할 때 유용합니다(예: 10초마다 데이터 수집). fixedDelay는 이전 태스크의 완료 시간을 기준으로 일정 시간이 지난 후에 다음 태스크를 실행합니다. 이는 이전 태스크가 완전히 처리된 후에 일정 시간 간격을 두고 다음 태스크를 실행해야 할 때 유용합니다(예: 이메일 발송 후 5분 대기). 두 방식 모두 밀리초 단위로 지정할 수 있으며, cron 표현식을 사용하여 더 복잡한 일정을 설정할 수도 있습니다.',
                                    NULL),

                                   ('스프링 부트에서 다른 모듈이나 라이브러리를 자동으로 구성할 때 사용하는 파일은?',
                                    'META-INF/spring.factories',
                                    '["application.properties", "META-INF/spring.factories", "spring-boot-dependencies.pom", "bootstrap.yml"]'::jsonb,
                                    'META-INF/spring.factories 파일은 스프링 부트의 자동 구성 메커니즘의 핵심입니다. 이 파일은 Spring Boot의 SpringFactoriesLoader가 탐색하는 특수한 프로퍼티 파일로, 다양한 스프링 부트 확장 포인트에 대한 구현 클래스를 정의합니다. 가장 흔한 사용 사례는 자동 구성 클래스를 등록하는 것입니다. 라이브러리 개발자는 이 파일에 org.springframework.boot.autoconfigure.EnableAutoConfiguration 키에 자신의 AutoConfiguration 클래스를 값으로 등록하여, 해당 라이브러리가 스프링 부트 애플리케이션에 추가될 때 자동으로 구성되도록 할 수 있습니다. 이외에도 ApplicationListener, EnvironmentPostProcessor, FailureAnalyzer 등 다양한 확장 포인트를 등록할 수 있습니다. Spring Boot 2.7부터는 @AutoConfiguration 어노테이션과 함께 spring.factories 메커니즘이 단계적으로 사라지고 있으며, 향후 스프링 부트 3.0에서는 spring.factories에서 자동 구성 클래스를 로드하는 방식이 완전히 제거될 예정입니다.',
                                    NULL)
                           ) as q(question, answer, options, explanation, code)
                  ) as questions(question, answer, options, explanation, code, quiz_id)
     ),



-- 보안 관련 문제 생성 (개수 증가)
     security_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'SQL 인젝션 공격을 방지하는 가장 좋은 방법은?'
                     WHEN 1 THEN '대칭 암호화와 비대칭 암호화의 주요 차이점은?'
                     WHEN 2 THEN 'CSRF 공격이란?'
                     WHEN 3 THEN '다음 중 가장 안전한 비밀번호 해싱 알고리즘은?'
                     WHEN 4 THEN 'HTTPS에서 사용하는 프로토콜은?'
                     WHEN 5 THEN 'XSS 공격의 방어 방법으로 적절하지 않은 것은?'
                     WHEN 6 THEN '다음 중 2단계 인증(2FA)에 해당하지 않는 것은?'
                     WHEN 7 THEN '디지털 서명의 주요 목적은?'
                     WHEN 8 THEN 'DDoS 공격의 주요 목표는?'
                     WHEN 9 THEN '다음 중 사회공학적 공격 방법이 아닌 것은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '매개변수화된 쿼리 사용'
                     WHEN 1 THEN '비대칭 암호화는 두 개의 다른 키를 사용한다'
                     WHEN 2 THEN '사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격'
                     WHEN 3 THEN 'bcrypt'
                     WHEN 4 THEN 'TLS(SSL)'
                     WHEN 5 THEN '데이터베이스 암호화'
                     WHEN 6 THEN '이메일 주소와 비밀번호 조합'
                     WHEN 7 THEN '메시지 무결성과 발신자 인증'
                     WHEN 8 THEN '서비스 가용성 침해'
                     WHEN 9 THEN '버퍼 오버플로우'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["매개변수화된 쿼리 사용", "모든 입력 필드 숨기기", "쿼리 로깅 비활성화", "데이터베이스 계정 권한 축소"]'::jsonb
                     WHEN 1 THEN '["비대칭 암호화는 두 개의 다른 키를 사용한다", "대칭 암호화가 항상 더 안전하다", "비대칭 암호화는 항상 더 빠르다", "대칭 암호화는 키가 필요 없다"]'::jsonb
                     WHEN 2 THEN '["사용자의 세션을 훔치는 공격", "사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격", "데이터베이스에서 민감한 정보를 추출하는 공격", "네트워크 트래픽을 감청하는 공격"]'::jsonb
                     WHEN 3 THEN '["MD5", "SHA-1", "bcrypt", "Base64"]'::jsonb
                     WHEN 4 THEN '["FTP", "SSH", "TLS(SSL)", "SMTP"]'::jsonb
                     WHEN 5 THEN '["입력 데이터 검증", "출력 데이터 이스케이핑", "콘텐츠 보안 정책(CSP) 사용", "데이터베이스 암호화"]'::jsonb
                     WHEN 6 THEN '["SMS 인증 코드", "생체 인식", "인증 앱의 일회용 코드", "이메일 주소와 비밀번호 조합"]'::jsonb
                     WHEN 7 THEN '["데이터 암호화", "메시지 무결성과 발신자 인증", "접근 제어", "네트워크 속도 향상"]'::jsonb
                     WHEN 8 THEN '["데이터 탈취", "서비스 가용성 침해", "권한 상승", "데이터 변조"]'::jsonb
                     WHEN 9 THEN '["피싱", "프리텍스팅", "스피어 피싱", "버퍼 오버플로우"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '매개변수화된 쿼리(Prepared Statements)는 사용자 입력을 SQL 쿼리와 분리하여 처리하므로 SQL 인젝션 공격을 방지하는 가장 효과적인 방법입니다.'
                     WHEN 1 THEN '대칭 암호화는 암호화와 복호화에 동일한 키를 사용하지만, 비대칭 암호화는 공개 키와 개인 키라는 두 개의 다른 키를 사용합니다.'
                     WHEN 2 THEN 'CSRF(Cross-Site Request Forgery)는 사용자가 인증된 상태에서 의도하지 않은 요청을 서버에 보내도록 속이는 공격입니다.'
                     WHEN 3 THEN 'bcrypt는 비밀번호 해싱을 위해 설계된 알고리즘으로, 느린 해시 함수와 솔트를 사용하여 무차별 대입 공격에 강합니다. MD5와 SHA-1은 취약하고, Base64는 인코딩이지 해싱이 아닙니다.'
                     WHEN 4 THEN 'HTTPS는 HTTP 프로토콜에 TLS(이전에는 SSL) 암호화 계층을 추가한 것입니다. TLS는 클라이언트와 서버 간의 통신을 암호화합니다.'
                     WHEN 5 THEN 'XSS(Cross-Site Scripting) 공격을 방어하기 위해서는 입력 검증, 출력 이스케이핑, CSP 적용이 필요합니다. 데이터베이스 암호화는 XSS 공격과 직접적인 관련이 없습니다.'
                     WHEN 6 THEN '이메일 주소와 비밀번호 조합은 단일 인증 요소(알고 있는 것)에 해당하며, 2단계 인증이 아닙니다. 2FA는 두 가지 독립적인 인증 요소를 사용해야 합니다.'
                     WHEN 7 THEN '디지털 서명은 메시지의 무결성을 확인하고 발신자의 신원을 인증하는 데 사용됩니다. 이를 통해 메시지가 변조되지 않았고 실제 발신자가 보냈음을 확인할 수 있습니다.'
                     WHEN 8 THEN 'DDoS(분산 서비스 거부) 공격은 다수의 시스템에서 대량의 트래픽을 생성하여 대상 시스템이나 서비스의 가용성을 침해하는 것이 주목적입니다.'
                     WHEN 9 THEN '버퍼 오버플로우는 기술적 취약점을 이용한 공격이며, 사회공학적 공격이 아닙니다. 피싱, 프리텍스팅, 스피어 피싱은 모두 인간의 심리를 이용한 사회공학적 공격입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 7 THEN
                         '// 다음 코드는 SQL 인젝션 공격의 취약점을 시뮬레이션하는 예제입니다.
                         function executeQuery(query) {
                             // 사용자 입력을 검증하지 않으면 보안 위험이 발생할 수 있습니다.
                             return database.run(query);
                         }'
                     WHEN mod(seq, 20) = 17 THEN
                         '<!-- 다음 코드의 취약점은? -->
                         <script>
                             let username = "<?php echo $_GET["username"]; ?>";
                             document.getElementById("welcome").innerHTML = "Welcome, " + username;
                         </script>'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '보안'
             )
             LIMIT 300
     ),
-- 클라우드 컴퓨팅 관련 문제 생성
     cloud_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '클라우드 서비스 모델 중 SaaS란?'
                     WHEN 1 THEN '다음 중 AWS의 스토리지 서비스는?'
                     WHEN 2 THEN '컨테이너 오케스트레이션 도구는?'
                     WHEN 3 THEN '클라우드 배포 모델 중 하이브리드 클라우드의 특징은?'
                     WHEN 4 THEN 'IaC(Infrastructure as Code) 도구가 아닌 것은?'
                     WHEN 5 THEN '마이크로서비스 아키텍처의 특징이 아닌 것은?'
                     WHEN 6 THEN '클라우드 환경에서 오토스케일링의 목적은?'
                     WHEN 7 THEN '서버리스 컴퓨팅의 장점이 아닌 것은?'
                     WHEN 8 THEN '다음 중 데이터센터 내 가상화 기술이 아닌 것은?'
                     WHEN 9 THEN '클라우드 보안을 위한 모델로 적절하지 않은 것은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '소프트웨어를 서비스로 제공'
                     WHEN 1 THEN 'S3'
                     WHEN 2 THEN 'Kubernetes'
                     WHEN 3 THEN '퍼블릭과 프라이빗 클라우드의 조합'
                     WHEN 4 THEN 'Jenkins'
                     WHEN 5 THEN '단일 대형 데이터베이스 사용'
                     WHEN 6 THEN '트래픽에 따라 자동으로 리소스 확장'
                     WHEN 7 THEN '장기 실행 작업에 비용 효율적'
                     WHEN 8 THEN 'Microservices'
                     WHEN 9 THEN '사용자 책임 모델'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["소프트웨어를 서비스로 제공", "플랫폼을 서비스로 제공", "인프라를 서비스로 제공", "데이터베이스를 서비스로 제공"]'::jsonb
                     WHEN 1 THEN '["EC2", "Lambda", "S3", "Route 53"]'::jsonb
                     WHEN 2 THEN '["Docker", "Kubernetes", "Maven", "Jenkins"]'::jsonb
                     WHEN 3 THEN '["단일 조직만 사용", "퍼블릭과 프라이빗 클라우드의 조합", "모든 리소스가 외부에 노출됨", "여러 조직이 공유함"]'::jsonb
                     WHEN 4 THEN '["Terraform", "CloudFormation", "Ansible", "Jenkins"]'::jsonb
                     WHEN 5 THEN '["서비스별 독립 배포", "서비스 간 느슨한 결합", "API를 통한 통신", "단일 대형 데이터베이스 사용"]'::jsonb
                     WHEN 6 THEN '["데이터베이스 쿼리 최적화", "코드 컴파일 시간 단축", "트래픽에 따라 자동으로 리소스 확장", "소스 코드 버전 관리"]'::jsonb
                     WHEN 7 THEN '["인프라 관리 불필요", "자동 확장성", "사용한 만큼만 비용 지불", "장기 실행 작업에 비용 효율적"]'::jsonb
                     WHEN 8 THEN '["VMware", "Hyper-V", "KVM", "Microservices"]'::jsonb
                     WHEN 9 THEN '["최소 권한의 원칙", "심층 방어", "사용자 책임 모델", "공유 책임 모델"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'SaaS(Software as a Service)는 소프트웨어를 인터넷을 통해 서비스로 제공하는 모델입니다. 사용자는 웹 브라우저를 통해 애플리케이션에 접근할 수 있습니다.'
                     WHEN 1 THEN 'S3(Simple Storage Service)는 AWS의 객체 스토리지 서비스입니다. EC2는 컴퓨팅 서비스, Lambda는 서버리스 함수 실행 서비스, Route 53은 DNS 서비스입니다.'
                     WHEN 2 THEN 'Kubernetes는 컨테이너 오케스트레이션 도구로, 컨테이너화된 애플리케이션의 배포, 확장, 관리를 자동화합니다. Docker는 컨테이너화 플랫폼이고, Maven과 Jenkins는 빌드 도구입니다.'
                     WHEN 3 THEN '하이브리드 클라우드는 퍼블릭 클라우드와 프라이빗 클라우드를 조합하여 사용하는 모델로, 조직은 두 환경 간에 데이터와 애플리케이션을 이동할 수 있습니다.'
                     WHEN 4 THEN 'Jenkins는 CI/CD 도구이며 IaC(Infrastructure as Code) 도구가 아닙니다. Terraform, CloudFormation, Ansible은 인프라를 코드로 관리하는 IaC 도구입니다.'
                     WHEN 5 THEN '마이크로서비스 아키텍처는 서비스별 독립 배포, 느슨한 결합, API 통신을 특징으로 합니다. 단일 대형 데이터베이스 사용은 모놀리식 아키텍처의 특징입니다.'
                     WHEN 6 THEN '오토스케일링은 애플리케이션의 트래픽이나 부하에 따라 컴퓨팅 리소스를 자동으로 확장하거나 축소하여 성능과 비용 효율성을 유지하는 데 목적이 있습니다.'
                     WHEN 7 THEN '서버리스 컴퓨팅은 단기적인 작업에 비용 효율적이지만, 장기 실행 작업에는 일반적으로 전통적인 VM이나 컨테이너가 더 경제적입니다.'
                     WHEN 8 THEN '마이크로서비스는 애플리케이션 아키텍처 패턴이며, 데이터센터 가상화 기술이 아닙니다. VMware, Hyper-V, KVM은 하이퍼바이저 기반 가상화 기술입니다.'
                     WHEN 9 THEN '클라우드 보안에서는 공유 책임 모델(Shared Responsibility Model)이 적용되며, 서비스 제공자와 고객이 보안 책임을 나눠 가집니다. "사용자 책임 모델"은 일반적으로 사용되지 않는 용어입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 10 THEN
                         '# 다음 코드는 무엇을 하는 코드인가?
                         resource "aws_instance" "web_server" {
                           ami           = "ami-0c55b159cbfafe1f0"
                           instance_type = "t2.micro"
                           tags = {
                             Name = "WebServer"
                           }
                         }'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '클라우드컴퓨팅'
             )
             LIMIT 300
     ),
-- 데브옵스 관련 문제 생성
     devops_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'CI/CD 파이프라인에서 CI는 무엇을 의미하는가?'
                     WHEN 1 THEN '다음 중 컨테이너 기술이 아닌 것은?'
                     WHEN 2 THEN '다음 중 Git 버전 관리 명령어가 아닌 것은?'
                     WHEN 3 THEN '다음 중 구성 관리 도구는?'
                     WHEN 4 THEN '블루-그린 배포 전략의 주요 이점은?'
                     WHEN 5 THEN '다음 중 Docker 명령어가 아닌 것은?'
                     WHEN 6 THEN 'DevOps의 주요 목표가 아닌 것은?'
                     WHEN 7 THEN '다음 중 모니터링 도구는?'
                     WHEN 8 THEN '컨테이너 오케스트레이션의 필요성이 아닌 것은?'
                     WHEN 9 THEN '다음 중 지속적 배포(CD)를 의미하는 것은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '지속적 통합'
                     WHEN 1 THEN 'Apache Tomcat'
                     WHEN 2 THEN 'git distribute'
                     WHEN 3 THEN 'Ansible'
                     WHEN 4 THEN '다운타임 최소화'
                     WHEN 5 THEN 'docker virtualize'
                     WHEN 6 THEN '모든 개발을 수동으로 테스트'
                     WHEN 7 THEN 'Prometheus'
                     WHEN 8 THEN '애플리케이션 코드 작성 간소화'
                     WHEN 9 THEN '변경사항을 자동으로 프로덕션에 배포'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["지속적 통합", "컨테이너 인터페이스", "중앙 인프라", "코드 인스펙션"]'::jsonb
                     WHEN 1 THEN '["Docker", "Kubernetes", "LXC", "Apache Tomcat"]'::jsonb
                     WHEN 2 THEN '["git commit", "git push", "git pull", "git distribute"]'::jsonb
                     WHEN 3 THEN '["Jenkins", "Maven", "Ansible", "JUnit"]'::jsonb
                     WHEN 4 THEN '["개발 비용 절감", "다운타임 최소화", "리소스 사용량 감소", "보안 강화"]'::jsonb
                     WHEN 5 THEN '["docker build", "docker run", "docker pull", "docker virtualize"]'::jsonb
                     WHEN 6 THEN '["개발과 운영 간 협업 강화", "배포 빈도 증가", "변경 실패율 감소", "모든 개발을 수동으로 테스트"]'::jsonb
                     WHEN 7 THEN '["Selenium", "Prometheus", "Maven", "Terraform"]'::jsonb
                     WHEN 8 THEN '["컨테이너 자동 확장", "컨테이너 장애 복구", "컨테이너 네트워킹 관리", "애플리케이션 코드 작성 간소화"]'::jsonb
                     WHEN 9 THEN '["코드를 정기적으로 병합", "변경사항을 자동으로 프로덕션에 배포", "수동 승인 후 배포", "정해진 일정에 따라 배포"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN 'CI(Continuous Integration, 지속적 통합)는 개발자들이 코드 변경사항을 정기적으로 중앙 저장소에 병합하고, 자동화된 빌드와 테스트를 실행하는 개발 방식입니다.'
                     WHEN 1 THEN 'Apache Tomcat은 Java 서블릿 컨테이너이며, 컨테이너 기술이 아닙니다. Docker, Kubernetes, LXC는 컨테이너 기술 또는 컨테이너 오케스트레이션 도구입니다.'
                     WHEN 2 THEN 'git distribute는 존재하지 않는 Git 명령어입니다. git commit, git push, git pull은 모두 유효한 Git 명령어입니다.'
                     WHEN 3 THEN 'Ansible은 구성 관리 도구입니다. Jenkins는 CI/CD 도구, Maven은 빌드 자동화 도구, JUnit은 테스트 프레임워크입니다.'
                     WHEN 4 THEN '블루-그린 배포 전략의 주요 이점은 새 버전과 이전 버전 사이를 빠르게 전환할 수 있어 다운타임을 최소화하는 것입니다.'
                     WHEN 5 THEN 'docker virtualize는 존재하지 않는 Docker 명령어입니다. docker build, docker run, docker pull은 모두 유효한 Docker 명령어입니다.'
                     WHEN 6 THEN 'DevOps의 목표는 자동화를 통해 개발과 운영 간 협업을 강화하고, 배포 빈도를 높이며, 변경 실패율을 줄이는 것입니다. 모든 개발을 수동으로 테스트하는 것은 DevOps 원칙에 반대됩니다.'
                     WHEN 7 THEN 'Prometheus는 시스템 모니터링 및 알림 도구입니다. Selenium은 테스트 자동화 도구, Maven은 빌드 도구, Terraform은 IaC 도구입니다.'
                     WHEN 8 THEN '컨테이너 오케스트레이션은 컨테이너의 배포, 관리, 확장, 네트워킹, 가용성을 자동화하기 위해 필요합니다. 애플리케이션 코드 작성 간소화는 컨테이너 오케스트레이션의 목적이 아닙니다.'
                     WHEN 9 THEN '지속적 배포(Continuous Deployment)는 변경사항이 모든 테스트를 통과하면 자동으로 프로덕션 환경에 배포되는 방식입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 CASE
                     WHEN mod(seq, 20) = 10 THEN
                         '# 다음 파일은 무엇을 정의하는가?
                         stages:
                           - build
                           - test
                           - deploy

                         build:
                           stage: build
                           script:
                             - echo "Building the app"
                             - npm install
                             - npm run build

                         test:
                           stage: test
                           script:
                             - echo "Running tests"
                             - npm run test

                         deploy:
                           stage: deploy
                           script:
                             - echo "Deploying application"
                             - npm run deploy
                           only:
                             - main'
                     ELSE NULL
                     END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1 FROM quiz_primary_tags pt
                                   JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id AND tm.name = '데브옵스'
             )
             LIMIT 300
     ),
-- 머신러닝 관련 문제 생성
     ml_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 10)
                     WHEN 0 THEN '지도 학습과 비지도 학습의 차이점은?'
                     WHEN 1 THEN '과적합(Overfitting)을 방지하는 방법이 아닌 것은?'
                     WHEN 2 THEN '다음 중 분류 알고리즘이 아닌 것은?'
                     WHEN 3 THEN '다음 중 딥러닝 활성화 함수가 아닌 것은?'
                     WHEN 4 THEN '주성분 분석(PCA)의 주 목적은?'
                     WHEN 5 THEN '앙상블 학습 방법이 아닌 것은?'
                     WHEN 6 THEN '다음 중 자연어 처리에 주로 사용되는 모델은?'
                     WHEN 7 THEN '컨볼루션 신경망(CNN)이 주로 사용되는 분야는?'
                     WHEN 8 THEN '정확도(Accuracy)가 좋은 성능 지표가 아닌 경우는?'
                     WHEN 9 THEN '다음 중 비지도 학습 알고리즘은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '지도 학습은 레이블이 있는 데이터로 학습한다'
                     WHEN 1 THEN '더 많은 특성(feature) 사용하기'
                     WHEN 2 THEN '선형 회귀'
                     WHEN 3 THEN 'Gaussian'
                     WHEN 4 THEN '차원 축소'
                     WHEN 5 THEN 'SVM(Support Vector Machine)'
                     WHEN 6 THEN 'BERT'
                     WHEN 7 THEN '이미지 인식'
                     WHEN 8 THEN '불균형 데이터셋의 경우'
                     WHEN 9 THEN 'K-means 클러스터링'
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '["지도 학습은 레이블이 있는 데이터로 학습한다", "비지도 학습은 출력값을 예측한다", "지도 학습은 군집화에 사용된다", "비지도 학습은 분류 문제에 주로 사용된다"]'::jsonb
                     WHEN 1 THEN '["교차 검증 사용", "정규화 적용", "더 많은 특성(feature) 사용하기", "조기 종료(early stopping)"]'::jsonb
                     WHEN 2 THEN '["로지스틱 회귀", "결정 트리", "선형 회귀", "랜덤 포레스트"]'::jsonb
                     WHEN 3 THEN '["ReLU", "Sigmoid", "Tanh", "Gaussian"]'::jsonb
                     WHEN 4 THEN '["분류 정확도 향상", "회귀 성능 향상", "차원 축소", "과적합 증가"]'::jsonb
                     WHEN 5 THEN '["랜덤 포레스트", "배깅", "부스팅", "SVM(Support Vector Machine)"]'::jsonb
                     WHEN 6 THEN '["CNN", "BERT", "MLP", "PCA"]'::jsonb
                     WHEN 7 THEN '["시계열 예측", "텍스트 분류", "이미지 인식", "회귀 분석"]'::jsonb
                     WHEN 8 THEN '["모든 클래스가 균등한 경우", "이진 분류 문제", "불균형 데이터셋의 경우", "충분한 테스트 데이터가 있는 경우"]'::jsonb
                     WHEN 9 THEN '["K-means 클러스터링", "로지스틱 회귀", "랜덤 포레스트", "서포트 벡터 머신"]'::jsonb
                     END,
                 CASE mod(seq, 10)
                     WHEN 0 THEN '지도 학습은 입력값과 그에 대응하는 정답(레이블)이 있는 데이터로 학습합니다. 반면 비지도 학습은 레이블 없이 데이터의 패턴이나 구조를 찾는 방식입니다.'
                     WHEN 1 THEN '더 많은 특성을 사용하는 것은 과적합을 증가시킬 수 있습니다. 과적합을 방지하는 방법으로는 교차 검증, 정규화, 조기 종료, 데이터 증강 등이 있습니다.'
                     WHEN 2 THEN '선형 회귀는 연속적인 출력값을 예측하는 회귀 알고리즘입니다. 로지스틱 회귀, 결정 트리, 랜덤 포레스트는 분류 알고리즘입니다.'
                     WHEN 3 THEN 'ReLU, Sigmoid, Tanh는 딥러닝에서 널리 사용되는 활성화 함수입니다. Gaussian은 확률 분포이며 일반적인 활성화 함수로 사용되지 않습니다.'
                     WHEN 4 THEN '주성분 분석(PCA)의 주 목적은 데이터의 차원을 축소하여 계산 효율성을 높이고 중요한 패턴을 보존하는 것입니다.'
                     WHEN 5 THEN '앙상블 학습 방법에는 랜덤 포레스트, 배깅, 부스팅 등이 있습니다. SVM은 단일 모델 알고리즘이며 앙상블 방법이 아닙니다.'
                     WHEN 6 THEN 'BERT(Bidirectional Encoder Representations from Transformers)는 자연어 처리를 위한 사전 훈련된 모델입니다. CNN은 이미지, MLP는 일반적인 패턴 인식, PCA는 차원 축소에 주로 사용됩니다.'
                     WHEN 7 THEN '컨볼루션 신경망(CNN)은 이미지 인식과 컴퓨터 비전 작업에 특화된 딥러닝 모델입니다.'
                     WHEN 8 THEN '불균형 데이터셋의 경우(예: 99% 정상, 1% 비정상), 단순히 정확도만으로는 모델의 성능을 제대로 평가하기 어렵습니다. 이런 경우 F1 점수, 정밀도, 재현율 등이 더 적합한 지표입니다.'
                     WHEN 9 THEN 'K-means 클러스터링은 데이터를 K개의 클러스터로 군집화하는 비지도 학습 알고리즘입니다. 로지스틱 회귀, 랜덤 포레스트, 서포트 벡터 머신은 모두 지도 학습 알고리즘입니다.'
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 5
                     WHEN 'INTERMEDIATE' THEN 10
                     ELSE 15
                     END,
                 CASE q.difficulty_level
                     WHEN 'BEGINNER' THEN 30
                     WHEN 'INTERMEDIATE' THEN 45
                     ELSE 60
                     END,
                 q.id,
                 NULL  -- 코드 스니펫은 생략 (필요 시 별도 추가 가능)
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE EXISTS (
                 SELECT 1
                 FROM quiz_primary_tags pt
                          JOIN tag_map tm ON pt.tag_id = tm.id
                 WHERE pt.quiz_id = q.id
                   AND tm.name IN ('머신러닝', '데이터분석', '컴퓨터비전', '자연어처리')
             )
             LIMIT 300
     ),



-- 사용자 퀴즈 시도 데이터 생성
     quiz_attempt_data AS (
         INSERT INTO public.quiz_attempts (
                                           created_at, start_time, end_time, score, is_completed, time_taken,
                                           user_id, quiz_id
             )
             SELECT
                 (NOW() - (random() * INTERVAL '90 days')),
                 (NOW() - (random() * INTERVAL '90 days')),
                 (NOW() - (random() * INTERVAL '89 days')),
                 floor(random() * 60 + 40), -- 40-100점 사이 점수
                 true, -- 완료된 시도
                 floor(random() * 900 + 100), -- 100-1000초 소요 시간
                 (SELECT id FROM users ORDER BY random() LIMIT 1),
                 (SELECT id FROM quizzes ORDER BY random() LIMIT 1)
             FROM generate_series(1, 200) -- 200개의 퀴즈 시도 생성
             RETURNING id, quiz_id, score, user_id
     ),
-- 퀴즈 시도에 따른 문제 시도 데이터 생성
     question_attempt_data AS (
         INSERT INTO public.question_attempts (
                                               created_at, time_taken, is_correct, user_answer,
                                               quiz_attempt_id, question_id
             )
             SELECT
                 (NOW() - (random() * INTERVAL '89 days')),
                 floor(random() * 60 + 5), -- 5-65초 소요 시간
                 CASE WHEN random() < (att.score / 100.0) THEN true ELSE false END, -- 점수 비율에 따른 정답률
                 CASE
                     WHEN random() < (att.score / 100.0) THEN (
                         SELECT correct_answer FROM questions WHERE id = (
                             SELECT id FROM questions WHERE quiz_id = att.quiz_id ORDER BY random() LIMIT 1
                         )
                     )
                     ELSE '잘못된 답변' -- 오답
                     END,
                 att.id, -- 퀴즈 시도 ID
                 (SELECT id FROM questions WHERE quiz_id = att.quiz_id ORDER BY random() LIMIT 1) -- 해당 퀴즈의 랜덤 문제
             FROM quiz_attempt_data att
                      CROSS JOIN generate_series(1, 5) -- 각 퀴즈 시도마다 5개의 문제 시도
     ),
-- 퀴즈 리뷰 생성
     quiz_review_data AS (
         INSERT INTO public.quiz_reviews (
                                          created_at, rating, content, quiz_id, reviewer_id
             )
             SELECT
                 NOW() - (random() * INTERVAL '60 days'),
                 floor(random() * 3 + 3), -- 3-5점 점수
                 CASE floor(random() * 5)
                     WHEN 0 THEN '매우 유익한 퀴즈였습니다. 특히 실무에 적용 가능한 내용이 좋았습니다.'
                     WHEN 1 THEN '난이도가 적절하고 내용이 알찬 퀴즈입니다.'
                     WHEN 2 THEN '문제의 설명이 명확해서 학습하기 좋았습니다.'
                     WHEN 3 THEN '개념을 정리하는데 많은 도움이 되었습니다. 다음 퀴즈도 기대합니다.'
                     ELSE '문제 구성이 체계적이고 학습에 효과적입니다.'
                     END,
                 (SELECT id FROM quizzes ORDER BY random() LIMIT 1),
                 (SELECT id FROM users WHERE role = 'USER' ORDER BY random() LIMIT 1)
             FROM generate_series(1, 100) -- 100개의 퀴즈 리뷰 생성
             RETURNING id, reviewer_id
     ),
-- 퀴즈 리뷰 댓글 생성
     quiz_review_comment_data AS (
         INSERT INTO public.quiz_review_comments (
                                                  created_at, content, parent_review_id, commenter_id
             )
             SELECT
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE floor(random() * 5)
                     WHEN 0 THEN '리뷰 감사합니다. 피드백을 반영하여 더 좋은 퀴즈를 만들겠습니다.'
                     WHEN 1 THEN '의견 공유해주셔서 감사합니다.'
                     WHEN 2 THEN '다음 버전에서 개선하도록 하겠습니다.'
                     WHEN 3 THEN '정확한 피드백 감사합니다. 많은 도움이 됩니다.'
                     ELSE '좋은 평가 감사합니다!'
                     END,
                 review.id,
                 CASE
                     WHEN random() < 0.7 THEN (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1)
                     ELSE (SELECT id FROM users WHERE id != review.reviewer_id ORDER BY random() LIMIT 1)
                     END
             FROM quiz_review_data review
             WHERE random() < 0.5 -- 50% 확률로 댓글 생성
     ),
-- 사용자 레벨 데이터 생성
     user_level_data AS (
         INSERT INTO public.user_levels (
                                         created_at, updated_at, level, current_exp, required_exp, user_id
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 u.level,
                 u.experience,
                 u.required_experience,
                 u.id
             FROM users u
             WHERE NOT EXISTS (SELECT 1 FROM user_levels WHERE user_id = u.id)
             RETURNING id, user_id, level
     ),
-- 사용자 레벨 이력 생성
     user_level_history_data AS (
         INSERT INTO public.user_level_history (
                                                updated_at, previous_level, level, user_id
             )
             SELECT
                 NOW() - ((5 - seq) * INTERVAL '30 days'),
                 level_data.level - seq,
                 level_data.level - seq + 1,
                 level_data.user_id
             FROM user_level_data level_data
                      CROSS JOIN generate_series(1, 4) seq
             WHERE level_data.level > seq -- 현재 레벨보다 낮은 이전 레벨만 기록
     ),
-- 사용자 업적 생성
     user_achievement_data AS (
         INSERT INTO public.user_achievement_history (
                                                      earned_at, achievement, achievement_name, user_id
             )
             SELECT
                 NOW() - (random() * INTERVAL '90 days'),
                 CASE floor(random() * 8)
                     WHEN 0 THEN 'FIRST_QUIZ_COMPLETED'::text
                     WHEN 1 THEN 'PERFECT_SCORE'::text
                     WHEN 2 THEN 'WINNING_STREAK_3'::text
                     WHEN 3 THEN 'WINNING_STREAK_5'::text
                     WHEN 4 THEN 'WINNING_STREAK_10'::text
                     WHEN 5 THEN 'DAILY_QUIZ_MASTER'::text
                     WHEN 6 THEN 'QUICK_SOLVER'::text
                     ELSE 'KNOWLEDGE_SEEKER'::text
                     END,
                 CASE floor(random() * 8)
                     WHEN 0 THEN '첫 퀴즈 완료'
                     WHEN 1 THEN '완벽한 점수'
                     WHEN 2 THEN '3연승'
                     WHEN 3 THEN '5연승'
                     WHEN 4 THEN '10연승'
                     WHEN 5 THEN '데일리 퀴즈 마스터'
                     WHEN 6 THEN '빠른 해결사'
                     ELSE '지식 탐구자'
                     END,
                 user_id
             FROM (
                      SELECT id as user_id FROM users ORDER BY random() LIMIT 10
                  ) as random_users
                      CROSS JOIN generate_series(1, 3) -- 각 사용자마다 평균 3개의 업적
             WHERE random() < 0.7 -- 70% 확률로 업적 생성
     ),
-- 사용자 전투 통계 생성
     user_battle_stats_data AS (
         INSERT INTO public.user_battle_stats (
                                               created_at, updated_at, total_battles, wins, total_score, highest_score,
                                               total_correct_answers, total_questions, highest_streak, current_streak,
                                               user_id
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 floor(random() * 50 + 10), -- 10-60 전투 횟수
                 floor(random() * 30 + 5),  -- 5-35 승리 횟수
                 floor(random() * 5000 + 1000), -- 1000-6000 총점
                 floor(random() * 200 + 100),   -- 100-300 최고 점수
                 floor(random() * 300 + 50),    -- 50-350 총 정답 수
                 floor(random() * 500 + 100),   -- 100-600 총 문제 수
                 floor(random() * 8 + 2),       -- 2-10 최고 연속 승리
                 floor(random() * 3),           -- 0-3 현재 연속 승리
                 u.id
             FROM users u
             WHERE NOT EXISTS (SELECT 1 FROM user_battle_stats WHERE user_id = u.id)
     )

-- 더미 데이터 생성 완료 확인
SELECT 'CS 퀴즈 플랫폼 더미 데이터 생성 완료' as result;