
-- 이 스크립트는 배틀 기능을 제외하고 퀴즈 중심의 테스트 데이터를 제공합니다

-- 기존 데이터 삭제 (주의: 필요한 경우에만 주석 해제)
-- TRUNCATE TABLE users, quizzes, questions, tags, quiz_tags, tag_synonyms, 
--              quiz_attempts, question_attempts, quiz_reviews, quiz_review_comments CASCADE;

-- 1. 사용자 데이터 생성
INSERT INTO public.users (
    email, username, experience, is_active, level, provider, provider_id,
    required_experience, role, total_points, created_at, updated_at, profile_image,
    last_login, access_token, refresh_token, token_expires_at
)
VALUES
    -- 관리자 계정
    (  'admin@example.com', 'admin', 800, true, 10, 'GITHUB', 'admin_id_123',
       1000, 'ADMIN', 5000, NOW() - INTERVAL '200 days', NOW(),
       'https://robohash.org/admin?set=set4', NOW() - INTERVAL '2 days',
       NULL, NULL, NULL);
-- 2. 태그 데이터 생성
INSERT INTO public.tags (created_at, name, description)
VALUES
    -- 주요 카테고리
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
    (NOW(), '블록체인', '분산 원장 기술, 암호화폐 및 스마트 계약');

-- 3. 하위 태그 생성 (웹개발 하위 태그)
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT
    NOW(),
    subtag.name,
    subtag.description,
    (SELECT id FROM public.tags WHERE name = '웹개발')
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
     ) as subtag(name, description);

-- 4. 하위 태그 생성 (프론트엔드 하위 태그)
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT
    NOW(),
    subtag.name,
    subtag.description,
    (SELECT id FROM public.tags WHERE name = '프론트엔드')
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
     ) as subtag(name, description);

-- 5. 하위 태그 생성 (백엔드 하위 태그)
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT
    NOW(),
    subtag.name,
    subtag.description,
    (SELECT id FROM public.tags WHERE name = '백엔드')
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
     ) as subtag(name, description);

-- 6. 태그 동의어 추가
INSERT INTO public.tag_synonyms (tag_id, synonym)
VALUES
    ((SELECT id FROM public.tags WHERE name = '자바스크립트'), 'JS'),
    ((SELECT id FROM public.tags WHERE name = '자바스크립트'), '자스'),
    ((SELECT id FROM public.tags WHERE name = '파이썬'), 'Python'),
    ((SELECT id FROM public.tags WHERE name = '파이썬'), 'py'),
    ((SELECT id FROM public.tags WHERE name = '데이터베이스'), 'DB'),
    ((SELECT id FROM public.tags WHERE name = '알고리즘'), '알고'),
    ((SELECT id FROM public.tags WHERE name = '자료구조'), '자구'),
    ((SELECT id FROM public.tags WHERE name = '웹개발'), '웹'),
    ((SELECT id FROM public.tags WHERE name = '머신러닝'), 'ML'),
    ((SELECT id FROM public.tags WHERE name = '머신러닝'), '기계학습'),
    ((SELECT id FROM public.tags WHERE name = '클라우드컴퓨팅'), 'Cloud'),
    ((SELECT id FROM public.tags WHERE name = '모바일개발'), 'Mobile'),
    ((SELECT id FROM public.tags WHERE name = '프론트엔드'), 'Frontend'),
    ((SELECT id FROM public.tags WHERE name = '백엔드'), 'Backend'),
    ((SELECT id FROM public.tags WHERE name = '데이터분석'), 'Data Analysis'),
    ((SELECT id FROM public.tags WHERE name = '컴퓨터비전'), 'CV'),
    ((SELECT id FROM public.tags WHERE name = '자연어처리'), 'NLP'),
    ((SELECT id FROM public.tags WHERE name = '블록체인'), 'Blockchain');

-- 7. 퀴즈 생성 (데일리 퀴즈)
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (seq * INTERVAL '1 day'),
    NOW() - (seq * INTERVAL '1 day'),
    '오늘의 CS 퀴즈 #' || (20-seq),
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
    (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
    floor(random() * 100 + 50), -- 50-150 시도 횟수
    random() * 40 + 60, -- 60-100 평균 점수
    floor(random() * 500 + 200), -- 200-700 조회수
    CASE
        WHEN seq = 0 THEN (NOW() + INTERVAL '1 day') -- 오늘의 퀴즈는 내일까지 유효
        ELSE (NOW() - ((seq-1) * INTERVAL '1 day')) -- 과거 퀴즈는 이미 만료됨
        END
FROM generate_series(0, 19) AS seq;

-- 8. 퀴즈 생성 (주제별 퀴즈)
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
    (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
    floor(random() * 80 + 20), -- 20-100 시도 횟수
    random() * 35 + 65, -- 65-100 평균 점수
    floor(random() * 400 + 100), -- 100-500 조회수
    NULL::timestamp -- 주제별 퀴즈는 만료일 없음
FROM generate_series(0, 40) AS seq;

-- 9. 퀴즈 생성 (커스텀 퀴즈)
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
    (SELECT id FROM public.users WHERE id != 1 ORDER BY random() LIMIT 1), -- 랜덤 사용자 (관리자 제외)
    floor(random() * 50 + 5), -- 5-55 시도 횟수
    random() * 30 + 65, -- 65-95 평균 점수
    floor(random() * 200 + 30), -- 30-230 조회수
    NULL::timestamp -- 커스텀 퀴즈는 만료일 없음
FROM generate_series(0, 19) AS seq;

-- 10. 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT
    q.id,
    t.id
FROM public.quizzes q
         CROSS JOIN LATERAL (
    SELECT id FROM public.tags
    WHERE (
              CASE
                  -- 퀴즈 제목 기반 태그 매핑
                  WHEN q.title LIKE '%자바스크립트%' AND name = '자바스크립트' THEN true
                  WHEN q.title LIKE '%파이썬%' AND name = '파이썬' THEN true
                  WHEN q.title LIKE '%데이터베이스%' AND name = '데이터베이스' THEN true
                  WHEN q.title LIKE '%SQL%' AND name = '데이터베이스' THEN true
                  WHEN q.title LIKE '%알고리즘%' AND name = '알고리즘' THEN true
                  WHEN q.title LIKE '%자료구조%' AND name = '자료구조' THEN true
                  WHEN q.title LIKE '%시스템%' AND name = '시스템설계' THEN true
                  WHEN q.title LIKE '%네트워킹%' AND name = '네트워크' THEN true
                  WHEN q.title LIKE '%네트워크%' AND name = '네트워크' THEN true
                  WHEN q.title LIKE '%운영체제%' AND name = '운영체제' THEN true
                  WHEN q.title LIKE '%웹%' AND name = '웹개발' THEN true
                  WHEN q.title LIKE '%데브옵스%' AND name = '데브옵스' THEN true
                  WHEN q.title LIKE '%머신러닝%' AND name = '머신러닝' THEN true
                  WHEN q.title LIKE '%보안%' AND name = '보안' THEN true
                  WHEN q.title LIKE '%클라우드%' AND name = '클라우드컴퓨팅' THEN true
                  WHEN q.title LIKE '%모바일%' AND name = '모바일개발' THEN true
                  WHEN q.title LIKE '%프론트엔드%' AND name = '프론트엔드' THEN true
                  WHEN q.title LIKE '%리액트%' AND name = '프론트엔드' THEN true
                  WHEN q.title LIKE '%백엔드%' AND name = '백엔드' THEN true
                  WHEN q.title LIKE '%데이터 분석%' AND name = '데이터분석' THEN true
                  WHEN q.title LIKE '%시각화%' AND name = '데이터분석' THEN true
                  WHEN q.title LIKE '%컴퓨터 비전%' AND name = '컴퓨터비전' THEN true
                  WHEN q.title LIKE '%이미지%' AND name = '컴퓨터비전' THEN true
                  WHEN q.title LIKE '%자연어%' AND name = '자연어처리' THEN true
                  WHEN q.title LIKE '%텍스트%' AND name = '자연어처리' THEN true
                  WHEN q.title LIKE '%블록체인%' AND name = '블록체인' THEN true

                  -- 퀴즈 타입 기반 기본 태그 매핑
                  WHEN q.quiz_type = 'DAILY' AND name = '웹개발' THEN true

                  -- 매칭되지 않는 경우 웹개발 태그 기본 적용
                  WHEN NOT EXISTS (
                      SELECT 1 FROM public.quiz_tags
                      WHERE quiz_id = q.id
                  ) AND name = '웹개발' THEN true

                  ELSE false
                  END
              )
    ORDER BY random()
    LIMIT 1
    ) AS t;

-- 하위 태그도 추가
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT DISTINCT
    q.id,
    sub_t.id
FROM public.quizzes q
         JOIN public.quiz_tags pt ON q.id = pt.quiz_id
         JOIN public.tags t ON pt.tag_id = t.id
         JOIN public.tags sub_t ON sub_t.parent_id = t.id
WHERE
    (q.title LIKE '%리액트%' AND sub_t.name = '리액트') OR
    (q.title LIKE '%앵귤러%' AND sub_t.name = '앵귤러') OR
    (q.title LIKE '%뷰%' AND sub_t.name = '뷰') OR
    (q.title LIKE '%노드%' AND sub_t.name = '노드JS') OR
    (q.title LIKE '%GraphQL%' AND sub_t.name = 'GraphQL') OR
    (q.title LIKE '%API%' AND sub_t.name = 'RESTful API')
        AND NOT EXISTS (
        SELECT 1 FROM public.quiz_tags
        WHERE quiz_id = q.id AND tag_id = sub_t.id
    );

-- 11. 자바스크립트 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
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
        ELSE NULL
        END

FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%자바스크립트%' OR q.title LIKE '%JS%' OR
             (q.quiz_type = 'CUSTOM' AND q.title LIKE '%프론트엔드%') OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND t.name = '자바스크립트'))
         LIMIT 10
     ) AS quiz_questions;

-- 12. 파이썬 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
        WHEN 0 THEN $$["my_list = [1, 2, 3]", "my_list = list(1, 2, 3)", "my_list = array(1, 2, 3)", "my_list = (1, 2, 3)"]$$::jsonb
        WHEN 1 THEN $$["for key, value in my_dict.items():", "for key, value in my_dict:", "for item in my_dict.items():", "for key in my_dict:"]$$::jsonb
        WHEN 2 THEN $$["[x ** 2 for x in range(10)]", "[for x in range(10): x ** 2]", "[x ** 2 in range(10)]", "[x for x ** 2 in range(10)]"]$$::jsonb
        WHEN 3 THEN $$["print(f\"{name} is good\")", "print(\"{} is good\".format(name))", "print(\"%s is good\" % name)", "print(f\"{} is good\")"]$$::jsonb
        WHEN 4 THEN $$["가변 개수의 위치 인자를 받는 매개변수", "가변 개수의 키워드 인자를 받는 매개변수", "기본값이 있는 매개변수", "위치 인자만 받는 매개변수"]$$::jsonb
        WHEN 5 THEN $$["class MyClass:", "Class MyClass {", "class MyClass()", "define class MyClass:"]$$::jsonb
        WHEN 6 THEN $$["try: ... except: ...", "try: ... catch: ...", "try { ... } catch { ... }", "if error: ... else: ..."]$$::jsonb
        WHEN 7 THEN $$["import module as", "from module import function", "import module", "from module import *"]$$::jsonb
        WHEN 8 THEN $$["yield", "generate", "return", "await"]$$::jsonb
        WHEN 9 THEN $$["print(f\"값: {num}\")", "print(\"값: \" + num)", "print(\"값: %d\" % num)", "print(\"값: \" + str(num))"]$$::jsonb
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
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
        ELSE NULL
        END
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%파이썬%' OR q.title LIKE '%Python%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND t.name = '파이썬'))
         LIMIT 10
     ) AS quiz_questions;

-- 13. 데이터베이스 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
        WHEN 0 THEN $$["DELETE는 조건절을 사용할 수 없다", "TRUNCATE는 인덱스를 삭제한다", "DELETE는 트리거를 발생시킨다", "TRUNCATE는 롤백이 불가능하다"]$$::jsonb
        WHEN 1 THEN $$["데이터 중복 최소화", "쿼리 성능 향상", "데이터베이스 크기 증가", "외래 키 제약 완화"]$$::jsonb
        WHEN 2 THEN $$["원자성", "일관성", "격리성", "확장성"]$$::jsonb
        WHEN 3 THEN $$["MySQL", "Oracle", "PostgreSQL", "MongoDB"]$$::jsonb
        WHEN 4 THEN $$["COUNT", "AVG", "TOP", "SUM"]$$::jsonb
        WHEN 5 THEN $$["검색 속도 향상", "데이터 무결성 보장", "트랜잭션 관리", "데이터 압축"]$$::jsonb
        WHEN 6 THEN $$["INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "COMBINE JOIN"]$$::jsonb
        WHEN 7 THEN $$["FINISH", "COMMIT", "SAVE", "END"]$$::jsonb
        WHEN 8 THEN $$["MySQL", "SQLite", "PostgreSQL", "Redis"]$$::jsonb
        WHEN 9 THEN $$["외부 쿼리부터 내부 서브쿼리 순으로 실행", "내부 서브쿼리부터 외부 쿼리 순으로 실행", "모든 쿼리가 동시에 실행", "쿼리 최적화기가 결정한 순서대로 실행"]$$::jsonb
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
    CASE
        WHEN mod(seq, 20) = 8 THEN
            '-- 다음 SQL 쿼리의 결과는?
            SELECT department, COUNT(*) as emp_count
            FROM employees
            WHERE salary > 50000
            GROUP BY department
            HAVING COUNT(*) > 5
            ORDER BY emp_count DESC;'
        ELSE NULL
        END
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%데이터베이스%' OR q.title LIKE '%SQL%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND t.name = '데이터베이스'))
         LIMIT 10
     ) AS quiz_questions;
-- 14. 알고리즘/자료구조 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
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
        ELSE NULL
        END
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%알고리즘%' OR q.title LIKE '%자료구조%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND (t.name = '알고리즘' OR t.name = '자료구조')))
         LIMIT 10
     ) AS quiz_questions;

-- 15. 웹개발 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
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
        ELSE NULL
        END
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%웹%' OR
             q.title LIKE '%프론트엔드%' OR
             q.title LIKE '%백엔드%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND (t.name = '웹개발' OR t.name = '프론트엔드' OR t.name = '백엔드')))
         LIMIT 15
     ) AS quiz_questions;

-- 16. 네트워크 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
    NULL
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%네트워크%' OR q.title LIKE '%네트워킹%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND t.name = '네트워크'))
         LIMIT 10
     ) AS quiz_questions;

-- 17. 운영체제 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
END,
    quiz_id,
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
        ELSE NULL
END
    FROM (
             SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
             FROM public.quizzes q
             WHERE q.title LIKE '%운영체제%' OR q.title LIKE '%OS%' OR
                 (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                          WHERE pt.quiz_id = q.id AND t.name = '운영체제'))
             LIMIT 10
         ) AS quiz_questions;

-- 18. 보안 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
    CASE
        WHEN mod(seq, 20) = 7 THEN
            '// 다음 코드는 SQL 인젝션 공격의 취약점을 시뮬레이션하는 예제입니다.
            function executeQuery(query) {
                // 사용자 입력을 검증하지 않으면 보안 위험이 발생할 수 있습니다.
                return database.run(query);
}'
        ELSE NULL
        END
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%보안%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND t.name = '보안'))
         LIMIT 10
     ) AS quiz_questions;

-- 19. 머신러닝/데이터분석 관련 문제 생성
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
    (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id),
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
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 5
        WHEN 'INTERMEDIATE' THEN 10
        ELSE 15
        END,
    CASE (SELECT difficulty_level FROM public.quizzes WHERE id = quiz_id)
        WHEN 'BEGINNER' THEN 30
        WHEN 'INTERMEDIATE' THEN 45
        ELSE 60
        END,
    quiz_id,
    NULL
FROM (
         SELECT q.id as quiz_id, generate_series(1, q.question_count) as seq
         FROM public.quizzes q
         WHERE q.title LIKE '%머신러닝%' OR q.title LIKE '%데이터 분석%' OR q.title LIKE '%ML%' OR
             (EXISTS (SELECT 1 FROM public.quiz_tags pt JOIN public.tags t ON pt.tag_id = t.id
                      WHERE pt.quiz_id = q.id AND (t.name = '머신러닝' OR t.name = '데이터분석')))
         LIMIT 10
     ) AS quiz_questions;

-- 20. 퀴즈 시도 데이터 생성
INSERT INTO public.quiz_attempts (
    created_at, start_time, end_time, score, is_completed, time_taken,
    user_id, quiz_id
)
SELECT
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '60 days')),
    (NOW() - (random() * INTERVAL '59 days')),
    floor(random() * 60 + 40), -- 40-100점 사이 점수
    true, -- 완료된 시도
    floor(random() * 900 + 100), -- 100-1000초 소요 시간
    (SELECT id FROM public.users ORDER BY random() LIMIT 1),
    (SELECT id FROM public.quizzes ORDER BY random() LIMIT 1)
FROM generate_series(1, 50) -- 50개의 퀴즈 시도 생성
LIMIT 50;

-- 21. 퀴즈 시도에 따른 문제 시도 데이터 생성
INSERT INTO public.question_attempts (
    created_at, time_taken, is_correct, user_answer,
    quiz_attempt_id, question_id
)
SELECT
    (NOW() - (random() * INTERVAL '59 days')),
    floor(random() * 60 + 5), -- 5-65초 소요 시간
    CASE WHEN random() < 0.7 THEN true ELSE false END, -- 70% 확률로 정답
    CASE
        WHEN random() < 0.7 THEN -- 70% 확률로 정답
            (SELECT correct_answer FROM public.questions WHERE id = (
                SELECT id FROM public.questions WHERE quiz_id = (
                    SELECT quiz_id FROM public.quiz_attempts WHERE id = qa.id
                ) ORDER BY random() LIMIT 1
            ))
        ELSE '잘못된 답변' -- 오답
        END,
    qa.id, -- 퀴즈 시도 ID
    (SELECT id FROM public.questions WHERE quiz_id = (
        SELECT quiz_id FROM public.quiz_attempts WHERE id = qa.id
    ) ORDER BY random() LIMIT 1) -- 해당 퀴즈의 랜덤 문제
FROM public.quiz_attempts qa
         CROSS JOIN generate_series(1, 5) -- 각 퀴즈 시도마다 5개의 문제 시도
LIMIT 200;

-- 22. 퀴즈 리뷰 생성
INSERT INTO public.quiz_reviews (
    created_at, rating, content, quiz_id, reviewer_id
)
SELECT
    NOW() - (random() * INTERVAL '30 days'),
    floor(random() * 3 + 3), -- 3-5점 점수
    CASE floor(random() * 5)
        WHEN 0 THEN '매우 유익한 퀴즈였습니다. 특히 실무에 적용 가능한 내용이 좋았습니다.'
        WHEN 1 THEN '난이도가 적절하고 내용이 알찬 퀴즈입니다.'
        WHEN 2 THEN '문제의 설명이 명확해서 학습하기 좋았습니다.'
        WHEN 3 THEN '개념을 정리하는데 많은 도움이 되었습니다. 다음 퀴즈도 기대합니다.'
        ELSE '문제 구성이 체계적이고 학습에 효과적입니다.'
        END,
    (SELECT id FROM public.quizzes ORDER BY random() LIMIT 1),
    (SELECT id FROM public.users WHERE role = 'USER' ORDER BY random() LIMIT 1)
FROM generate_series(1, 30) -- 30개의 퀴즈 리뷰 생성
LIMIT 30;

-- 23. 퀴즈 리뷰 댓글 생성
INSERT INTO public.quiz_review_comments (
    created_at, content, parent_review_id, commenter_id
)
SELECT
    NOW() - (random() * INTERVAL '15 days'),
    CASE floor(random() * 5)
        WHEN 0 THEN '리뷰 감사합니다. 피드백을 반영하여 더 좋은 퀴즈를 만들겠습니다.'
        WHEN 1 THEN '의견 공유해주셔서 감사합니다.'
        WHEN 2 THEN '다음 버전에서 개선하도록 하겠습니다.'
        WHEN 3 THEN '정확한 피드백 감사합니다. 많은 도움이 됩니다.'
        ELSE '좋은 평가 감사합니다!'
        END,
    (SELECT id FROM public.quiz_reviews ORDER BY random() LIMIT 1),
    CASE
        WHEN random() < 0.7 THEN (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1)
        ELSE (SELECT id FROM public.users WHERE id != (SELECT reviewer_id FROM public.quiz_reviews ORDER BY random() LIMIT 1) ORDER BY random() LIMIT 1)
        END
FROM generate_series(1, 15) -- 15개의 댓글 생성
LIMIT 15;

-- 24. 사용자 레벨 데이터 생성
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
FROM public.users u
WHERE NOT EXISTS (SELECT 1 FROM public.user_levels WHERE user_id = u.id)
RETURNING id, user_id, level;

-- 25. 사용자 업적 이력 생성
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
         SELECT id as user_id FROM public.users ORDER BY random() LIMIT 4
     ) as random_users
         CROSS JOIN generate_series(1, 3) -- 각 사용자마다 평균 3개의 업적
LIMIT 12;

-- 사용자 업적 데이터 생성
INSERT INTO public.user_achievements (user_level_id, achievements)
SELECT
    ul.id,
    CASE floor(random() * 4)
        WHEN 0 THEN 'QUIZ_MASTER'
        WHEN 1 THEN 'DAILY_CHAMPION'
        WHEN 2 THEN 'KNOWLEDGE_SEEKER'
        ELSE 'QUICK_SOLVER'
        END
FROM public.user_levels ul
LIMIT 10;

-- 더미 데이터 생성 완료 확인
SELECT 'CS 퀴즈 플랫폼 더미 데이터 생성 완료' as result;-- CS-Quiz 플랫폼을 위한 간소화된 더미 데이터