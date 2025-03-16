-- CS-Quiz 플랫폼을 위한 향상된 더미 데이터
-- 이 스크립트는 애플리케이션 테스트를 위한 포괄적인 테스트 데이터를 제공합니다

-- 필요한 경우 기존 데이터 삭제 (주의: 필요한 경우에만 주석 해제)
-- TRUNCATE TABLE users, user_levels, user_battle_stats, user_achievements, quiz_attempts, question_attempts,
--              quizzes, questions, tags, quiz_tags, tag_synonyms, battle_rooms, battle_participants, battle_answers,
--              quiz_reviews, quiz_review_comments, user_achievement_history, user_level_history CASCADE;

-- 사용자 관련 데이터 준비
WITH user_level_info AS (
    SELECT
        unnest(ARRAY['admin', '김철수', '이영희', '박민준', '정수연', '최지훈']) as username,
        unnest(ARRAY[10, 5, 3, 7, 2, 4]) as level,
        unnest(ARRAY[800, 450, 250, 620, 150, 380]) as current_exp,
        unnest(ARRAY[1000, 600, 400, 800, 300, 500]) as required_exp,
        unnest(ARRAY[5000, 2200, 1500, 3500, 800, 1900]) as total_points,
        unnest(ARRAY['ADMIN', 'USER', 'USER', 'USER', 'USER', 'USER']) as role
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
                 (NOW(), '보안', '보안 원칙, 취약점 및 모범 사례')
             RETURNING id, name
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
                              ('뷰', 'Vue.js 프로그레시브 프레임워크')
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
                     ((SELECT id FROM tag_map WHERE name = '머신러닝'), '기계학습')
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
-- Java 태그 동의어 추가
     java_synonyms AS (
         INSERT INTO public.tag_synonyms (tag_id, synonym)
             SELECT (SELECT id FROM java_tag), synonym
             FROM (
                      VALUES ('자바'), ('JAVA'), ('java')
                  ) as s(synonym)
             ON CONFLICT DO NOTHING
     ),
-- 모든 사용자의 ID 가져오기
     user_ids AS (
         SELECT id FROM initial_users
     ),
-- 관리자 사용자 ID 가져오기
     admin_user AS (
         SELECT id FROM initial_users WHERE role = 'ADMIN' LIMIT 1
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

             RETURNING id, quiz_type, difficulty_level, creator_id, question_count
     ),
-- 퀴즈와 태그 연결
     java_quiz_tags AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT q.id, (SELECT id FROM java_tag)
             FROM java_quizzes q
     ),
     daily_quizzes AS (
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
                 5,
                 'DAILY',
                 15,
                 (SELECT id FROM admin_user),
                 floor(random() * 100 + 50),
                 random() * 40 + 60,
                 floor(random() * 500 + 200),
                 CASE
                     WHEN seq = 0 THEN NOW() + INTERVAL '1 day'
                     ELSE NOW() - ((seq-1) * INTERVAL '1 day')
                     END
             FROM generate_series(0, 29) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count
     ),
-- 주제별 퀴즈 생성
     topic_quizzes AS (
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
             FROM generate_series(0, 35) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count
     ),
-- 커스텀 퀴즈 생성
     custom_quizzes AS (
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
             FROM generate_series(0, 14) AS seq
             RETURNING id, quiz_type, difficulty_level, creator_id, question_count
     ),
-- 모든 퀴즈 통합
     all_quizzes AS (
         SELECT id, quiz_type, difficulty_level, creator_id, question_count FROM daily_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count FROM topic_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count FROM custom_quizzes
         UNION ALL
         SELECT id, quiz_type, difficulty_level, creator_id, question_count FROM java_quizzes
     ),

-- 퀴즈와 태그 연결
     quiz_tags AS (
         WITH quiz_info AS (
             SELECT id, quiz_type, (floor(random() * 12))::int AS tag_index
             FROM all_quizzes
             ),
             tag_info AS (
                 SELECT id, name, row_number() OVER (ORDER BY id) - 1 AS tag_index
                 FROM inserted_tags
                 )
             INSERT INTO public.quiz_tags (quiz_id, tag_id)
                 -- 모든 퀴즈에 주 태그 배정
                 SELECT
                     q.id AS quiz_id,
                     t.id AS tag_id
                 FROM quiz_info q
                          JOIN tag_info t ON q.tag_index = t.tag_index

                 UNION ALL

                 -- 일부 퀴즈에 추가 태그 배정 (25% 확률)
                 SELECT
                     q.id AS quiz_id,
                     t.id AS tag_id
                 FROM all_quizzes q
                          CROSS JOIN inserted_tags t
                 WHERE random() < 0.25
                   AND NOT EXISTS (
                     SELECT 1 FROM quiz_tags
                     WHERE quiz_id = q.id AND tag_id = t.id
                 )
                 LIMIT 30
     ),



     -- 자바 기초 면접 질문 (초급)
     java_basic_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             WITH quiz_ids AS (
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'BEGINNER'
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
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'INTERMEDIATE'
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
                 SELECT id FROM java_quizzes WHERE difficulty_level = 'ADVANCED'
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
                           'Java NIO(New Input/Output)는 Java 1.4에서 도입된 IO API로, 기존 IO 패키지와 비교해 몇 가지 주요 차이점이 있습니다. 1) 버퍼 지향: NIO는 버퍼(Buffer) 기반으로 작동하여 데이터를 일시적으로 저장하고 처리할 수 있습니다. 2) 논블로킹 IO: NIO는 논블로킹 모드를 지원하여 한 스레드가 여러 채널을 관리할 수 있습니다. 3) 채널: NIO는 채널(Channel)을 통해 데이터를',null),
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
                           NULL)
                  ) AS questions(question, answer, options, explanation, code)
     ),

                    -- 자바스크립트 문제 생성
                         js_questions AS (
                             INSERT INTO public.questions (
                                                           created_at, updated_at, question_text, question_type, difficulty_level,
                                                           correct_answer, options, explanation, points, time_limit_seconds,
                                                           quiz_id, code_snippet
                                 )
                                 SELECT
                                     NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN '자바스크립트에서 변수를 선언하는 키워드가 아닌 것은?'
                     WHEN 1 THEN '다음 중 자바스크립트의 원시 타입(Primitive Type)이 아닌 것은?'
                     WHEN 2 THEN '자바스크립트에서 함수를 선언하는 올바른 방법은?'
                     WHEN 3 THEN '다음 코드의 실행 결과는? console.log(typeof [])'
                     WHEN 4 THEN 'ES6에서 추가된 기능이 아닌 것은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'switch'
                     WHEN 1 THEN 'array'
                     WHEN 2 THEN 'function myFunc() {}'
                     WHEN 3 THEN 'object'
                     WHEN 4 THEN 'class'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["var", "let", "const", "switch"]'::jsonb
                     WHEN 1 THEN '["string", "number", "boolean", "array"]'::jsonb
                     WHEN 2 THEN '["function myFunc() {}", "let myFunc = function() {}", "const myFunc = () => {}", "myFunc: function() {}"]'::jsonb
                     WHEN 3 THEN '["undefined", "object", "array", "reference"]'::jsonb
                     WHEN 4 THEN '["let/const", "화살표 함수", "클래스", "switch문"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'var, let, const는 변수 선언 키워드이지만, switch는 조건문을 작성할 때 사용하는 키워드입니다.'
                     WHEN 1 THEN '자바스크립트의 원시 타입은 string, number, boolean, null, undefined, symbol, bigint입니다. Array는 객체(Object) 타입입니다.'
                     WHEN 2 THEN '자바스크립트에서 함수를 선언하는 방법은 함수 선언식, 함수 표현식, 화살표 함수 등이 있습니다.'
                     WHEN 3 THEN '자바스크립트에서 배열(Array)의 typeof 결과는 "object"입니다. 배열은 특수한 형태의 객체입니다.'
                     WHEN 4 THEN 'ES6에서는 let/const, 화살표 함수, 클래스, 템플릿 리터럴, 구조 분해 할당 등이 추가되었습니다. switch문은 ES6 이전부터 존재했습니다.'
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
                 CASE WHEN mod(seq, 10) = 3 THEN
                          '// 다음 코드의 실행 결과를 생각해보세요
                          let arr = [];
                          console.log(typeof arr);'
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 12) < 5
             LIMIT 200
     ),
-- 파이썬 문제 생성
     py_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN '파이썬에서 리스트를 생성하는 올바른 방법은?'
                     WHEN 1 THEN '파이썬에서 딕셔너리를 순회하는 올바른 방법은?'
                     WHEN 2 THEN '파이썬의 리스트 컴프리헨션으로 올바르게 작성된 것은?'
                     WHEN 3 THEN '파이썬에서 문자열을 포맷팅하는 방법이 아닌 것은?'
                     WHEN 4 THEN '파이썬 함수 정의에서 "*args"의 의미는?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'my_list = [1, 2, 3]'
                     WHEN 1 THEN 'for key, value in my_dict.items():'
                     WHEN 2 THEN '[x ** 2 for x in range(10)]'
                     WHEN 3 THEN 'print(f"{} is good")'
                     WHEN 4 THEN '가변 개수의 위치 인자를 받는 매개변수'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["my_list = [1, 2, 3]", "my_list = list(1, 2, 3)", "my_list = array(1, 2, 3)", "my_list = (1, 2, 3)"]'::jsonb
                     WHEN 1 THEN '["for key, value in my_dict.items():", "for key, value in my_dict:", "for item in my_dict.items():", "for key in my_dict:"]'::jsonb
                     WHEN 2 THEN '["[x ** 2 for x in range(10)]", "[for x in range(10): x ** 2]", "[x ** 2 in range(10)]", "[x for x ** 2 in range(10)]"]'::jsonb
                     WHEN 3 THEN E'["print(f\\"{name} is good\\")", "print(\\"{} is good\\".format(name))", "print(\\"%s is good\\" % name)", "print(f\\"{} is good\\")"]'::jsonb
                     WHEN 4 THEN '["가변 개수의 위치 인자를 받는 매개변수", "가변 개수의 키워드 인자를 받는 매개변수", "기본값이 있는 매개변수", "위치 인자만 받는 매개변수"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '파이썬에서 리스트는 대괄호([])를 사용하여 생성합니다. list()는 이터러블 객체에서 리스트를 생성하는 함수입니다.'
                     WHEN 1 THEN '딕셔너리의 키와 값을 함께 순회하려면 items() 메서드를 사용해야 합니다.'
                     WHEN 2 THEN '리스트 컴프리헨션은 [표현식 for 변수 in 이터러블] 형태로 작성합니다.'
                     WHEN 3 THEN 'f-문자열에서는 중괄호 안에 변수나 표현식이 있어야 합니다. 빈 중괄호는 올바른 형식이 아닙니다.'
                     WHEN 4 THEN '*args는 가변 개수의 위치 인자를 받기 위한 구문입니다. 키워드 인자는 **kwargs로 받습니다.'
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
                 CASE WHEN mod(seq, 10) = 2 THEN
                          '# 다음 코드의 결과는?
                          squares = [x ** 2 for x in range(5)]
                          print(squares)'
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 12) >= 5 AND mod(seq, 12) < 10
             LIMIT 200
     ),
-- 알고리즘, 자료구조 관련 문제 생성
     algo_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN '시간 복잡도가 O(n log n)인 정렬 알고리즘은?'
                     WHEN 1 THEN '이진 검색 트리에서 삽입 연산의 시간 복잡도는?'
                     WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는?'
                     WHEN 3 THEN '그래프 탐색에 사용되지 않는 알고리즘은?'
                     WHEN 4 THEN '다음 중 그리디 알고리즘을 사용하는 문제는?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '퀵 정렬'
                     WHEN 1 THEN 'O(log n)'
                     WHEN 2 THEN 'O(n)'
                     WHEN 3 THEN '삽입 정렬'
                     WHEN 4 THEN '다익스트라 최단 경로'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["버블 정렬", "삽입 정렬", "퀵 정렬", "계수 정렬"]'::jsonb
                     WHEN 1 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
                     WHEN 2 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
                     WHEN 3 THEN '["깊이 우선 탐색", "너비 우선 탐색", "다익스트라", "삽입 정렬"]'::jsonb
                     WHEN 4 THEN '["최단 경로 찾기", "최소 신장 트리", "다익스트라 최단 경로", "0/1 배낭 문제"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '퀵 정렬, 병합 정렬, 힙 정렬은 평균적으로 O(n log n)의 시간 복잡도를 가집니다. 버블 정렬과 삽입 정렬은 O(n²)입니다.'
                     WHEN 1 THEN '이진 검색 트리에서 삽입은 트리의 높이에 비례하며, 균형 잡힌 트리의 경우 O(log n)입니다.'
                     WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는 충돌이 많이 발생할 경우 O(n)입니다.'
                     WHEN 3 THEN '삽입 정렬은 정렬 알고리즘이며, 그래프 탐색 알고리즘이 아닙니다. DFS, BFS, 다익스트라는 그래프 탐색 알고리즘입니다.'
                     WHEN 4 THEN '다익스트라 알고리즘은 그리디 알고리즘의 대표적인 예입니다. 항상 현재 가장 최적인 경로를 선택합니다.'
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
                 CASE WHEN mod(seq, 10) = 4 THEN
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
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 12) >= 10
             LIMIT 200
     ),
-- 네트워크 관련 문제 생성
     network_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'HTTP 상태 코드 404는 무엇을 의미하는가?'
                     WHEN 1 THEN 'TCP/IP 모델의 계층 순서가 올바른 것은?'
                     WHEN 2 THEN '다음 중 라우팅 프로토콜이 아닌 것은?'
                     WHEN 3 THEN 'IPv4 주소의 클래스 A의 첫 번째 옥텟 범위는?'
                     WHEN 4 THEN 'DNS의 주요 기능은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '리소스를 찾을 수 없음'
                     WHEN 1 THEN '링크 계층 - 인터넷 계층 - 전송 계층 - 응용 계층'
                     WHEN 2 THEN 'SMTP'
                     WHEN 3 THEN '0-127'
                     WHEN 4 THEN '도메인 이름을 IP 주소로 변환'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["리소스를 찾을 수 없음", "권한 없음", "서버 내부 오류", "요청 성공"]'::jsonb
                     WHEN 1 THEN '["링크 계층 - 인터넷 계층 - 전송 계층 - 응용 계층", "응용 계층 - 전송 계층 - 인터넷 계층 - 링크 계층", "전송 계층 - 인터넷 계층 - 링크 계층 - 응용 계층", "인터넷 계층 - 링크 계층 - 전송 계층 - 응용 계층"]'::jsonb
                     WHEN 2 THEN '["OSPF", "RIP", "BGP", "SMTP"]'::jsonb
                     WHEN 3 THEN '["0-127", "128-191", "192-223", "224-239"]'::jsonb
                     WHEN 4 THEN '["도메인 이름을 IP 주소로 변환", "데이터 패킷을 암호화", "네트워크 트래픽을 제어", "방화벽 규칙을 관리"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '404 상태 코드는 요청한 리소스를 서버에서 찾을 수 없다는 것을 의미합니다.'
                     WHEN 1 THEN 'TCP/IP 모델의 계층 순서는 링크(물리+데이터 링크) - 인터넷(네트워크) - 전송 - 응용 계층입니다.'
                     WHEN 2 THEN 'SMTP는 이메일 전송 프로토콜이며, 라우팅 프로토콜이 아닙니다. OSPF, RIP, BGP는 라우팅 프로토콜입니다.'
                     WHEN 3 THEN 'IPv4 클래스 A는 첫 비트가 0으로 시작하며, 첫 번째 옥텟 범위는 0-127입니다.'
                     WHEN 4 THEN 'DNS(Domain Name System)의 주요 기능은 사람이 읽을 수 있는 도메인 이름을 IP 주소로 변환하는 것입니다.'
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
             WHERE mod(seq, 24) >= 12 AND mod(seq, 24) < 17
             LIMIT 200
     ),
-- 웹개발 관련 문제 생성
     web_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'REST API에서 PATCH 메서드의 용도는?'
                     WHEN 1 THEN 'CORS란 무엇인가?'
                     WHEN 2 THEN '다음 중 프론트엔드 프레임워크가 아닌 것은?'
                     WHEN 3 THEN 'CSS 선택자 우선순위가 가장 높은 것은?'
                     WHEN 4 THEN 'localStorage와 sessionStorage의 차이점은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '리소스 부분 업데이트'
                     WHEN 1 THEN '교차 출처 리소스 공유'
                     WHEN 2 THEN 'Django'
                     WHEN 3 THEN '인라인 스타일'
                     WHEN 4 THEN 'sessionStorage는 브라우저 세션이 끝나면 데이터가 삭제됨'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["리소스 부분 업데이트", "리소스 전체 업데이트", "리소스 생성", "리소스 삭제"]'::jsonb
                     WHEN 1 THEN '["교차 출처 리소스 공유", "콘텐츠 전송 네트워크", "캐스케이딩 스타일 시트", "클라이언트 측 렌더링"]'::jsonb
                     WHEN 2 THEN '["React", "Angular", "Vue", "Django"]'::jsonb
                     WHEN 3 THEN '["인라인 스타일", "ID 선택자", "클래스 선택자", "태그 선택자"]'::jsonb
                     WHEN 4 THEN '["서로 다른 도메인 간 데이터 공유", "데이터 암호화 방식의 차이", "sessionStorage는 브라우저 세션이 끝나면 데이터가 삭제됨", "localStorage는 용량 제한이 더 큼"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'PATCH 메서드는 리소스의 일부분만 업데이트하는 데 사용됩니다. PUT은 리소스 전체를 업데이트합니다.'
                     WHEN 1 THEN 'CORS(Cross-Origin Resource Sharing)는 다른 출처의 리소스에 대한 접근을 제어하는 HTTP 헤더 기반 메커니즘입니다.'
                     WHEN 2 THEN 'Django는 Python 기반의 백엔드 프레임워크입니다. React, Angular, Vue는 프론트엔드 프레임워크입니다.'
                     WHEN 3 THEN 'CSS 선택자 우선순위는 인라인 스타일 > ID 선택자 > 클래스 선택자 > 태그 선택자 순입니다.'
                     WHEN 4 THEN 'localStorage와 sessionStorage는 모두 클라이언트 측 데이터 저장소이지만, sessionStorage는 브라우저 세션이 종료되면 데이터가 삭제되는 반면, localStorage는 명시적으로 삭제할 때까지 데이터가 유지됩니다.'
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
                 CASE WHEN mod(seq, 10) = 6 THEN
                          '// 다음 코드의 결과는?
                          const promise1 = Promise.resolve(3);
                          const promise2 = new Promise((resolve, reject) => {
                            setTimeout(() => resolve("foo"), 100);
                          });

                          Promise.all([promise1, promise2]).then(values => {
                            console.log(values);
                          });'
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 24) >= 17 AND mod(seq, 24) < 22
             LIMIT 200
     ),
-- 운영체제 관련 문제 생성
     os_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN '프로세스와 스레드의 주요 차이점은?'
                     WHEN 1 THEN '페이지 교체 알고리즘이 아닌 것은?'
                     WHEN 2 THEN '교착 상태(Deadlock)의 필요 조건이 아닌 것은?'
                     WHEN 3 THEN '선점형 스케줄링 알고리즘이 아닌 것은?'
                     WHEN 4 THEN '가상 메모리의 주요 목적은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '스레드는 자원을 공유하지만 프로세스는 독립적이다'
                     WHEN 1 THEN 'Round Robin'
                     WHEN 2 THEN '선점 가능'
                     WHEN 3 THEN 'FCFS'
                     WHEN 4 THEN '물리적 메모리보다 큰 프로그램 실행 허용'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["스레드는 자원을 공유하지만 프로세스는 독립적이다", "프로세스는 경량이고 스레드는 중량이다", "스레드는 컨텍스트 스위칭 비용이 더 높다", "프로세스는 하나의 스레드만 가질 수 있다"]'::jsonb
                     WHEN 1 THEN '["LRU", "FIFO", "Round Robin", "Optimal"]'::jsonb
                     WHEN 2 THEN '["상호 배제", "점유와 대기", "비선점", "선점 가능"]'::jsonb
                     WHEN 3 THEN '["FCFS", "SJF(선점형)", "Round Robin", "Priority Scheduling(선점형)"]'::jsonb
                     WHEN 4 THEN '["물리적 메모리보다 큰 프로그램 실행 허용", "디스크 입출력 시간 단축", "CPU 사용률 감소", "데이터 보안 향상"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '프로세스는 독립적인 메모리 공간과 자원을 가지지만, 스레드는 프로세스 내에서 메모리와 자원을 공유합니다. 스레드는 "경량 프로세스"라고도 불립니다.'
                     WHEN 1 THEN 'Round Robin은 페이지 교체 알고리즘이 아니라 CPU 스케줄링 알고리즘입니다. LRU, FIFO, Optimal은 페이지 교체 알고리즘입니다.'
                     WHEN 2 THEN '교착 상태의 4가지 필요 조건은 상호 배제, 점유와 대기, 비선점, 순환 대기입니다. "선점 가능"은 오히려 교착 상태를 방지하는 조건입니다.'
                     WHEN 3 THEN 'FCFS(First-Come, First-Served)는 비선점형 스케줄링 알고리즘입니다. 나머지는 모두 선점형 스케줄링 알고리즘입니다.'
                     WHEN 4 THEN '가상 메모리의 주요 목적은 물리적 메모리 크기보다 큰 프로그램을 실행할 수 있게 하는 것입니다. 필요한 부분만 물리 메모리에 로드하고 나머지는 디스크에 유지합니다.'
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
             WHERE mod(seq, 24) >= 22
             LIMIT 200
     ),
-- 데이터베이스 관련 문제 생성
     db_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'SQL에서 DELETE와 TRUNCATE의 차이점은?'
                     WHEN 1 THEN '정규화의 주요 목적은?'
                     WHEN 2 THEN 'ACID 속성에 포함되지 않는 것은?'
                     WHEN 3 THEN '다음 중 비관계형 데이터베이스는?'
                     WHEN 4 THEN 'GROUP BY 절과 함께 사용할 수 없는 집계 함수는?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'TRUNCATE는 롤백이 불가능하다'
                     WHEN 1 THEN '데이터 중복 최소화'
                     WHEN 2 THEN '확장성'
                     WHEN 3 THEN 'MongoDB'
                     WHEN 4 THEN 'TOP'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["DELETE는 조건절을 사용할 수 없다", "TRUNCATE는 인덱스를 삭제한다", "DELETE는 트리거를 발생시킨다", "TRUNCATE는 롤백이 불가능하다"]'::jsonb
                     WHEN 1 THEN '["데이터 중복 최소화", "쿼리 성능 향상", "데이터베이스 크기 증가", "외래 키 제약 완화"]'::jsonb
                     WHEN 2 THEN '["원자성", "일관성", "격리성", "확장성"]'::jsonb
                     WHEN 3 THEN '["MySQL", "Oracle", "PostgreSQL", "MongoDB"]'::jsonb
                     WHEN 4 THEN '["COUNT", "AVG", "TOP", "SUM"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'DELETE는 트랜잭션 로그에 각 행의 삭제를 기록하여 롤백이 가능하지만, TRUNCATE는 테이블의 데이터 페이지를 할당 해제하는 방식으로 빠르게 모든 데이터를 제거하며 로그를 거의 기록하지 않아 롤백이 불가능합니다.'
                     WHEN 1 THEN '정규화의 주요 목적은 데이터 중복을 최소화하여 데이터 무결성을 유지하고 저장 공간을 효율적으로 사용하는 것입니다.'
                     WHEN 2 THEN 'ACID 속성은 원자성(Atomicity), 일관성(Consistency), 격리성(Isolation), 지속성(Durability)입니다. 확장성(Scalability)은 ACID 속성에 포함되지 않습니다.'
                     WHEN 3 THEN 'MongoDB는 문서 기반 NoSQL 데이터베이스입니다. MySQL, Oracle, PostgreSQL은 모두 관계형 데이터베이스입니다.'
                     WHEN 4 THEN 'TOP은 SQL Server에서 결과 집합의 행 수를 제한하는 키워드로, 집계 함수가 아닙니다. COUNT, AVG, SUM은 집계 함수입니다.'
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
                 CASE WHEN mod(seq, 10) = 8 THEN
                          '-- 다음 SQL 쿼리의 결과는?
                          SELECT department, COUNT(*) as emp_count
                          FROM employees
                          WHERE salary > 50000
                          GROUP BY department
                          HAVING COUNT(*) > 5
                          ORDER BY emp_count DESC;'
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 30) >= 0 AND mod(seq, 30) < 5
             LIMIT 200
     ),
-- 보안 관련 문제 생성
     security_questions AS (
         INSERT INTO public.questions (
                                       created_at, updated_at, question_text, question_type, difficulty_level,
                                       correct_answer, options, explanation, points, time_limit_seconds,
                                       quiz_id, code_snippet
             )
             SELECT
                 NOW() - (random() * INTERVAL '180 days'),
                 NOW() - (random() * INTERVAL '30 days'),
                 CASE mod(seq, 5)
                     WHEN 0 THEN 'SQL 인젝션 공격을 방지하는 가장 좋은 방법은?'
                     WHEN 1 THEN '대칭 암호화와 비대칭 암호화의 주요 차이점은?'
                     WHEN 2 THEN 'CSRF 공격이란?'
                     WHEN 3 THEN '다음 중 가장 안전한 비밀번호 해싱 알고리즘은?'
                     WHEN 4 THEN 'HTTPS에서 사용하는 프로토콜은?'
                     END,
                 'MULTIPLE_CHOICE',
                 q.difficulty_level,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '매개변수화된 쿼리 사용'
                     WHEN 1 THEN '비대칭 암호화는 두 개의 다른 키를 사용한다'
                     WHEN 2 THEN '사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격'
                     WHEN 3 THEN 'bcrypt'
                     WHEN 4 THEN 'TLS(SSL)'
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '["매개변수화된 쿼리 사용", "모든 입력 필드 숨기기", "쿼리 로깅 비활성화", "데이터베이스 계정 권한 축소"]'::jsonb
                     WHEN 1 THEN '["비대칭 암호화는 두 개의 다른 키를 사용한다", "대칭 암호화가 항상 더 안전하다", "비대칭 암호화는 항상 더 빠르다", "대칭 암호화는 키가 필요 없다"]'::jsonb
                     WHEN 2 THEN '["사용자의 세션을 훔치는 공격", "사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격", "데이터베이스에서 민감한 정보를 추출하는 공격", "네트워크 트래픽을 감청하는 공격"]'::jsonb
                     WHEN 3 THEN '["MD5", "SHA-1", "bcrypt", "Base64"]'::jsonb
                     WHEN 4 THEN '["FTP", "SSH", "TLS(SSL)", "SMTP"]'::jsonb
                     END,
                 CASE mod(seq, 5)
                     WHEN 0 THEN '매개변수화된 쿼리(Prepared Statements)는 사용자 입력을 SQL 쿼리와 분리하여 처리하므로 SQL 인젝션 공격을 방지하는 가장 효과적인 방법입니다.'
                     WHEN 1 THEN '대칭 암호화는 암호화와 복호화에 동일한 키를 사용하지만, 비대칭 암호화는 공개 키와 개인 키라는 두 개의 다른 키를 사용합니다.'
                     WHEN 2 THEN 'CSRF(Cross-Site Request Forgery)는 사용자가 인증된 상태에서 의도하지 않은 요청을 서버에 보내도록 속이는 공격입니다.'
                     WHEN 3 THEN 'bcrypt는 비밀번호 해싱을 위해 설계된 알고리즘으로, 느린 해시 함수와 솔트를 사용하여 무차별 대입 공격에 강합니다. MD5와 SHA-1은 취약하고, Base64는 인코딩이지 해싱이 아닙니다.'
                     WHEN 4 THEN 'HTTPS는 HTTP 프로토콜에 TLS(이전에는 SSL) 암호화 계층을 추가한 것입니다. TLS는 클라이언트와 서버 간의 통신을 암호화합니다.'
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
                 CASE WHEN mod(seq, 10) = 7 THEN
                          '// 다음 코드는 SQL 인젝션 공격의 취약점을 시뮬레이션하는 예제입니다.
                          function executeQuery(query) {
                              // 사용자 입력을 검증하지 않으면 보안 위험이 발생할 수 있습니다.
                              return database.run(query);
                          }'
                      ELSE NULL END
             FROM all_quizzes q
                      CROSS JOIN generate_series(0, q.question_count - 1) AS seq
             WHERE mod(seq, 30) >= 5 AND mod(seq, 30) < 10
             LIMIT 200
     )
SELECT 1;
