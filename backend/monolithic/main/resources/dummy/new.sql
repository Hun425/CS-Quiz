-- 새로운 더미 데이터 추가 (프론트엔드, 백엔드, 네트워크)

-- 0. 필수 의존성 데이터 생성 (관리자 사용자, 기본 태그들)
-- 관리자 사용자 생성 (없는 경우에만)
INSERT INTO public.users (
    email, username, experience, is_active, level, provider, provider_id,
    required_experience, role, total_points, created_at, updated_at, profile_image,
    last_login, access_token, refresh_token, token_expires_at
)
SELECT 
    'admin@example.com', 'admin', 800, true, 10, 'GITHUB', 'admin_id_123',
    1000, 'ADMIN', 5000, NOW(), NOW(),
    'https://robohash.org/admin?set=set4', NOW() - INTERVAL '2 days',
    NULL, NULL, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM public.users WHERE role = 'ADMIN'
);

-- 1. 필요한 태그 추가 (프론트엔드, 백엔드, 네트워크)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '프론트엔드', '프론트엔드 웹 개발 관련 기술 및 개념 (HTML, CSS, JavaScript, 프레임워크 등)'),
    (NOW(), '백엔드', '백엔드 개발 관련 기술 및 개념 (서버, 데이터베이스, API, 프레임워크 등)'),
    (NOW(), '네트워크', '네트워킹 프로토콜, 모델 및 인프라')
ON CONFLICT (name) DO NOTHING; -- 이미 존재하면 추가하지 않음

-- 2. 새로운 퀴즈 추가 (프론트엔드, 백엔드, 네트워크 - 각 초급/중급 1개씩, 총 6개 퀴즈)

-- 관리자 ID 및 필요한 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1 -- dummy_data.sql 에서 ADMIN 역할 사용자 생성 참고
), frontend_tag AS (
    SELECT id FROM public.tags WHERE name = '프론트엔드'
), backend_tag AS (
    SELECT id FROM public.tags WHERE name = '백엔드'
), network_tag AS (
    SELECT id FROM public.tags WHERE name = '네트워크' -- dummy_data.sql 에 정의된 태그 사용
),
-- 퀴즈 생성
     inserted_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 프론트엔드 초급
                 (NOW() - INTERVAL '5 days', NOW(), '프론트엔드 기초 개념 퀴즈', 'HTML, CSS, JavaScript 기초 지식을 묻는 퀴즈입니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 50 + 10), random() * 30 + 60, floor(random() * 300 + 50), NULL),
                 -- 프론트엔드 중급
                 (NOW() - INTERVAL '4 days', NOW(), '프론트엔드 심화 기술 퀴즈', 'JS 심화, 브라우저 API 및 프레임워크 관련 질문입니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 40 + 10), random() * 25 + 65, floor(random() * 200 + 40), NULL),
                 -- 백엔드 초급
                 (NOW() - INTERVAL '3 days', NOW(), '백엔드 개발 입문 퀴즈', '서버, API, 데이터베이스 기본 개념을 확인합니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 60 + 15), random() * 30 + 60, floor(random() * 350 + 60), NULL),
                 -- 백엔드 중급
                 (NOW() - INTERVAL '2 days', NOW(), '백엔드 심화 기술 면접 준비', '인증, DB 최적화, 캐싱 등 백엔드 심화 기술 관련 질문입니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 40, (SELECT id FROM admin_user), floor(random() * 45 + 10), random() * 20 + 70, floor(random() * 250 + 45), NULL),
                 -- 네트워크 초급
                 (NOW() - INTERVAL '1 day', NOW(), '네트워크 기초 용어 퀴즈', 'OSI 7계층, TCP/IP 등 기본적인 네트워크 용어를 다룹니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 70 + 20), random() * 25 + 65, floor(random() * 400 + 70), NULL),
                 -- 네트워크 중급
                 (NOW(), NOW(), '네트워크 프로토콜 심층 분석', 'HTTP, DNS, TCP 등 주요 프로토콜의 동작 원리를 묻습니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 55 + 15), random() * 20 + 70, floor(random() * 300 + 55), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, ft.id FROM inserted_quizzes iq, frontend_tag ft WHERE iq.title LIKE '프론트엔드%'
             UNION ALL
             SELECT iq.id, bt.id FROM inserted_quizzes iq, backend_tag bt WHERE iq.title LIKE '백엔드%'
             UNION ALL
             SELECT iq.id, nt.id FROM inserted_quizzes iq, network_tag nt WHERE iq.title LIKE '네트워크%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (퀴즈별 10개씩, 총 60개)

-- 프론트엔드 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '프론트엔드 기초 개념 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'HTML에서 하이퍼링크를 만드는 태그는?', 'MULTIPLE_CHOICE', 'BEGINNER', '<a>', '["<img>", "<a>", "<p>", "<div>"]'::jsonb, '<a> 태그는 다른 웹 페이지나 같은 페이지의 다른 위치로 연결하는 하이퍼링크를 정의합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CSS에서 요소의 배경색을 지정하는 속성은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'background-color', '["color", "font-size", "background-color", "border"]'::jsonb, '`background-color` 속성은 요소의 배경색을 설정합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'JavaScript에서 변수를 선언할 때 사용하지 **않는** 키워드는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'declare', '["var", "let", "const", "declare"]'::jsonb, '`var`, `let`, `const`는 JavaScript에서 변수를 선언하는 키워드입니다. `declare`는 사용되지 않습니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTML 문서의 기본 구조를 정의하는 최상위 태그는?', 'MULTIPLE_CHOICE', 'BEGINNER', '<html>', '["<body>", "<head>", "<html>", "<title>"]'::jsonb, '`<html>` 태그는 전체 HTML 문서를 감싸는 루트 요소입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CSS에서 글자 크기를 조절하는 속성은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'font-size', '["font-weight", "font-family", "font-size", "text-align"]'::jsonb, '`font-size` 속성은 텍스트의 크기를 지정합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTML에서 순서 없는 목록(Unordered List)을 만드는 태그는?', 'MULTIPLE_CHOICE', 'BEGINNER', '<ul>', '["<ol>", "<li>", "<ul>", "<dl>"]'::jsonb, '`<ul>` 태그는 순서가 중요하지 않은 목록을 나타냅니다. 각 항목은 `<li>` 태그를 사용합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CSS에서 요소를 화면에 보이지 않게 숨기는 속성은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'display: none;', '["visibility: hidden;", "display: none;", "opacity: 0;", "position: absolute; left: -9999px;"]'::jsonb, '`display: none;`은 요소를 렌더링 트리에서 제거하여 화면에 표시되지 않고 공간도 차지하지 않게 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'JavaScript의 기본 데이터 타입(Primitive type)이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'Object', '["String", "Number", "Boolean", "Object"]'::jsonb, 'JavaScript의 원시 타입은 String, Number, Boolean, Null, Undefined, Symbol, BigInt 7가지입니다. Object는 참조 타입입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTML에서 이미지를 삽입할 때 사용하는 태그는?', 'MULTIPLE_CHOICE', 'BEGINNER', '<img>', '["<image>", "<img>", "<picture>", "<src>"]'::jsonb, '`<img>` 태그는 HTML 문서에 이미지를 삽입하는 데 사용됩니다. `src` 속성으로 이미지 경로를 지정합니다.', 5, 30, (SELECT id FROM quiz_info));

-- 프론트엔드 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '프론트엔드 심화 기술 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'JavaScript에서 비동기 작업을 처리하는 방식이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Synchronous Loop', '["Callback Function", "Promise", "Async/Await", "Synchronous Loop"]'::jsonb, '콜백 함수, 프로미스, async/await는 JavaScript에서 비동기 작업을 처리하는 대표적인 방법입니다. 동기 루프는 비동기 처리 방식이 아닙니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'React에서 컴포넌트 간 데이터 전달 방식으로 적절하지 않은 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Direct DOM Manipulation', '["Props (Properties)", "Context API", "State Management Library (Redux, Zustand)", "Direct DOM Manipulation"]'::jsonb, 'React는 가상 DOM을 사용하여 효율적으로 UI를 업데이트하며, 직접적인 DOM 조작은 권장되지 않습니다. Props, Context API, 상태 관리 라이브러리를 통해 데이터를 전달합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '브라우저 저장소 중 세션 기간 동안만 데이터가 유지되는 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Session Storage', '["Local Storage", "Session Storage", "Cookies", "IndexedDB"]'::jsonb, 'Session Storage는 브라우저 탭이나 창이 닫히면 데이터가 사라지지만, Local Storage는 사용자가 직접 삭제하기 전까지 유지됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'CSS에서 Flexbox 레이아웃의 주 축(main axis) 정렬 속성은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'justify-content', '["align-items", "justify-content", "flex-direction", "align-self"]'::jsonb, '`justify-content` 속성은 주 축 방향으로 Flex 아이템들을 정렬하는 방법을 지정합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'JavaScript의 이벤트 위임(Event Delegation)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '상위 요소에 이벤트 리스너를 등록하여 하위 요소의 이벤트를 처리', '["각 하위 요소마다 개별 이벤트 리스너를 등록", "상위 요소에 이벤트 리스너를 등록하여 하위 요소의 이벤트를 처리", "이벤트 버블링을 막는 기술", "이벤트 캡처링만 사용하는 기술"]'::jsonb, '이벤트 위임은 여러 하위 요소의 이벤트를 공통 상위 요소에서 하나의 리스너로 관리하여 성능을 개선하는 기법입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '웹 성능 최적화를 위한 방법으로 거리가 먼 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '모든 스크립트를 동기적으로 로드', '["이미지 압축 및 최적화", "코드 분할(Code Splitting)", "브라우저 캐싱 활용", "모든 스크립트를 동기적으로 로드"]'::jsonb, '스크립트를 동기적으로 로드하면 페이지 렌더링을 차단하여 성능 저하를 유발할 수 있습니다. 비동기 로드나 지연 로드가 권장됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'REST API에서 클라이언트 오류를 나타내는 HTTP 상태 코드 범위는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '4xx', '["2xx", "3xx", "4xx", "5xx"]'::jsonb, '4xx 범위의 상태 코드(예: 400 Bad Request, 404 Not Found)는 클라이언트 측의 오류를 나타냅니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Vue.js에서 컴포넌트의 반응형 데이터를 선언하는 옵션은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'data', '["methods", "computed", "props", "data"]'::jsonb, '`data` 옵션은 Vue 컴포넌트의 반응형 상태를 정의하는 함수 또는 객체를 반환합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '크로스 사이트 스크립팅(XSS) 공격을 방지하기 위한 방법은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '사용자 입력 값을 적절히 이스케이프(escape) 처리', '["HTTP 대신 HTTPS 사용", "강력한 비밀번호 정책 사용", "사용자 입력 값을 적절히 이스케이프(escape) 처리", "서버 측 세션 사용"]'::jsonb, '사용자로부터 입력받은 데이터를 HTML, JavaScript 등으로 해석될 수 없도록 이스케이프 처리하는 것이 XSS 방어의 핵심입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '웹 접근성(Web Accessibility)을 준수하는 방법이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '의미 없는 div와 span만 사용하기', '["키보드만으로 모든 기능 사용 가능하게 하기", "이미지에 alt 속성 제공하기", "적절한 명암 대비 제공하기", "의미 없는 div와 span만 사용하기"]'::jsonb, '시맨틱 HTML 태그(예: `<nav>`, `<article>`, `<button>`)를 사용하여 콘텐츠의 의미 구조를 명확히 하는 것이 웹 접근성에 중요합니다.', 10, 45, (SELECT id FROM quiz_info));


-- 백엔드 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '백엔드 개발 입문 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'HTTP 메서드 중 리소스 생성을 주로 요청하는 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'POST', '["GET", "POST", "PUT", "DELETE"]'::jsonb, 'POST 메서드는 주로 새로운 리소스를 생성하거나 서버에 데이터를 제출하는 데 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '관계형 데이터베이스(RDBMS)가 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'MongoDB', '["MySQL", "PostgreSQL", "Oracle", "MongoDB"]'::jsonb, 'MongoDB는 문서 지향 NoSQL 데이터베이스입니다. 나머지는 관계형 데이터베이스입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'API(Application Programming Interface)의 주요 역할은?', 'MULTIPLE_CHOICE', 'BEGINNER', '소프트웨어 컴포넌트 간의 상호작용 정의', '["사용자 인터페이스 디자인", "데이터베이스 스키마 설계", "소프트웨어 컴포넌트 간의 상호작용 정의", "운영체제 커널 관리"]'::jsonb, 'API는 서로 다른 소프트웨어 시스템이나 컴포넌트가 정해진 규칙에 따라 통신하고 상호작용할 수 있도록 하는 인터페이스입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '서버 측 언어가 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'HTML', '["Java", "Python", "Node.js", "HTML"]'::jsonb, 'HTML은 웹 페이지의 구조를 정의하는 마크업 언어로, 주로 클라이언트 측(브라우저)에서 해석됩니다. Java, Python, Node.js는 서버 측에서 실행될 수 있는 언어/환경입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스에서 데이터의 고유 식별자로 사용되는 키는?', 'MULTIPLE_CHOICE', 'BEGINNER', '기본 키 (Primary Key)', '["외래 키 (Foreign Key)", "기본 키 (Primary Key)", "후보 키 (Candidate Key)", "슈퍼 키 (Super Key)"]'::jsonb, '기본 키는 테이블 내의 각 레코드를 고유하게 식별하는 데 사용되는 컬럼 또는 컬럼 집합입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'REST(Representational State Transfer)의 기본 원칙이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '상태 유지 (Stateful)', '["클라이언트-서버 구조", "무상태 (Stateless)", "캐시 가능 (Cacheable)", "상태 유지 (Stateful)"]'::jsonb, 'REST는 무상태(Stateless) 아키텍처 스타일입니다. 서버는 클라이언트의 상태를 저장하지 않으며, 각 요청은 필요한 모든 정보를 포함해야 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '웹 서버(Web Server)의 주된 역할은?', 'MULTIPLE_CHOICE', 'BEGINNER', '정적 컨텐츠(HTML, CSS, 이미지) 제공', '["동적 컨텐츠 생성 및 처리", "데이터베이스 관리", "정적 컨텐츠(HTML, CSS, 이미지) 제공", "비즈니스 로직 실행"]'::jsonb, '웹 서버는 주로 클라이언트로부터 HTTP 요청을 받아 HTML 파일, 이미지, CSS 등 정적 파일을 제공하는 역할을 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'JSON(JavaScript Object Notation)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '데이터 교환 형식으로 널리 사용됨', '["JavaScript에서만 사용 가능", "XML보다 복잡한 구조 표현", "데이터 교환 형식으로 널리 사용됨", "HTML 태그 포함 가능"]'::jsonb, 'JSON은 사람이 읽고 쓰기 쉬우며 기계가 분석하고 생성하기 쉬운 경량 데이터 교환 형식입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTTP 요청 시, 요청에 대한 부가 정보를 담는 부분은?', 'MULTIPLE_CHOICE', 'BEGINNER', '헤더 (Header)', '["메서드 (Method)", "URL", "헤더 (Header)", "바디 (Body)"]'::jsonb, 'HTTP 헤더는 요청 또는 응답에 대한 메타데이터(예: 콘텐츠 타입, 캐시 제어, 인증 정보)를 포함합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 트랜잭션(Transaction)의 특징이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '분산성 (Distribution)', '["원자성 (Atomicity)", "일관성 (Consistency)", "격리성 (Isolation)", "분산성 (Distribution)"]'::jsonb, '데이터베이스 트랜잭션의 4가지 주요 특징(ACID)은 원자성, 일관성, 격리성, 지속성(Durability)입니다. 분산성은 ACID 속성이 아닙니다.', 5, 30, (SELECT id FROM quiz_info));

-- 백엔드 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '백엔드 심화 기술 면접 준비' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '데이터베이스 인덱스(Index)를 사용하는 주된 이유는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '검색 성능 향상', '["데이터 무결성 보장", "저장 공간 절약", "데이터 암호화", "검색 성능 향상"]'::jsonb, '인덱스는 테이블의 특정 컬럼(들)에 대한 데이터의 저장 위치를 미리 정렬된 형태로 가지고 있어, 해당 컬럼을 조건으로 하는 검색(SELECT) 쿼리의 속도를 크게 향상시킵니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'JWT(JSON Web Token)의 구성 요소가 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Encryption Key', '["Header", "Payload", "Signature", "Encryption Key"]'::jsonb, 'JWT는 헤더(Header), 페이로드(Payload), 서명(Signature) 세 부분으로 구성됩니다. 암호화 키는 JWT 표준 구성 요소가 아닙니다 (선택적으로 페이로드를 암호화할 수는 있음).', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '캐싱(Caching) 전략 중 가장 최근에 사용된 데이터를 우선적으로 유지하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'LRU (Least Recently Used)', '["FIFO (First-In, First-Out)", "LFU (Least Frequently Used)", "LRU (Least Recently Used)", "Random"]'::jsonb, 'LRU 알고리즘은 캐시 메모리가 가득 찼을 때 가장 오랫동안 참조되지 않은(가장 최근에 사용되지 않은) 데이터를 교체하는 방식입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '메시지 큐(Message Queue)를 사용하는 주된 목적이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '실시간 동기 통신', '["비동기 처리", "시스템 간의 결합도 감소", "트래픽 버퍼링", "실시간 동기 통신"]'::jsonb, '메시지 큐는 시스템 간 비동기 통신, 부하 분산, 서비스 간 결합도 감소 등을 위해 사용됩니다. 실시간 동기 통신과는 거리가 멉니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'ORM(Object-Relational Mapping)의 장점이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '항상 최적의 SQL 성능 보장', '["객체 지향적 코드 작성 가능", "데이터베이스 종속성 감소", "생산성 향상", "항상 최적의 SQL 성능 보장"]'::jsonb, 'ORM은 개발 생산성을 높이고 특정 DB에 대한 종속성을 줄여주지만, 자동 생성된 SQL이 복잡한 쿼리에서는 직접 작성한 SQL보다 성능이 떨어질 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '로드 밸런서(Load Balancer)의 역할이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '데이터베이스 스키마 관리', '["서버 부하 분산", "서버 장애 감지 및 대체", "SSL 암호화/복호화 처리", "데이터베이스 스키마 관리"]'::jsonb, '로드 밸런서는 여러 서버에 걸쳐 네트워크 트래픽을 분산시키는 역할을 하며, 스키마 관리와는 관련이 없습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 정규화(Normalization) 과정에서 발생하는 단점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '쿼리 성능 저하 가능성 (JOIN 연산 증가)', '["데이터 중복 증가", "데이터 무결성 저하", "쿼리 성능 저하 가능성 (JOIN 연산 증가)", "테이블 개수 감소"]'::jsonb, '정규화는 데이터 중복을 줄이지만, 여러 테이블로 분리되면서 원하는 데이터를 얻기 위해 JOIN 연산이 많아져 쿼리 성능이 저하될 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'MSA(Microservice Architecture)의 특징이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '강한 결합 (Tightly Coupled)', '["독립적인 배포 가능", "기술 다양성 허용", "강한 결합 (Tightly Coupled)", "서비스별 데이터베이스 관리"]'::jsonb, 'MSA는 작고 독립적인 서비스들로 시스템을 구성하여 서비스 간의 결합도를 낮추는(Loosely Coupled) 것을 지향합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '동시성 제어(Concurrency Control) 기법 중 하나로, 트랜잭션이 데이터에 접근하기 전에 잠금(Lock)을 거는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '비관적 잠금 (Pessimistic Locking)', '["낙관적 잠금 (Optimistic Locking)", "비관적 잠금 (Pessimistic Locking)", "타임스탬프 순서화 (Timestamp Ordering)", "다중 버전 동시성 제어 (MVCC)"]'::jsonb, '비관적 잠금은 트랜잭션 간 충돌이 발생할 가능성이 높다고 가정하고, 데이터 접근 전에 미리 잠금을 설정하여 충돌을 예방하는 방식입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'OAuth 2.0에서 클라이언트가 사용자의 권한을 위임받아 보호된 리소스에 접근하기 위해 사용하는 토큰은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Access Token', '["Refresh Token", "ID Token", "Access Token", "Authorization Code"]'::jsonb, 'Access Token은 클라이언트가 특정 범위의 보호된 리소스에 접근할 수 있는 권한을 나타내는 자격 증명입니다.', 10, 45, (SELECT id FROM quiz_info));


-- 네트워크 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '네트워크 기초 용어 퀴즈' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'OSI 7계층 모델에서 데이터 링크 계층(Data Link Layer)의 주요 역할은?', 'MULTIPLE_CHOICE', 'BEGINNER', '인접 노드 간 신뢰성 있는 데이터 전송', '["데이터 압축 및 암호화", "패킷의 경로 설정", "물리적 연결 관리", "인접 노드 간 신뢰성 있는 데이터 전송"]'::jsonb, '데이터 링크 계층은 물리적 링크를 통해 연결된 인접한 두 노드 사이에서 오류 제어와 흐름 제어를 수행하며 신뢰성 있는 데이터 프레임 전송을 담당합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '컴퓨터 네트워크에서 각 장치를 고유하게 식별하는 물리적인 주소는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'MAC 주소', '["IP 주소", "MAC 주소", "포트 번호", "서브넷 마스크"]'::jsonb, 'MAC(Media Access Control) 주소는 네트워크 인터페이스 카드(NIC)에 할당된 고유한 하드웨어 주소입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '인터넷에서 도메인 이름(예: www.google.com)을 IP 주소로 변환해주는 시스템은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'DNS (Domain Name System)', '["DHCP (Dynamic Host Configuration Protocol)", "DNS (Domain Name System)", "NAT (Network Address Translation)", "ARP (Address Resolution Protocol)"]'::jsonb, 'DNS는 사람이 기억하기 쉬운 도메인 이름을 컴퓨터가 통신에 사용하는 IP 주소로 변환하는 역할을 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'TCP와 UDP의 가장 큰 차이점은?', 'MULTIPLE_CHOICE', 'BEGINNER', '연결 지향성 및 신뢰성 여부', '["전송 속도", "헤더 크기", "연결 지향성 및 신뢰성 여부", "사용 포트 번호 범위"]'::jsonb, 'TCP는 연결 지향적이며 데이터 전송의 신뢰성을 보장하지만(느림), UDP는 비연결 지향적이고 신뢰성을 보장하지 않는 대신 전송 속도가 빠릅니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'IP 주소와 서브넷 마스크를 사용하여 알 수 있는 정보는?', 'MULTIPLE_CHOICE', 'BEGINNER', '네트워크 주소', '["MAC 주소", "기본 게이트웨이 주소", "DNS 서버 주소", "네트워크 주소"]'::jsonb, 'IP 주소와 서브넷 마스크에 비트 AND 연산을 수행하면 해당 IP 주소가 속한 네트워크의 주소를 알 수 있습니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTTP와 HTTPS의 주요 차이점은?', 'MULTIPLE_CHOICE', 'BEGINNER', '데이터 암호화 여부', '["사용 포트 번호", "데이터 암호화 여부", "요청/응답 속도", "지원하는 브라우저"]'::jsonb, 'HTTPS는 HTTP에 SSL/TLS 프로토콜을 추가하여 통신 내용을 암호화함으로써 보안을 강화한 버전입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '네트워크 장비 중 서로 다른 네트워크 간의 통신을 가능하게 하는 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '라우터 (Router)', '["허브 (Hub)", "스위치 (Switch)", "라우터 (Router)", "리피터 (Repeater)"]'::jsonb, '라우터는 IP 주소를 기반으로 패킷의 최적 경로를 결정하고, 서로 다른 네트워크 대역 간의 데이터 전송을 중개합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'TCP/IP 모델의 응용 계층(Application Layer)에 속하는 프로토콜이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'IP (Internet Protocol)', '["HTTP", "FTP", "SMTP", "IP (Internet Protocol)"]'::jsonb, 'IP는 인터넷 계층(Internet Layer) 프로토콜로, 데이터 패킷의 주소 지정과 라우팅을 담당합니다. HTTP, FTP, SMTP는 응용 계층 프로토콜입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '웹 브라우저 주소창에 URL을 입력했을 때 가장 먼저 일어나는 일은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'DNS 서버에 IP 주소 요청', '["웹 서버에 HTML 요청", "DNS 서버에 IP 주소 요청", "로컬 캐시에서 IP 주소 확인", "TCP 연결 수립"]'::jsonb, '브라우저는 URL의 도메인 이름을 IP 주소로 변환하기 위해 가장 먼저 로컬 캐시를 확인하고, 없으면 DNS 서버에 질의합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '공유기(Router)에서 내부 네트워크의 여러 장치가 하나의 공인 IP 주소를 사용하게 하는 기술은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'NAT (Network Address Translation)', '["DHCP", "DNS", "NAT", "VPN"]'::jsonb, 'NAT는 사설 IP 주소를 사용하는 내부 네트워크 장치들이 외부 인터넷과 통신할 때, 공유기의 공인 IP 주소로 변환해주는 기술입니다.', 5, 30, (SELECT id FROM quiz_info));

-- 네트워크 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '네트워크 프로토콜 심층 분석' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'TCP 연결 설정 과정(3-way handshake)의 순서는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'SYN -> SYN+ACK -> ACK', '["SYN -> ACK -> SYN+ACK", "SYN -> SYN+ACK -> ACK", "ACK -> SYN -> SYN+ACK", "SYN+ACK -> SYN -> ACK"]'::jsonb, '클라이언트가 서버에 SYN 패킷을 보내고, 서버는 SYN+ACK 패킷으로 응답하며, 클라이언트는 마지막으로 ACK 패킷을 보내 연결을 확립합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTTP 메서드 중 서버 리소스의 변경 없이 데이터 조회만을 목적으로 하는 멱등(Idempotent) 메서드는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'GET', '["POST", "PUT", "GET", "PATCH"]'::jsonb, 'GET 메서드는 서버의 데이터를 조회하는 데 사용되며, 여러 번 호출해도 서버 상태에 영향을 주지 않는 멱등성을 가집니다. PUT과 DELETE도 멱등하지만 리소스를 변경할 수 있습니다. POST와 PATCH는 멱등하지 않습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'DNS 레코드 타입 중 도메인 이름에 대한 메일 교환 서버(Mail Exchanger)를 지정하는 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'MX', '["A", "CNAME", "MX", "TXT"]'::jsonb, 'MX 레코드는 특정 도메인으로 이메일을 보낼 때 참조하는 메일 서버의 호스트 이름을 지정합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'IP 주소 192.168.1.100과 서브넷 마스크 255.255.255.0 (/24)이 주어졌을 때, 이 네트워크의 브로드캐스트 주소는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '192.168.1.255', '["192.168.1.0", "192.168.1.1", "192.168.1.254", "192.168.1.255"]'::jsonb, '서브넷 마스크 /24는 네트워크 ID에 24비트, 호스트 ID에 8비트를 사용합니다. 호스트 ID 부분이 모두 1이면 브로드캐스트 주소이므로, 192.168.1.255가 됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'TCP의 혼잡 제어(Congestion Control) 알고리즘의 목표는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '네트워크 혼잡을 감지하고 전송률을 조절', '["데이터 전송 오류율 최소화", "네트워크 혼잡을 감지하고 전송률을 조절", "패킷의 최단 경로 탐색", "연결 설정 시간 단축"]'::jsonb, '혼잡 제어는 네트워크의 혼잡 상태를 파악하고 데이터 전송량을 동적으로 조절하여 네트워크 전체의 성능 저하를 막는 메커니즘입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'ARP (Address Resolution Protocol)의 기능은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'IP 주소를 MAC 주소로 변환', '["MAC 주소를 IP 주소로 변환", "IP 주소를 도메인 이름으로 변환", "IP 주소를 MAC 주소로 변환", "MAC 주소를 포트 번호로 변환"]'::jsonb, 'ARP는 동일 네트워크 대역 내에서 통신할 때, 상대방의 IP 주소를 알고 있지만 물리적인 MAC 주소를 모르는 경우 이를 알아내기 위해 사용되는 프로토콜입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'HTTP 상태 코드 301과 302의 차이점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '301은 영구 이동, 302는 임시 이동', '["301은 성공, 302는 실패", "301은 서버 오류, 302는 클라이언트 오류", "301은 영구 이동, 302는 임시 이동", "301은 캐시 가능, 302는 캐시 불가능"]'::jsonb, '301 Moved Permanently는 요청한 리소스가 영구적으로 새 URL로 이동했음을 나타내며, 302 Found는 일시적으로 다른 URL로 리디렉션됨을 의미합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'TLS/SSL 핸드셰이크 과정에서 서버가 클라이언트에게 주로 제공하는 정보는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '서버의 공개 키가 포함된 인증서', '["클라이언트의 비밀 키", "세션 키", "서버의 공개 키가 포함된 인증서", "데이터 압축 알고리즘"]'::jsonb, 'TLS 핸드셰이크 중 서버는 자신의 신원을 증명하고 공개 키를 전달하기 위해 서버 인증서를 클라이언트에게 보냅니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'UDP 기반의 프로토콜이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'HTTP', '["DNS", "DHCP", "HTTP", "RTP (Real-time Transport Protocol)"]'::jsonb, 'HTTP는 주로 TCP 기반 위에서 동작하여 신뢰성 있는 데이터 전송을 보장합니다. DNS, DHCP, RTP 등은 속도가 중요한 경우 UDP를 사용합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '로드 밸런싱 알고리즘 중 클라이언트의 IP 주소를 해싱하여 특정 서버로 요청을 보내는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'IP Hash', '["Round Robin", "Least Connections", "IP Hash", "Weighted Round Robin"]'::jsonb, 'IP Hash 방식은 클라이언트의 IP 주소를 해싱한 값에 따라 요청을 처리할 서버를 고정적으로 할당하여, 특정 클라이언트의 요청이 항상 같은 서버로 가도록 보장합니다 (세션 유지에 유리).', 10, 45, (SELECT id FROM quiz_info));