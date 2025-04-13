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
-- 모든 사용자의 ID 가져오기
     user_ids AS (
         SELECT id FROM initial_users
     ),
-- 관리자 사용자 ID 가져오기
     admin_user AS (
         SELECT id FROM initial_users WHERE role = 'ADMIN' LIMIT 1
     ),
-- 데일리 퀴즈 생성
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
                 5, -- 각 데일리 퀴즈는 5개 문제
                 'DAILY',
                 15, -- 15분 제한시간
                 (SELECT id FROM admin_user),
                 floor(random() * 100 + 50), -- 50-150 시도 횟수
                 random() * 40 + 60, -- 60-100 평균 점수
                 floor(random() * 500 + 200), -- 200-700 조회수
                 CASE
                     WHEN seq = 0 THEN NOW() + INTERVAL '1 day' -- 오늘의 퀴즈는 내일까지 유효
                     ELSE NOW() - ((seq-1) * INTERVAL '1 day') -- 과거 퀴즈는 이미 만료됨
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
                 'REGULAR',
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
                 'SPECIAL',
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
