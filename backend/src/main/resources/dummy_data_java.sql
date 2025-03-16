

-- 1. 자바 관련 태그 생성 (존재하지 않는 경우에만)
INSERT INTO public.tags (created_at, name, description)
SELECT NOW(), 'Java', 'Java 프로그래밍 언어, 프레임워크, 개념 및 모범 사례'
WHERE NOT EXISTS (
    SELECT 1 FROM public.tags WHERE name = 'Java'
);

-- 2. 자바 하위 태그 생성
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT 
    NOW(), 
    subtag.name, 
    subtag.description, 
    (SELECT id FROM public.tags WHERE name = 'Java')
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
WHERE EXISTS (SELECT 1 FROM public.tags WHERE name = 'Java');

-- 3. 스프링 관련 하위 태그 생성
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT 
    NOW(), 
    subtag.name, 
    subtag.description, 
    (SELECT id FROM public.tags WHERE name = 'Java 스프링')
FROM (
    VALUES
        ('Spring MVC', 'Spring MVC 웹 애플리케이션 프레임워크'),
        ('Spring Boot', 'Spring Boot 자동 구성 및 빠른 개발'),
        ('Spring Data', 'Spring Data JPA 및 데이터 접근 기술'),
        ('Spring Security', 'Spring Security 인증 및 권한 부여'),
        ('Spring Cloud', 'Spring Cloud 마이크로서비스 아키텍처'),
        ('Spring AOP', 'Spring AOP 관점 지향 프로그래밍')
) as subtag(name, description)
WHERE EXISTS (SELECT 1 FROM public.tags WHERE name = 'Java 스프링');

-- 4. 자바 태그 동의어 추가
INSERT INTO public.tag_synonyms (tag_id, synonym)
SELECT (SELECT id FROM public.tags WHERE name = 'Java'), synonym
FROM (
    VALUES ('자바'), ('JAVA'), ('java')
) as s(synonym)
WHERE EXISTS (SELECT 1 FROM public.tags WHERE name = 'Java');

-- 5. 자바 관련 퀴즈 생성 (기초)
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (INTERVAL '30 days' * random()),
    NOW() - (INTERVAL '15 days' * random()),
    '자바 기초 마스터하기 #' || seq,
    '자바 언어의 기본 문법, 객체지향 개념, 예외 처리 등을 다루는 종합 퀴즈입니다.',
    'BEGINNER',
    true,
    10, -- 각 퀴즈당 10개 문제
    'TOPIC_BASED',
    30, -- 30분 제한시간
    (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
    floor(random() * 70 + 30), -- 30-100 시도 횟수
    random() * 25 + 70, -- 70-95 평균 점수
    floor(random() * 300 + 100), -- 100-400 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 2) AS seq;

-- 6. 자바 관련 퀴즈 생성 (중급)
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (INTERVAL '40 days' * random()),
    NOW() - (INTERVAL '20 days' * random()),
    '자바 중급 개발자를 위한 도전 #' || seq,
    '자바 컬렉션, 제네릭, 스트림 API, 람다 표현식 등 중급 자바 개념에 대한 심층 퀴즈입니다.',
    'INTERMEDIATE',
    true,
    8, -- 각 퀴즈당 8개 문제
    'TOPIC_BASED',
    25, -- 25분 제한시간
    (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
    floor(random() * 60 + 20), -- 20-80 시도 횟수
    random() * 20 + 65, -- 65-85 평균 점수
    floor(random() * 250 + 100), -- 100-350 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 2) AS seq;

-- 7. 자바 관련 퀴즈 생성 (고급)
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
SELECT
    NOW() - (INTERVAL '50 days' * random()),
    NOW() - (INTERVAL '25 days' * random()),
    '자바 고급 심화 프로그래밍 #' || seq,
    '자바 동시성, 리플렉션, 메모리 관리, JVM 최적화 등 고급 자바 개발자를 위한 심화 퀴즈입니다.',
    'ADVANCED',
    true,
    8, -- 각 퀴즈당 8개 문제
    'TOPIC_BASED',
    25, -- 25분 제한시간
    (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
    floor(random() * 40 + 10), -- 10-50 시도 횟수
    random() * 15 + 60, -- 60-75 평균 점수
    floor(random() * 200 + 50), -- 50-250 조회수
    NULL -- 만료일 없음
FROM generate_series(1, 2) AS seq;

-- 8. 스프링 관련 퀴즈 생성 (기초, 중급, 고급 각 1개)
INSERT INTO public.quizzes (
    created_at, updated_at, title, description, difficulty_level,
    is_public, question_count, quiz_type, time_limit,
    creator_id, attempt_count, avg_score, view_count, valid_until
)
VALUES
    (
        NOW() - (INTERVAL '60 days' * random()),
        NOW() - (INTERVAL '30 days' * random()),
        '스프링 프레임워크 기초',
        '스프링 IoC, DI, Bean 생명주기, AOP 개념 등 스프링 프레임워크의 기본을 다루는 퀴즈입니다.',
        'BEGINNER',
        true,
        10,
        'TOPIC_BASED',
        30,
        (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
        floor(random() * 70 + 30),
        random() * 25 + 70,
        floor(random() * 300 + 100),
        NULL
    ),
    (
        NOW() - (INTERVAL '50 days' * random()),
        NOW() - (INTERVAL '25 days' * random()),
        '스프링 부트와 MVC 중급',
        '스프링 부트, 스프링 MVC, RESTful API 설계, 예외 처리 등 중급 스프링 개발 내용을 다루는 퀴즈입니다.',
        'INTERMEDIATE',
        true,
        8,
        'TOPIC_BASED',
        25,
        (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
        floor(random() * 60 + 20),
        random() * 20 + 65,
        floor(random() * 250 + 100),
        NULL
    ),
    (
        NOW() - (INTERVAL '40 days' * random()),
        NOW() - (INTERVAL '20 days' * random()),
        '스프링 고급 개발자 테스트',
        '스프링 데이터 JPA, 스프링 시큐리티, 마이크로서비스, 테스트 전략 등 고급 스프링 개발 기술에 관한 퀴즈입니다.',
        'ADVANCED',
        true,
        8,
        'TOPIC_BASED',
        25,
        (SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1),
        floor(random() * 40 + 10),
        random() * 15 + 60,
        floor(random() * 200 + 50),
        NULL
    );

-- 9. 자바 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, t.id
FROM public.quizzes q
JOIN public.tags t ON t.name = 'Java'
WHERE q.title LIKE '%자바%' OR q.title LIKE '%Java%';

-- 10. 자바 하위 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, t.id
FROM public.quizzes q
JOIN public.tags t ON t.name = (
    CASE
        WHEN q.title LIKE '%기초%' AND (q.title LIKE '%자바%' OR q.title LIKE '%Java%') THEN 'Java 기초'
        WHEN q.title LIKE '%중급%' AND (q.title LIKE '%자바%' OR q.title LIKE '%Java%') THEN 'Java 객체지향'
        WHEN q.title LIKE '%고급%' AND (q.title LIKE '%자바%' OR q.title LIKE '%Java%') THEN 'Java 스레드'
    END
)
WHERE (q.title LIKE '%자바%' OR q.title LIKE '%Java%')
AND t.name IN ('Java 기초', 'Java 객체지향', 'Java 스레드');

-- 11. 스프링 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, t.id
FROM public.quizzes q
JOIN public.tags t ON t.name = 'Java 스프링'
WHERE q.title LIKE '%스프링%' OR q.title LIKE '%Spring%';

-- 12. 스프링 하위 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT q.id, t.id
FROM public.quizzes q
JOIN public.tags t ON 
    CASE
        WHEN q.title LIKE '%MVC%' OR q.title LIKE '%기초%' THEN t.name = 'Spring MVC'
        WHEN q.title LIKE '%부트%' OR q.title LIKE '%Boot%' THEN t.name = 'Spring Boot'
        WHEN q.title LIKE '%데이터%' OR q.title LIKE '%JPA%' THEN t.name = 'Spring Data'
        WHEN q.title LIKE '%보안%' OR q.title LIKE '%Security%' THEN t.name = 'Spring Security'
    END
WHERE (q.title LIKE '%스프링%' OR q.title LIKE '%Spring%')
AND t.name IN ('Spring MVC', 'Spring Boot', 'Spring Data', 'Spring Security');

-- 13. 자바 기초 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '60 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 15)
        WHEN 0 THEN 'JVM이란 무엇이며 어떤 역할을 하는가?'
        WHEN 1 THEN '자바의 기본 데이터 타입(Primitive Type)이 아닌 것은?'
        WHEN 2 THEN '다음 중 자바의 객체지향 특성이 아닌 것은?'
        WHEN 3 THEN '자바에서 main 메소드의 올바른 시그니처는?'
        WHEN 4 THEN '다음 중 자바의 접근 제한자(Access Modifier)를 가장 제한적인 것부터 나열한 것은?'
        WHEN 5 THEN '자바에서 "=="와 "equals()" 메소드의 차이점은?'
        WHEN 6 THEN '다음 중 자바의 예외 처리 구문으로 올바른 것은?'
        WHEN 7 THEN '다음 코드의 출력 결과는?'
        WHEN 8 THEN '자바에서 String 클래스가 불변(immutable)인 이유는?'
        WHEN 9 THEN 'Java SE와 Java EE의 차이점은?'
        WHEN 10 THEN '자바에서 가비지 컬렉션(Garbage Collection)이란?'
        WHEN 11 THEN '자바에서 final 키워드의 용도는?'
        WHEN 12 THEN '자바에서 인터페이스(Interface)와 추상 클래스(Abstract Class)의 주요 차이점은?'
        WHEN 13 THEN '자바에서 오버로딩(Overloading)과 오버라이딩(Overriding)의 차이점은?'
        WHEN 14 THEN '다음 코드의 출력 결과는?'
    END,
    'MULTIPLE_CHOICE',
    'BEGINNER',
    CASE mod(seq, 15)
        WHEN 0 THEN 'Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다'
        WHEN 1 THEN 'String'
        WHEN 2 THEN '포인터 조작'
        WHEN 3 THEN 'public static void main(String[] args)'
        WHEN 4 THEN 'private → default → protected → public'
        WHEN 5 THEN '== 연산자는 참조 값을 비교하고, equals()는 객체의 내용을 비교한다'
        WHEN 6 THEN 'try { ... } catch (Exception e) { ... } finally { ... }'
        WHEN 7 THEN '15'
        WHEN 8 THEN '보안, 스레드 안전성, 해시코드 캐싱 등의 이점 때문'
        WHEN 9 THEN 'Java SE는 표준 에디션으로 기본 기능을 제공하고, Java EE는 기업 환경을 위한 확장 기능을 제공한다'
        WHEN 10 THEN '더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제하는 기능'
        WHEN 11 THEN '클래스, 메소드, 변수가 더 이상 변경될 수 없음을 나타냄'
        WHEN 12 THEN '인터페이스는 다중 상속이 가능하지만, 추상 클래스는 단일 상속만 가능하다'
        WHEN 13 THEN '오버로딩은 같은 이름의 메소드를 다른 매개변수로 정의하는 것이고, 오버라이딩은 상속받은 메소드를 재정의하는 것이다'
        WHEN 14 THEN 'Child Method'
    END,
    CASE mod(seq, 15)
        WHEN 0 THEN '["Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다", "Java Variable Method로, 변수와 메소드를 관리하는 시스템이다", "Java Visual Monitor로, 화면 출력을 담당하는 시스템이다", "Java Version Manager로, 자바 버전을 관리하는 도구이다"]'::jsonb
        WHEN 1 THEN '["int", "char", "boolean", "String"]'::jsonb
        WHEN 2 THEN '["상속", "캡슐화", "다형성", "포인터 조작"]'::jsonb
        WHEN 3 THEN '["public void main(String[] args)", "public static void main()", "static void main(String[] args)", "public static void main(String[] args)"]'::jsonb
        WHEN 4 THEN '["private → default → protected → public", "public → protected → default → private", "private → protected → default → public", "default → private → protected → public"]'::jsonb
        WHEN 5 THEN '["== 연산자는 참조 값을 비교하고, equals()는 객체의 내용을 비교한다", "== 연산자는 객체의 내용을 비교하고, equals()는 참조 값을 비교한다", "== 연산자와 equals() 메소드는 모두 객체의 내용을 비교한다", "== 연산자와 equals() 메소드는 모두 참조 값을 비교한다"]'::jsonb
        WHEN 6 THEN '["try { ... } catch { ... } finally { ... }", "try { ... } exception (Exception e) { ... }", "try { ... } catch (Exception e) { ... } finally { ... }", "try { ... } except (Exception e) { ... } finally { ... }"]'::jsonb
        WHEN 7 THEN '["10", "15", "20", "컴파일 에러"]'::jsonb
        WHEN 8 THEN '["메모리를 절약하기 위해", "보안, 스레드 안전성, 해시코드 캐싱 등의 이점 때문", "가비지 컬렉션을 효율적으로 하기 위해", "문자열 연산을 빠르게 하기 위해"]'::jsonb
        WHEN 9 THEN '["Java SE는 보안 에디션, Java EE는 확장 에디션이다", "Java SE는 표준 에디션으로 기본 기능을 제공하고, Java EE는 기업 환경을 위한 확장 기능을 제공한다", "Java SE는 서버 에디션, Java EE는 임베디드 에디션이다", "Java SE는 단일 스레드만 지원하고, Java EE는 멀티 스레딩을 지원한다"]'::jsonb
        WHEN 10 THEN '["프로그램이 종료될 때 모든 객체를 삭제하는 기능", "더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제하는 기능", "개발자가 명시적으로 호출하여 메모리를 정리하는 기능", "사용하지 않는 변수를 제거하는 컴파일러 최적화 기술"]'::jsonb
        WHEN 11 THEN '["예외 처리를 위한 키워드", "클래스, 메소드, 변수가 더 이상 변경될 수 없음을 나타냄", "객체 생성을 위한 키워드", "스레드 동기화를 위한 키워드"]'::jsonb
        WHEN 12 THEN '["인터페이스는 메소드 구현이 불가능하지만, 추상 클래스는 일부 메소드 구현이 가능하다", "인터페이스는 다중 상속이 가능하지만, 추상 클래스는 단일 상속만 가능하다", "인터페이스는 필드를 가질 수 없지만, 추상 클래스는 필드를 가질 수 있다", "모든 위의 설명이 맞다"]'::jsonb
        WHEN 13 THEN '["오버로딩은 상속 관계에서만 가능하고, 오버라이딩은 같은 클래스 내에서만 가능하다", "오버로딩은 같은 이름의 메소드를 다른 매개변수로 정의하는 것이고, 오버라이딩은 상속받은 메소드를 재정의하는 것이다", "오버로딩은 컴파일 타임에 결정되고, 오버라이딩은 프로그램 실행 중에 결정된다", "오버로딩은 성능 최적화를 위한 것이고, 오버라이딩은 보안을 위한 것이다"]'::jsonb
        WHEN 14 THEN '["Parent Method", "Child Method", "컴파일 에러", "런타임 에러"]'::jsonb
    END,
    CASE mod(seq, 15)
        WHEN 0 THEN 'JVM(Java Virtual Machine)은 자바 바이트코드(.class 파일)를 각 운영체제에 맞게 해석하고 실행하는 가상 머신입니다. 덕분에 Java는 "Write Once, Run Anywhere"라는 특징을 갖습니다.'
        WHEN 1 THEN 'String은 기본 데이터 타입이 아닌 참조 타입(Reference Type)입니다. 자바의 기본 데이터 타입은 byte, short, int, long, float, double, char, boolean의 8가지입니다.'
        WHEN 2 THEN '자바는 포인터를 직접 조작할 수 없으며, 메모리 관리를 자동으로 처리합니다. 자바의 주요 객체지향 특성은 상속, 캡슐화, 다형성, 추상화입니다.'
        WHEN 3 THEN '자바 애플리케이션의 시작점인 main 메소드는 반드시 public static void main(String[] args) 형태로 선언해야 합니다. JVM이 이 시그니처를 찾아 프로그램을 실행합니다.'
        WHEN 4 THEN '자바의 접근 제한자는 private(해당 클래스 내에서만 접근 가능), default(같은 패키지 내에서만 접근 가능), protected(같은 패키지 및 상속받은 클래스에서 접근 가능), public(어디서든 접근 가능) 순으로 제한 범위가 넓어집니다.'
        WHEN 5 THEN '== 연산자는 기본 타입일 경우 값을 비교하고, 참조 타입일 경우 객체의 주소를 비교합니다. equals() 메소드는 Object 클래스에서 상속받은 메소드로, 재정의하지 않으면 == 연산자와 같이 동작하지만, String과 같은 클래스에서는 내용 비교를 위해 오버라이딩되어 있습니다.'
        WHEN 6 THEN '자바에서 예외 처리는 try-catch-finally 블록을 사용합니다. try 블록에서 예외가 발생할 수 있는 코드를 작성하고, catch 블록에서 예외를 처리하며, finally 블록은 예외 발생 여부와 상관없이 항상 실행됩니다.'
        WHEN 7 THEN '삼항 연산자 조건 ? 참일 때 값 : 거짓일 때 값 형태로 사용합니다. 5 > 2는 참이므로 5 + 10인 15가 출력됩니다.'
        WHEN 8 THEN 'String이 불변인 이유는 여러 가지가 있습니다. 보안 측면에서 중요한 데이터(비밀번호 등)가 변경되지 않도록 보장하고, 여러 스레드에서 동시에 접근해도 안전하며, String이 HashMap이나 HashSet의 키로 자주 사용되기 때문에 해시코드를 캐싱할 수 있어 성능상 이점이 있습니다.'
        WHEN 9 THEN 'Java SE(Standard Edition)는 자바 언어의 핵심 기능을 포함하는 기본 플랫폼입니다. Java EE(Enterprise Edition)는 SE를 기반으로 대규모 기업 환경에서 필요한 웹 서비스, 분산 컴퓨팅, 트랜잭션 관리 등의 기능을 추가로 제공합니다.'
        WHEN 10 THEN '가비지 컬렉션은 JVM의 중요한 기능으로, 프로그래머가 명시적으로 메모리를 해제하지 않아도 더 이상 사용되지 않는(참조되지 않는) 객체를 탐지하고 자동으로 메모리에서 제거합니다. 이를 통해 메모리 누수를 방지하고 개발자의 부담을 줄입니다.'
        WHEN 11 THEN 'final 키워드는 다양한 상황에서 사용됩니다. 변수에 사용하면 상수가 되어 값을 변경할 수 없고, 메소드에 사용하면 오버라이딩할 수 없으며, 클래스에 사용하면 상속할 수 없게 됩니다.'
        WHEN 12 THEN '인터페이스와 추상 클래스의 차이점은 여러 가지가 있습니다. 인터페이스는 다중 구현이 가능하지만 추상 클래스는 단일 상속만 가능합니다. 인터페이스는 Java 8 이전에는 추상 메소드만 가질 수 있었으나(Java 8부터는 default, static 메소드 가능), 추상 클래스는 추상 메소드와 일반 메소드를 모두 가질 수 있습니다. 인터페이스는 상수만 가질 수 있지만, 추상 클래스는 인스턴스 변수를 가질 수 있습니다. 사실 정답은 "모든 위의 설명이 맞다"이지만, 가장 핵심적인 차이점은 다중 상속/구현의 가능 여부입니다.'
        WHEN 13 THEN '오버로딩은 한 클래스 내에서 같은 이름의 메소드를 매개변수의 개수나 타입을 다르게 하여 여러 개 정의하는 것입니다. 오버라이딩은 상속 관계에서 자식 클래스가 부모 클래스의 메소드를 같은 시그니처로 재정의하는 것입니다. 오버로딩은 컴파일 시점에 결정되는 정적 바인딩이고, 오버라이딩은 런타임에 결정되는 동적 바인딩입니다.'
        WHEN 14 THEN '이 코드는 메소드 오버라이딩의 예입니다. Parent 타입 변수로 Child 객체를 참조하고 있지만, 호출되는 메소드는 실제 객체의 타입인 Child 클래스의 메소드입니다. 이것은 자바의 동적 바인딩(Dynamic Binding) 특성 때문입니다.'
END,
    5, -- 기초 문제 5점
    30, -- 30초 제한시간
    (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%자바 기초%' ORDER BY random() LIMIT 1),
    CASE mod(seq, 15)
        WHEN 7 THEN 'int result = 5 > 2 ? 5 + 10 : 20;\nSystem.out.println(result);'
        WHEN 14 THEN 'class Parent {\n    void method() {\n        System.out.println("Parent Method");\n    }\n}\n\nclass Child extends Parent {\n    @Override\n    void method() {\n        System.out.println("Child Method");\n    }\n}\n\npublic class Test {\n    public static void main(String[] args) {\n        Parent p = new Child();\n        p.method();\n    }\n}'
        ELSE NULL
END
    FROM generate_series(1, 20) AS seq
WHERE seq <= 15;


-- 14. 자바 중급 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '60 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 15)
        WHEN 0 THEN '자바의 실행 과정을 올바르게 설명한 것은?'
        WHEN 1 THEN '다음 중 Thread-safe하지 않은 컬렉션은?'
        WHEN 2 THEN '자바에서 Checked Exception과 Unchecked Exception의 차이점은?'
        WHEN 3 THEN '다음 코드의 실행 결과는?'
        WHEN 4 THEN '자바에서 synchronized 키워드의 역할은?'
        WHEN 5 THEN '다음 중 불변 객체(Immutable Object)의 특징이 아닌 것은?'
        WHEN 6 THEN '자바에서 equals()와 hashCode() 메소드를 함께 오버라이딩해야 하는 이유는?'
        WHEN 7 THEN '자바에서 static 키워드의 특징으로 올바른 것은?'
        WHEN 8 THEN '다음 코드의 출력 결과는?'
        WHEN 9 THEN '자바에서 String, StringBuilder, StringBuffer의 주요 차이점은?'
        WHEN 10 THEN 'try-with-resources 구문의 목적은?'
        WHEN 11 THEN '자바의 직렬화(Serialization)와 역직렬화(Deserialization)란?'
        WHEN 12 THEN '자바의 리플렉션(Reflection) API의 주요 용도는?'
        WHEN 13 THEN '자바에서 volatile 키워드의 역할은?'
        WHEN 14 THEN '자바 메모리 관리에서 "세대별 가비지 컬렉션(Generational Garbage Collection)"이란?'
        END,
    'MULTIPLE_CHOICE',
    'INTERMEDIATE',
    CASE mod(seq, 15)
        WHEN 0 THEN '소스 코드(.java) → 바이트 코드(.class) → JVM에서 실행'
        WHEN 1 THEN 'ArrayList'
        WHEN 2 THEN 'Checked Exception은 컴파일 시점에 확인되고 명시적인 처리가 필요하지만, Unchecked Exception은 런타임에 발생하고 명시적인 처리가 강제되지 않는다'
        WHEN 3 THEN '종료 블록\n예외 발생: java.lang.ArithmeticException: / by zero'
        WHEN 4 THEN '여러 스레드가 동시에 접근하는 것을 방지하여 스레드 안전성을 보장한다'
        WHEN 5 THEN '객체 생성 후 상태를 변경할 수 있다'
        WHEN 6 THEN 'equals()가 true를 반환하는 두 객체는 반드시 같은 hashCode를 반환해야 하기 때문이다'
        WHEN 7 THEN 'static 멤버는 클래스가 로드될 때 메모리에 할당되며, 모든 인스턴스가 공유한다'
        WHEN 8 THEN 'Value1: 10\nValue2: 20'
        WHEN 9 THEN 'String은 불변, StringBuffer는 Thread-safe하고 가변적, StringBuilder는 Thread-safe하지 않고 가변적이다'
        WHEN 10 THEN '자동으로 리소스를 닫아주어 리소스 누수를 방지한다'
        WHEN 11 THEN '직렬화는 객체를 바이트 스트림으로 변환하는 과정이고, 역직렬화는 바이트 스트림을 다시 객체로 변환하는 과정이다'
        WHEN 12 THEN '런타임에 클래스의 정보를 검사하고 조작하는 것'
        WHEN 13 THEN '변수의 값이 스레드의 로컬 캐시가 아닌 항상 메인 메모리에서 읽고 쓰도록 보장한다'
        WHEN 14 THEN '객체를 Young 영역과 Old 영역으로 나누어 관리하는 방식'
        END,
    CASE mod(seq, 15)
        WHEN 0 THEN '["소스 코드(.java) → 네이티브 코드(.exe) → 운영체제에서 실행", "소스 코드(.java) → 바이트 코드(.class) → JVM에서 실행", "바이트 코드(.class) → 소스 코드(.java) → JVM에서 실행", "소스 코드(.java) → 직접 OS에서 실행"]'::jsonb
        WHEN 1 THEN '["ArrayList", "Vector", "ConcurrentHashMap", "CopyOnWriteArrayList"]'::jsonb
        WHEN 2 THEN '["Checked Exception은 심각한 오류를 나타내고, Unchecked Exception은 경미한 오류를 나타낸다", "Checked Exception은 복구 가능한 오류이고, Unchecked Exception은 복구 불가능한 오류이다", "Checked Exception은 컴파일 시점에 확인되고 명시적인 처리가 필요하지만, Unchecked Exception은 런타임에 발생하고 명시적인 처리가 강제되지 않는다", "Checked Exception은 JVM에 의해 발생하고, Unchecked Exception은 프로그래머에 의해 발생한다"]'::jsonb
        WHEN 3 THEN '["예외 발생: java.lang.ArithmeticException: / by zero", "결과: 0\n종료 블록", "종료 블록\n예외 발생: java.lang.ArithmeticException: / by zero", "0"]'::jsonb
        WHEN 4 THEN '["메소드의 실행 속도를 높인다", "여러 스레드가 동시에 접근하는 것을 방지하여 스레드 안전성을 보장한다", "메모리 사용량을 최적화한다", "예외 처리를 자동화한다"]'::jsonb
        WHEN 5 THEN '["객체 생성 후 상태를 변경할 수 있다", "모든 필드가 final이다", "클래스가 상속되지 않도록 설계된다", "getter는 있지만 setter는 없다"]'::jsonb
        WHEN 6 THEN '["자바 문법상 두 메소드는 항상 함께 구현해야 하기 때문이다", "equals()가 true를 반환하는 두 객체는 반드시 같은 hashCode를 반환해야 하기 때문이다", "hashCode()는 객체 비교 시 항상 먼저 호출되기 때문이다", "두 메소드 모두 성능 최적화에 필수적이기 때문이다"]'::jsonb
        WHEN 7 THEN '["static 메소드 내에서 this 키워드를 사용할 수 있다", "static 블록은 객체 생성 시마다 실행된다", "static 멤버는 클래스가 로드될 때 메모리에 할당되며, 모든 인스턴스가 공유한다", "static 메소드는 오버라이딩이 가능하다"]'::jsonb
        WHEN 8 THEN '["Value1: 10\nValue2: 10", "Value1: 10\nValue2: 20", "Value1: 20\nValue2: 20", "컴파일 에러"]'::jsonb
        WHEN 9 THEN '["String, StringBuilder, StringBuffer 모두 불변(immutable)이다", "String은 불변, StringBuffer는 Thread-safe하고 가변적, StringBuilder는 Thread-safe하지 않고 가변적이다", "String은 가변적, StringBuilder와 StringBuffer는 불변이다", "String은 Thread-safe하지 않고, StringBuilder와 StringBuffer는 Thread-safe하다"]'::jsonb
        WHEN 10 THEN '["예외 발생을 방지한다", "자동으로 리소스를 닫아주어 리소스 누수를 방지한다", "코드의 가독성만 향상시킨다", "실행 속도를 개선한다"]'::jsonb
        WHEN 11 THEN '["직렬화는 클래스를 컴파일하는 과정이고, 역직렬화는 바이트코드를 실행하는 과정이다", "직렬화는 객체를 바이트 스트림으로 변환하는 과정이고, 역직렬화는 바이트 스트림을 다시 객체로 변환하는 과정이다", "직렬화는 객체의 메모리 주소를 저장하는 것이고, 역직렬화는 주소로부터 객체를 복원하는 것이다", "직렬화는 데이터베이스에 객체를 저장하는 과정이고, 역직렬화는 데이터베이스에서 객체를 가져오는 과정이다"]'::jsonb
        WHEN 12 THEN '["컴파일 시간을 단축시키는 것", "런타임에 클래스의 정보를 검사하고 조작하는 것", "메모리 사용량을 최적화하는 것", "네트워크 통신을 간소화하는 것"]'::jsonb
        WHEN 13 THEN '["변수의 값이 스레드의 로컬 캐시가 아닌 항상 메인 메모리에서 읽고 쓰도록 보장한다", "변수의 값이 변경되지 않도록 상수로 만든다", "메소드가 오버라이딩되지 않도록 한다", "객체의 직렬화를 가능하게 한다"]'::jsonb
        WHEN 14 THEN '["모든 객체를 동일한 우선순위로 처리하는 방식", "객체를 Young 영역과 Old 영역으로 나누어 관리하는 방식", "사용자가 직접 가비지 컬렉션을 호출하는 방식", "가비지 컬렉션을 여러 스레드로 병렬 처리하는 방식"]'::jsonb
        END,
    CASE mod(seq, 15)
        WHEN 0 THEN '자바 프로그램의 실행 과정은 다음과 같습니다: 개발자가 .java 파일을 작성 → 자바 컴파일러(javac)가 이를 컴파일하여 바이트 코드(.class 파일)로 변환 → JVM이 바이트 코드를 로드하고 실행합니다. 이 과정 덕분에 자바는 "한 번 작성하면 어디서나 실행"(WORA) 특성을 갖게 됩니다.'
        WHEN 1 THEN 'ArrayList는 Thread-safe하지 않습니다. 다중 스레드 환경에서 안전하게 사용하려면 Collections.synchronizedList()로 래핑하거나, Vector, CopyOnWriteArrayList 같은 Thread-safe한 컬렉션을 사용해야 합니다. Vector는 메소드에 synchronized 키워드가 붙어있어 Thread-safe하며, ConcurrentHashMap과 CopyOnWriteArrayList는 Java 5부터 추가된 동시성 컬렉션입니다.'
        WHEN 2 THEN 'Checked Exception은 Exception 클래스를 상속하며, 컴파일 시점에 확인됩니다. 메소드에서 throws 절로 선언하거나 try-catch로 처리해야 합니다(예: IOException, SQLException). Unchecked Exception은 RuntimeException을 상속하며, 컴파일러가 예외 처리를 강제하지 않습니다(예: NullPointerException, ArrayIndexOutOfBoundsException). 일반적으로 프로그램 오류를 나타내며, 미리 방지하는 것이 좋습니다.'
        WHEN 3 THEN '이 코드에서는 try 블록에서 10/0으로 ArithmeticException이 발생합니다. 발생한 예외는 catch 블록에서 잡히고 메시지가 출력됩니다. finally 블록은 예외 발생 여부와 상관없이 항상 실행되므로 "종료 블록"이 먼저 출력되고, 그 다음 catch 블록의 내용이 출력됩니다.'
        WHEN 4 THEN 'synchronized 키워드는 멀티스레드 환경에서 여러 스레드가 공유 자원에 동시에 접근하는 것을 방지하는 동기화 메커니즘입니다. 메소드나 블록에 synchronized를 사용하면 한 시점에 하나의 스레드만 해당 코드를 실행할 수 있어 데이터 일관성과 스레드 안전성을 보장합니다. 다만, 과도한 사용은 성능 저하를 가져올 수 있습니다.'
        WHEN 5 THEN '불변 객체는 생성 후에 그 상태가 변경되지 않는 객체입니다. 일반적인 특징으로는 1) 모든 필드가 final로 선언됨 2) 클래스가 final로 선언되거나 다른 방법으로 상속 방지 3) 상태 변경 메소드(setter 등)가 없음 4) 가변 객체를 참조하는 필드가 있다면, 그것이 외부로 노출되지 않도록 방어적 복사를 사용함 등이 있습니다. String, Integer 같은 래퍼 클래스가 대표적인 불변 객체입니다.'
        WHEN 6 THEN 'equals()와 hashCode()는 함께 오버라이딩하는 것이 중요한 이유는 자바의 일반 규약 때문입니다. 이 규약에 따르면, equals() 메소드로 비교했을 때 동등한 두 객체는 반드시 같은 hashCode 값을 반환해야 합니다. 만약 이 규약을 지키지 않으면 HashMap, HashSet과 같은 해시 기반 컬렉션에서 객체가 예상대로 동작하지 않게 됩니다. 예를 들어, equals()로는 같다고 판단되는 객체가 서로 다른 hashCode를 반환하면, HashMap에서 검색 시 원하는 객체를 찾지 못할 수 있습니다.'
        WHEN 7 THEN 'static 키워드는 클래스 수준의 멤버를 정의할 때 사용합니다. static 멤버(변수나 메소드)는 클래스가 메모리에 로드될 때 생성되어 프로그램이 종료될 때까지 유지되며, 모든 인스턴스가 이를 공유합니다. static 메소드 내에서는 this를 사용할 수 없고(인스턴스가 없을 수 있으므로), static 메소드는 오버라이딩되지 않습니다(클래스에 바인딩되므로). static 블록은 클래스가 로드될 때 한 번만 실행됩니다.'
        WHEN 8 THEN '이 코드에서 changeValue 메소드는 기본 타입(int)을 매개변수로 받습니다. 자바에서 기본 타입은 값이 복사되어 전달되므로(pass by value), 메소드 내에서 값을 변경해도 원본에는 영향을 주지 않습니다. 따라서 value1은 변경되지 않고 10을 유지합니다. 반면, changeReferenceValue 메소드는 참조 타입(StringBuilder)을 매개변수로 받습니다. 참조 타입도 값으로 전달되지만, 그 값이 객체의 참조이므로 메소드 내에서 같은 객체를 참조하고 그 객체의 상태를 변경할 수 있습니다. 따라서 sb 객체의 내용이 "10"에서 "20"으로 변경됩니다.'
        WHEN 9 THEN 'String은 불변 클래스로, 한 번 생성된 문자열은 변경할 수 없습니다. 따라서 문자열 연산이 많은 경우 성능 저하가 발생할 수 있습니다. StringBuffer와 StringBuilder는 모두 가변적이며 내부 버퍼를 사용하여 문자열 조작이 효율적입니다. 주요 차이점은 StringBuffer는 Thread-safe하지만(synchronized 메소드 사용), StringBuilder는 Thread-safe하지 않아 단일 스레드 환경에서 더 빠릅니다. JDK 1.5부터 추가된 StringBuilder가 성능 면에서 유리해 단일 스레드 환경에서는 주로 StringBuilder를 사용합니다.'
        WHEN 10 THEN 'try-with-resources는 Java 7에서 도입된 구문으로, AutoCloseable 인터페이스를 구현한 자원(파일, 데이터베이스 연결, 네트워크 연결 등)을 사용한 후 자동으로 close() 메소드를 호출하여 닫아줍니다. 이를 통해 개발자가 명시적으로 finally 블록에서 리소스를 닫지 않아도 되므로 코드가 간결해지고, 예외가 발생하더라도 리소스 누수를 방지할 수 있습니다.'
        WHEN 11 THEN '직렬화(Serialization)는 객체의 상태를 바이트 스트림으로 변환하는 과정으로, 객체를 파일로 저장하거나 네트워크를 통해 전송할 때 사용됩니다. 역직렬화(Deserialization)는 바이트 스트림을 다시 객체로 복원하는 과정입니다. 자바에서 직렬화를 지원하려면 클래스가 Serializable 인터페이스를 구현해야 합니다. 직렬화는 객체의 완전한 복제본을 만들거나 객체의 상태를 영속화할 때 유용하지만, 보안 및 버전 관리 문제에 주의해야 합니다.'
        WHEN 12 THEN '리플렉션(Reflection)은 실행 중인 자바 프로그램이 자체적으로 검사하거나 내부 속성을 조작할 수 있게 하는 API입니다. 이를 통해 런타임에 클래스, 인터페이스, 필드, 메소드 등에 접근하여 정보를 가져오거나, 메소드를 호출하거나, 객체를 생성할 수 있습니다. 주로 프레임워크나 라이브러리에서 사용되며, 스프링의 의존성 주입, ORM의 객체-테이블 매핑, 직렬화 등에 활용됩니다. 그러나 타입 안전성이 손상되고, 성능 저하가 있을 수 있으며, 접근 제한을 우회할 수 있어 주의해서 사용해야 합니다.'
        WHEN 13 THEN 'volatile 키워드는 멀티스레드 환경에서 변수의 가시성(visibility) 문제를 해결하기 위해 사용됩니다. 멀티 코어 시스템에서 각 스레드는 CPU 캐시에 변수의 복사본을 유지할 수 있어, 한 스레드가 변수를 변경해도 다른 스레드는 이를 인식하지 못할 수 있습니다. volatile로 선언된 변수는 항상 메인 메모리에서 읽고 쓰도록 보장되어, 모든 스레드가 최신 값을 볼 수 있습니다. 그러나 volatile은 원자성(atomicity)을 보장하지 않으므로, 복합 연산(예: i++)에는 synchronized나 AtomicInteger 같은 다른 동기화 메커니즘이 필요합니다.'
        WHEN 14 THEN '세대별 가비지 컬렉션은 객체의 수명에 따라 힙 메모리를 여러 영역으로 나누어 관리하는 방식입니다. 이는 "대부분의 객체는 생성 후 짧은 시간 내에 사용되지 않게 된다"는 약한 세대 가설(Weak Generational Hypothesis)에 기반합니다. 일반적으로 Young 영역(Eden, Survivor 0, Survivor 1)과 Old 영역으로 나뉘며, 객체는 Young 영역에 생성된 후 일정 시간 살아남으면 Old 영역으로 이동합니다. Young 영역에서의 가비지 컬렉션(Minor GC)은 빠르게 자주 일어나고, Old 영역에서의 가비지 컬렉션(Major GC 또는 Full GC)은 덜 자주 발생하도록 설계되어 전체적인 성능을 개선합니다.'
        END,
    10, -- 중급 문제 10점
    45, -- 45초 제한시간
    (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%자바 중급%' ORDER BY random() LIMIT 1),
    CASE mod(seq, 15)
        WHEN 3 THEN 'public class ExceptionTest {\n    public static void main(String[] args) {\n        try {\n            int result = 10 / 0; // ArithmeticException 발생\n            System.out.println("결과: " + result);\n        } catch (ArithmeticException e) {\n            System.out.println("예외 발생: " + e);\n        } finally {\n            System.out.println("종료 블록");\n        }\n    }\n}'
        WHEN 8 THEN 'public class PassByValueTest {\n    public static void main(String[] args) {\n        int value1 = 10;\n        StringBuilder sb = new StringBuilder("10");\n        \n        changeValue(value1);\n        changeReferenceValue(sb);\n        \n        System.out.println("Value1: " + value1);\n        System.out.println("Value2: " + sb);\n    }\n    \n    public static void changeValue(int value) {\n        value = 20;\n    }\n    \n    public static void changeReferenceValue(StringBuilder value) {\n        value.delete(0, value.length());\n        value.append("20");\n    }\n}'
        ELSE NULL
        END
FROM generate_series(1, 20) AS seq
WHERE seq <= 15;

-- 15. 자바 고급 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '60 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 15)
        WHEN 0 THEN '자바에서 G1 가비지 컬렉터(G1 Garbage Collector)의 주요 특징은?'
        WHEN 1 THEN '자바 NIO(New Input/Output)의 주요 특징과 기존 IO와의 차이점은?'
        WHEN 2 THEN '자바에서 메모리 누수(Memory Leak)가 발생할 수 있는 상황은?'
        WHEN 3 THEN '다음 중 Java의 ClassLoader 시스템에 대한 설명으로 올바른 것은?'
        WHEN 4 THEN '다음 코드에서 발생할 수 있는 문제점은?'
        WHEN 5 THEN 'Java의 CompletableFuture와 기존 Future의 주요 차이점은?'
        WHEN 6 THEN '자바에서 메소드 참조(Method Reference)를 사용하는 올바른 예는?'
        WHEN 7 THEN '자바의 Virtual Thread(가상 스레드)의 주요 특징은?'
        WHEN 8 THEN 'Java 모듈 시스템(JPMS, Java Platform Module System)의 주요 목적은?'
        WHEN 9 THEN '자바에서 패턴 매칭(Pattern Matching)을 사용한 올바른 예는?'
        WHEN 10 THEN 'Java에서 레코드(Record)의 주요 특징이 아닌 것은?'
        WHEN 11 THEN '자바에서 Heap과 Stack 메모리의 차이점으로 올바른 것은?'
        WHEN 12 THEN '다음 코드의 출력 결과는?'
        WHEN 13 THEN '자바의 Record, Sealed Classes, Pattern Matching의 공통된 목적은?'
        WHEN 14 THEN '자바에서 ThreadLocal의 주요 사용 사례는?'
        END,
    'MULTIPLE_CHOICE',
    'ADVANCED',
    CASE mod(seq, 15)
        WHEN 0 THEN '힙 메모리를 균등한 크기의 영역(Region)으로 나누어 관리하고, 사용자가 지정한 최대 중지 시간(pause time) 목표를 달성하기 위해 최적화된다'
        WHEN 1 THEN 'NIO는 버퍼 기반이고 논블로킹 IO를 지원하며, 채널 개념을 도입하여 양방향 데이터 전송이 가능하다'
        WHEN 2 THEN '정적 필드에 컬렉션 객체를 저장하고 요소를 계속 추가하기만 하는 경우'
        WHEN 3 THEN 'Bootstrap, Extension, Application ClassLoader가 계층 구조를 이루며 위임 모델(delegation model)에 따라 작동한다'
        WHEN 4 THEN '경쟁 상태(race condition)가 발생할 수 있다'
        WHEN 5 THEN 'CompletableFuture는 비동기 작업을 조합하고 콜백을 지원하는 기능을 제공한다'
        WHEN 6 THEN 'list.forEach(System.out::println);'
        WHEN 7 THEN '운영체제 스레드보다 가볍고, 블로킹 작업에서도 효율적으로 리소스를 사용한다'
        WHEN 8 THEN '강력한 캡슐화, 명시적 의존성 선언, 모듈화된 JDK를 제공하여 플랫폼 확장성과 보안을 향상시킨다'
        WHEN 9 THEN 'if (obj instanceof String s) { System.out.println(s.length()); }'
        WHEN 10 THEN '필드 값을 변경할 수 있는 setter 메소드가 자동 생성된다'
        WHEN 11 THEN 'Stack은 각 스레드마다 하나씩 할당되고 주로 메소드 호출과 지역 변수를 저장하며, Heap은 모든 스레드가 공유하고 객체를 저장한다'
        WHEN 12 THEN 'Child'
        WHEN 13 THEN '데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 것'
        WHEN 14 THEN '스레드 안전성을 유지하면서 스레드별로 독립적인 상태를 관리해야 할 때'
    END,
    CASE mod(seq, 15)
        WHEN 0 THEN '["힙 메모리를 균등한 크기의 영역(Region)으로 나누어 관리하고, 사용자가 지정한 최대 중지 시간(pause time) 목표를 달성하기 위해 최적화된다", "항상 단일 스레드로 실행되어 CPU 사용량을 최소화한다", "전체 힙 메모리를 한 번에 처리하여 파편화를 방지한다", "Young 영역과 Old 영역의 구분 없이 모든 객체를 동일하게 처리한다"]'::jsonb
        WHEN 1 THEN '["NIO는 항상 기존 IO보다 빠르게 동작한다", "NIO는 버퍼 기반이고 논블로킹 IO를 지원하며, 채널 개념을 도입하여 양방향 데이터 전송이 가능하다", "NIO는 스트림만 사용하고 블로킹 방식으로만 작동한다", "NIO는 멀티스레딩을 지원하지 않는다"]'::jsonb
        WHEN 2 THEN '["모든 지역 변수는 메모리 누수를 일으킨다", "모든 정적 변수는 메모리 누수를 일으킨다", "정적 필드에 컬렉션 객체를 저장하고 요소를 계속 추가하기만 하는 경우", "모든 익명 클래스는 메모리 누수를 일으킨다"]'::jsonb
        WHEN 3 THEN '["클래스는 항상 하나의 ClassLoader에 의해서만 로드된다", "Bootstrap, Extension, Application ClassLoader가 계층 구조를 이루며 위임 모델(delegation model)에 따라 작동한다", "ClassLoader는 Java 애플리케이션 시작 시 한 번만 사용된다", "모든 클래스는 동일한 ClassLoader에 의해 로드된다"]'::jsonb
        WHEN 4 THEN '["경쟁 상태(race condition)가 발생할 수 있다", "데드락(deadlock)이 발생할 수 있다", "메모리 누수(memory leak)가 발생할 수 있다", "스택 오버플로우(stack overflow)가 발생할 수 있다"]'::jsonb
        WHEN 5 THEN '["CompletableFuture는 항상 단일 스레드로 실행된다", "CompletableFuture는 비동기 작업을 조합하고 콜백을 지원하는 기능을 제공한다", "CompletableFuture는 동기 방식으로만 작동한다", "CompletableFuture는 결과를 반환할 수 없다"]'::jsonb
        WHEN 6 THEN '["list.forEach(System.out.println);", "list.forEach(System.out::println);", "list.forEach(::System.out.println);", "list.forEach(System::out::println);"]'::jsonb
        WHEN 7 THEN '["물리적 CPU 코어마다 하나씩만 생성될 수 있다", "운영체제 스레드보다 가볍고, 블로킹 작업에서도 효율적으로 리소스를 사용한다", "기존 스레드보다 느리지만 더 안정적이다", "Java 7부터 도입된 기능이다"]'::jsonb
        WHEN 8 THEN '["메모리 사용량을 줄이는 것", "애플리케이션 실행 속도를 높이는 것", "강력한 캡슐화, 명시적 의존성 선언, 모듈화된 JDK를 제공하여 플랫폼 확장성과 보안을 향상시킨다", "기존 패키지 시스템을 완전히 대체하는 것"]'::jsonb
        WHEN 9 THEN '["if (obj instanceof String) { System.out.println(((String)obj).length()); }", "if (obj instanceof String s) { System.out.println(s.length()); }", "if (obj matches String) { System.out.println(obj.length()); }", "if (obj instanceof String s && s.isEmpty()) { System.out.println(obj); }"]'::jsonb
        WHEN 10 THEN '["불변(immutable) 데이터 클래스를 간결하게 정의할 수 있다", "equals(), hashCode(), toString() 메소드가 자동 생성된다", "필드 값을 변경할 수 있는 setter 메소드가 자동 생성된다", "각 필드에 접근할 수 있는 getter 메소드가 자동 생성된다"]'::jsonb
        WHEN 11 THEN '["Heap은 각 스레드마다 하나씩 할당되고, Stack은 모든 스레드가 공유한다", "Stack은 각 스레드마다 하나씩 할당되고 주로 메소드 호출과 지역 변수를 저장하며, Heap은 모든 스레드가 공유하고 객체를 저장한다", "Heap은 항상 Stack보다 큰 메모리 공간을 갖는다", "Stack은 자동으로 관리되지만, Heap은 명시적으로 메모리를 해제해야 한다"]'::jsonb
        WHEN 12 THEN '["Parent", "Child", "컴파일 에러", "런타임 에러"]'::jsonb
        WHEN 13 THEN '["성능 최적화", "다형성 제한", "데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 것", "함수형 프로그래밍 패러다임으로의 전환"]'::jsonb
        WHEN 14 THEN '["단일 스레드 애플리케이션의 성능 향상", "스레드 안전성을 유지하면서 스레드별로 독립적인 상태를 관리해야 할 때", "여러 스레드 간 데이터 공유를 위해", "스레드 풀을 효율적으로 관리하기 위해"]'::jsonb
    END,
    CASE mod(seq, 15)
        WHEN 0 THEN 'G1(Garbage First) GC는 Java 7에서 도입되고 Java 9부터 기본 GC로 설정된 고성능 가비지 컬렉터입니다. G1 GC의 주요 특징은 힙을 균등한 크기의 영역(Region)으로 나누어 관리하며, 이를 통해 전체 힙이 아닌 일부 영역만 수집할 수 있어 중지 시간을 줄일 수 있습니다. 또한 사용자가 지정한 최대 중지 시간(pause time) 목표에 맞추어 GC 작업을 수행하며, 영역별로 가비지가 많은 영역(Garbage First)부터 수집하여 효율성을 높입니다. G1은 전체적으로는 Young과 Old 영역의 개념을 유지하지만, 물리적으로 인접해 있지 않고 논리적으로만 구분됩니다.'
        WHEN 1 THEN 'Java NIO(New Input/Output)는 Java 1.4에서 도입된 IO API로, 기존 IO 패키지와 비교해 몇 가지 주요 차이점이 있습니다. 1) 버퍼 지향: NIO는 버퍼(Buffer) 기반으로 작동하여 데이터를 일시적으로 저장하고 처리할 수 있습니다. 2) 논블로킹 IO: NIO는 논블로킹 모드를 지원하여 한 스레드가 여러 채널을 관리할 수 있습니다. 3) 채널: NIO는 채널(Channel)을 통해 데이터를 읽고 쓰며, 양방향 통신이 가능합니다. 4) 셀렉터: 하나의 스레드로 여러 채널을 모니터링할 수 있는 Selector를 제공합니다. 기존 IO는 스트림 지향적이고 블로킹 방식이며, 대용량 데이터나 많은 연결을 처리할 때 NIO가 더 효율적일 수 있습니다. 그러나 NIO가 항상 더 빠른 것은 아니며, 단순한 작업에는 기존 IO가 더 적합할 수 있습니다.'
        WHEN 2 THEN '자바는 가비지 컬렉션을 통해 메모리를 자동으로 관리하지만, 여전히 메모리 누수가 발생할 수 있습니다. 주요 원인으로는 1) 정적 필드: 정적 필드는 애플리케이션 수명 동안 유지되므로, 컬렉션이나 큰 객체를 정적 필드에 저장하고 계속 추가만 한다면 메모리가 계속 증가합니다. 2) 캐시: 캐시에 객체를 넣고 제거하지 않으면 메모리가 소진될 수 있습니다. 3) 리스너 등록 해제 누락: 리스너나 콜백을 등록하고 해제하지 않으면 참조가 남아 메모리 누수가 발생합니다. 4) 클로저와 내부 클래스: 내부 클래스는 외부 클래스 인스턴스를 참조하므로, 필요 이상으로 오래 유지되면 메모리 누수를 일으킬 수 있습니다. 5) 스트림, 연결 등 리소스 미반환: try-with-resources 등을 사용하지 않고 명시적으로 닫지 않으면 메모리 누수가 발생할 수 있습니다.'
        WHEN 3 THEN 'Java의 ClassLoader 시스템은 계층적 구조로 이루어져 있으며, 클래스를 JVM으로 로드하는 역할을 합니다. 1) Bootstrap ClassLoader: 가장 기본적인 클래스로더로, JVM의 일부이며 네이티브 코드로 구현됩니다. java.lang 패키지 등 Java API의 핵심 클래스를 로드합니다. 2) Extension(Platform) ClassLoader: Bootstrap ClassLoader의 자식으로, 자바 확장 API(ext 디렉토리의 클래스)를 로드합니다. 3) Application(System) ClassLoader: 사용자가 정의한 클래스패스 상의 클래스를 로드합니다. 이 클래스로더들은 위임 모델(delegation model)에 따라 작동합니다. 즉, 클래스 로드 요청이 오면 먼저 부모 클래스로더에게 위임하고, 부모가 로드할 수 없을 때만 자신이 로드를 시도합니다. 이를 통해 클래스의 유일성과 안전성을 보장합니다. 사용자는 또한 자신만의 커스텀 ClassLoader를 만들어 특별한 클래스 로딩 동작을 구현할 수도 있습니다.'
        WHEN 4 THEN '이 코드에서는 counter 변수를 두 개의 스레드에서 동시에 접근하여 증가시키고 있습니다. counter++는 원자적 연산이 아니라 읽기, 증가, 쓰기의 세 단계로 이루어지는 복합 연산입니다. 따라서 한 스레드가 counter 값을 읽고 아직 증가된 값을 쓰기 전에 다른 스레드가 같은 counter 값을 읽게 되면, 예상보다 적은 횟수만 증가하는 경쟁 상태(race condition)가 발생할 수 있습니다. 이 문제를 해결하려면 synchronized 키워드를 사용하거나, java.util.concurrent.atomic 패키지의 AtomicInteger 같은 원자적 변수 타입을 사용해야 합니다.'
        WHEN 5 THEN 'CompletableFuture는 Java 8에서 도입된 Future 인터페이스의 구현체로, 기존 Future보다 더 많은 기능을 제공합니다. 주요 차이점으로는 1) 작업 조합: thenApply, thenCompose, thenCombine 등의 메소드를 통해 비동기 작업을 순차적으로 조합하거나 병렬로 실행할 수 있습니다. 2) 콜백 지원: thenAccept, thenRun 등으로 결과가 준비되었을 때 실행할 콜백을 등록할 수 있습니다. 3) 예외 처리: exceptionally, handle 등으로 예외 처리 로직을 추가할 수 있습니다. 4) 완료 처리: complete 메소드로 외부에서 결과를 설정할 수 있습니다. 5) 다양한 팩토리 메소드: completedFuture, supplyAsync, runAsync 등 다양한 생성 방법을 제공합니다. 이러한 기능들은 복잡한 비동기 작업 흐름을 더 명확하고 유연하게 표현할 수 있게 해줍니다.'
        WHEN 6 THEN '메소드 참조(Method Reference)는 Java 8에서 도입된 기능으로, 이미 정의된 메소드를 람다 표현식 대신 사용할 수 있게 해줍니다. 메소드 참조는 :: 연산자를 사용하여 표현하며, 코드를 더 간결하게 만들 수 있습니다. 메소드 참조의 종류로는 1) 정적 메소드 참조: ClassName::staticMethodName 2) 인스턴스 메소드 참조: instance::instanceMethodName 3) 특정 타입의 인스턴스 메소드 참조: ClassName::instanceMethodName 4) 생성자 참조: ClassName::new 가 있습니다. 예시에서 System.out::println은 System.out 객체의 println 메소드를 참조하는 인스턴스 메소드 참조입니다.'
        WHEN 7 THEN 'Virtual Thread(가상 스레드)는 Java 19에서 프리뷰로 도입되고 Java 21에서 정식 기능으로 포함된 경량 스레드 구현입니다. 주요 특징으로는 1) 경량성: 운영체제 스레드에 비해 매우 적은 메모리를 사용하여 수백만 개의 가상 스레드를 생성할 수 있습니다. 2) 효율적인 블로킹: 가상 스레드가 블로킹 작업을 수행할 때 캐리어 스레드(플랫폼 스레드)를 점유하지 않고 양보하여 다른 가상 스레드가 실행될 수 있게 합니다. 3) 플랫폼 스레드와 동일한 API: 기존 Thread API와 호환되어 사용하기 쉽습니다. 4) 동시성 모델 개선: 많은 동시 요청을 처리하는 서버 애플리케이션에서 확장성을 크게 향상시킬 수 있습니다. 가상 스레드는 "스레드 당 요청" 모델을 효율적으로 구현할 수 있게 하여, 복잡한 비동기 프로그래밍 없이도 높은 처리량을 달성할 수 있습니다.'
        WHEN 8 THEN 'Java 모듈 시스템(JPMS)은 Java 9에서 Project Jigsaw의 일부로 도입되었습니다. 주요 목적과 특징으로는 1) 강력한 캡슐화: 모듈은 명시적으로 외부에 노출할 패키지만 공개하고, 나머지는 모듈 내부로 숨길 수 있습니다. 이는 패키지 수준의 접근 제한보다 더 강력한 캡슐화를 제공합니다. 2) 명시적 의존성: 모듈은 module-info.java 파일에 자신이 필요로 하는 의존성을 명시적으로 선언합니다. 이로 인해 런타임 전에 의존성 문제를 탐지할 수 있습니다. 3) 모듈화된 JDK: JDK 자체가 여러 모듈로 나뉘어, 애플리케이션에 필요한 모듈만 포함할 수 있게 되었습니다. 이는 더 작은 런타임과 배포 크기를 가능하게 합니다. 4) 보안 향상: 명시적으로 허용하지 않은 내부 API에 대한 접근이 차단되어 보안이 향상됩니다. 5) 플랫폼 무결성: JDK의 내부 API를 보호하여 플랫폼 진화를 더 용이하게 합니다. JPMS는 기존 패키지 시스템을 대체하는 것이 아니라 그 위에 구축되어 대규모 애플리케이션의 구조와 의존성을 더 잘 관리할 수 있게 해줍니다.'
        WHEN 9 THEN '패턴 매칭은 Java 16에서 정식으로 도입된 기능으로, 객체의 타입과 구조를 검사하고 조건이 일치하면 변수에 바인딩할 수 있습니다. 패턴 매칭은 instanceof 연산자와 함께 사용되며, 기존에 instanceof 검사 후 별도로 타입 캐스팅을 하던 코드를 더 간결하게 만들 수 있습니다. `if (obj instanceof String s) { System.out.println(s.length()); }`에서 객체가 String 타입이면 s 변수에 자동으로 캐스팅되어 바인딩됩니다. Java 17에서는 switch 문에서도 패턴 매칭을 사용할 수 있게 되었고, Java의 후속 버전에서는 레코드 패턴, 배열 패턴 등 더 다양한 패턴 매칭 기능이 추가될 예정입니다.'
        WHEN 10 THEN 'Record는 Java 16에서 정식 기능으로 도입된 새로운 유형의 클래스로, 데이터를 저장하기 위한 목적으로 설계되었습니다. 주요 특징으로는 1) 불변성: 레코드는 불변(immutable) 객체로, 생성 후 내부 상태를 변경할 수 없습니다. 따라서 setter 메소드가 자동 생성되지 않습니다. 2) 간결한 구문: `record Point(int x, int y) {}`와 같이 매우 간결하게 정의할 수 있습니다. 3) 자동 생성 메소드: 생성자, 각 필드의 접근자(예: x(), y()), equals(), hashCode(), toString() 메소드가 자동으로 생성됩니다. 4) 투명성: 레코드는 그 내용이 공개적으로 접근 가능하고 표현 가능합니다. 레코드는 주로 DTO(Data Transfer Object), 값 객체(Value Object) 등 데이터 전달 목적의 클래스를 간결하게 정의할 때 유용합니다.'
        WHEN 11 THEN 'Java 메모리 구조에서 Stack과 Heap은 서로 다른 목적과 특성을 가집니다. Stack 메모리는 1) 각 스레드마다 하나씩 할당됩니다. 2) 주로 메소드 호출 정보(스택 프레임), 지역 변수, 부분 결과, 메소드 매개변수 등을 저장합니다. 3) 메소드 호출이 완료되면 해당 프레임이 자동으로 제거됩니다(LIFO 구조). 4) 크기가 제한적이며, 초과하면 StackOverflowError가 발생합니다. Heap 메모리는 1) 모든 스레드가 공유하는 메모리 영역입니다. 2) 객체(인스턴스)와 배열이 저장됩니다. 3) 가비지 컬렉션에 의해 관리되며, 더 이상 참조되지 않는 객체는 자동으로 메모리가 해제됩니다. 4) JVM 시작 시 생성되고, 필요에 따라 크기가 조정될 수 있습니다. 5) 메모리가 부족하면 OutOfMemoryError가 발생합니다.'
        WHEN 12 THEN '이 코드는 변수의 정적 타입과 동적 타입에 관한 개념을 보여줍니다. 정적 타입(컴파일 타임에 결정되는 타입)에 따라 어떤 메소드를 호출할 수 있는지 결정되고, 동적 타입(런타임에 결정되는 실제 객체 타입)에 따라 오버라이딩된 메소드 중 어떤 것이 실행될지 결정됩니다. 이 예제에서 p는 Parent 타입 변수이므로 정적 타입은 Parent입니다. 그러나 실제로 가리키는 객체는 Child 타입이므로 동적 타입은 Child입니다. test() 메소드는 Parent 클래스에 정의되어 있으므로 호출 가능하며, 이 메소드는 Child 클래스에서 오버라이딩되었습니다. 메소드가 호출되면 동적 타입에 따라 Child 클래스의 메소드가 실행되고 "Child"가 출력됩니다. 반면, childOnlyMethod()는 Child 클래스에만 정의되어 있고 Parent 클래스에는 없으므로, Parent 타입 변수로는 컴파일 단계에서 호출할 수 없습니다.'
        WHEN 13 THEN 'Record, Sealed Classes, Pattern Matching은 최근 Java에 추가된 기능들로, 모두 데이터 중심 프로그래밍을 더 안전하고 간결하게 지원하는 공통 목적을 가지고 있습니다. 1) Record는 불변 데이터 객체를 간결하게 정의할 수 있게 해줍니다. 2) Sealed Classes는 클래스 계층을 명시적으로 제한하여 타입 안전성을 높입니다. 3) Pattern Matching은 데이터 구조를 분해하고 조건에 따라 처리하는 코드를 간결하게 작성할 수 있게 해줍니다. 이 세 기능을 함께 사용하면 대수적 데이터 타입(Algebraic Data Types)과 유사한 패턴을 자바에서도 안전하고 표현력 있게 구현할 수 있어, 함수형 언어에서 흔히 볼 수 있는 데이터 중심 프로그래밍 스타일을 지원합니다.'
        WHEN 14 THEN 'ThreadLocal은 각 스레드가 자신만의 독립적인 변수 사본을 가질 수 있게 해주는 클래스입니다. 주요 사용 사례로는 1) 사용자 인증 정보: 웹 애플리케이션에서 요청을 처리하는 스레드에 사용자 인증 정보를 저장합니다. 2) 트랜잭션 컨텍스트: 스프링과 같은 프레임워크에서 트랜잭션 관리를 위해 현재 트랜잭션 정보를 스레드에 바인딩합니다. 3) 날짜 포맷터: SimpleDateFormat과 같은 스레드 불안전 객체를 스레드마다 독립적으로 생성합니다. 4) 스레드 단위 캐싱: 특정 연산의 결과를 스레드별로 캐싱합니다. ThreadLocal은 스레드 안전성 문제 없이 스레드별 상태를 관리할 수 있어 유용하지만, 스레드 풀 환경에서는 ThreadLocal 변수가 재사용되는 스레드에 그대로 남아 메모리 누수나 예상치 못한 동작을 일으킬 수 있으므로, 사용 후 반드시 remove() 메소드로 값을 제거해야 합니다.'
        END,
    15, -- 고급 문제 15점
    60, -- 60초 제한시간
    (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%자바 고급%' ORDER BY random() LIMIT 1),
    CASE mod(seq, 15)
        WHEN 4 THEN 'public class CounterTest {\n    private static int counter = 0;\n    \n    public static void main(String[] args) throws InterruptedException {\n        Thread t1 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                counter++;\n            }\n        });\n        \n        Thread t2 = new Thread(() -> {\n            for(int i = 0; i < 1000; i++) {\n                counter++;\n            }\n        });\n        \n        t1.start();\n        t2.start();\n        \n        t1.join();\n        t2.join();\n        \n        System.out.println("Counter value: " + counter);\n    }\n}'
        WHEN 12 THEN 'class Parent {\n    public void test() {\n        System.out.println("Parent");\n    }\n}\n\nclass Child extends Parent {\n    @Override\n    public void test() {\n        System.out.println("Child");\n    }\n    \n    public void childOnlyMethod() {\n        System.out.println("Child only");\n    }\n}\n\npublic class Test {\n    public static void main(String[] args) {\n        Parent p = new Child();\n        p.test();\n        // p.childOnlyMethod(); // 컴파일 에러 발생\n    }\n}'
        ELSE NULL
        END
FROM generate_series(1, 20) AS seq
WHERE seq <= 15;

-- 16. 스프링 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '60 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 20)
        WHEN 0 THEN '스프링 프레임워크의 핵심 개념인 IoC(Inversion of Control)란 무엇인가?'
        WHEN 1 THEN '스프링에서 의존성 주입(Dependency Injection)의 방법이 아닌 것은?'
        WHEN 2 THEN '스프링 빈(Bean)의 기본 스코프는?'
        WHEN 3 THEN '스프링 MVC에서 @Controller와 @RestController의 차이점은?'
        WHEN 4 THEN '스프링에서 AOP(Aspect-Oriented Programming)의 주요 용도는?'
        WHEN 5 THEN '스프링 부트(Spring Boot)의 주요 장점이 아닌 것은?'
        WHEN 6 THEN '스프링에서 @Transactional 어노테이션의 전파 속성(propagation) 중 기본값은?'
        WHEN 7 THEN '스프링 시큐리티(Spring Security)의 인증(Authentication)과 인가(Authorization)의 차이점은?'
        WHEN 8 THEN '스프링에서 JPA와 Hibernate의 관계는?'
        WHEN 9 THEN '스프링에서 @Autowired 어노테이션을 사용할 때, 동일한 타입의 빈이 여러 개 있을 경우 해결 방법이 아닌 것은?'
        WHEN 10 THEN '스프링 부트에서 외부 설정 값을 가져오는 방법이 아닌 것은?'
        WHEN 11 THEN '스프링에서 싱글톤 빈이 상태를 가질 때 발생할 수 있는 문제는?'
        WHEN 12 THEN '스프링 부트 애플리케이션을 프로덕션 환경에 배포할 때 고려해야 할 사항이 아닌 것은?'
        WHEN 13 THEN '스프링에서 @RequestBody와 @ResponseBody 어노테이션의 역할은?'
        WHEN 14 THEN '스프링에서 DispatcherServlet의 역할은?'
        WHEN 15 THEN '스프링 부트의 자동 설정(Auto-configuration) 원리는?'
        WHEN 16 THEN '스프링에서 Bean Validation API(@Valid, @NotNull 등)를 사용하는 위치로 적절하지 않은 것은?'
        WHEN 17 THEN '스프링의 @Transactional 어노테이션이 동작하지 않을 수 있는 경우는?'
        WHEN 18 THEN '스프링 애플리케이션 컨텍스트(Application Context)가 로드될 때의 단계가 올바른 순서로 나열된 것은?'
        WHEN 19 THEN '스프링 부트 액추에이터(Spring Boot Actuator)의 주요 기능이 아닌 것은?'
        END,
    'MULTIPLE_CHOICE',
    CASE
        WHEN seq < 7 THEN 'BEGINNER'
        WHEN seq < 14 THEN 'INTERMEDIATE'
        ELSE 'ADVANCED'
        END,
    CASE mod(seq, 20)
        WHEN 0 THEN '객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것'
        WHEN 1 THEN '메소드 오버라이딩 주입'
        WHEN 2 THEN 'singleton'
        WHEN 3 THEN '@Controller는 주로 뷰를 반환하고, @RestController는 데이터(JSON/XML)를 직접 반환한다'
        WHEN 4 THEN '트랜잭션 관리, 로깅, 보안과 같은 횡단 관심사를 모듈화하는 것'
        WHEN 5 THEN '복잡한 XML 설정이 필요하다'
        WHEN 6 THEN 'REQUIRED'
        WHEN 7 THEN '인증은 사용자가 누구인지 확인하는 과정이고, 인가는 인증된 사용자가 특정 자원에 접근할 권한이 있는지 확인하는 과정이다'
        WHEN 8 THEN 'JPA는 자바 ORM 표준 스펙이고, Hibernate는 JPA의 구현체 중 하나이다'
        WHEN 9 THEN '두 빈 모두 사용하기 위해 @MixedAutowired 어노테이션 사용'
        WHEN 10 THEN '@ConfigurationValue 어노테이션 사용'
        WHEN 11 THEN '멀티스레드 환경에서 동시성 문제가 발생할 수 있다'
        WHEN 12 THEN '모든 로깅을 비활성화하여 성능 향상'
        WHEN 13 THEN '@RequestBody는 HTTP 요청 본문을 자바 객체로 변환하고, @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환한다'
        WHEN 14 THEN '프론트 컨트롤러로서 모든 웹 요청을 받아 적절한 핸들러로 분배한다'
        WHEN 15 THEN '@Conditional 어노테이션을 기반으로 classpath의 라이브러리, 기존 설정, 환경 등을 고려하여 자동으로 빈을 구성한다'
        WHEN 16 THEN 'private 메소드 매개변수 검증'
        WHEN 17 THEN '같은 클래스 내에서 @Transactional 메소드 호출'
        WHEN 18 THEN '빈 정의 로딩 → 빈 정의 검증 → 빈 전처리 → 빈 인스턴스화 및 의존성 주입 → 초기화 콜백 호출'
        WHEN 19 THEN '자동으로 데이터베이스 스키마 생성'
        END,
    CASE mod(seq, 20)
        WHEN 0 THEN '["MVC 패턴을 구현하는 방법", "객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것", "데이터베이스 연결을 자동화하는 기술", "비동기 처리를 간소화하는 기법"]'::jsonb
        WHEN 1 THEN '["생성자 주입(Constructor Injection)", "세터 주입(Setter Injection)", "필드 주입(Field Injection)", "메소드 오버라이딩 주입"]'::jsonb
        WHEN 2 THEN '["singleton", "prototype", "request", "session"]'::jsonb
        WHEN 3 THEN '["@Controller는 싱글톤이고, @RestController는 프로토타입이다", "@Controller는 주로 뷰를 반환하고, @RestController는 데이터(JSON/XML)를 직접 반환한다", "@Controller는 스프링 3 이상에서만 사용 가능하고, @RestController는 스프링 4 이상에서만 사용 가능하다", "@Controller는 동기 방식만 지원하고, @RestController는 비동기 방식도 지원한다"]'::jsonb
        WHEN 4 THEN '["객체 생성을 자동화하는 것", "데이터베이스 연결을 관리하는 것", "트랜잭션 관리, 로깅, 보안과 같은 횡단 관심사를 모듈화하는 것", "MVC 패턴을 구현하는 것"]'::jsonb
        WHEN 5 THEN '["내장 서버를 제공하여 별도의 웹 서버 설정이 필요 없다", "자동 설정(Auto Configuration)을 통해 설정을 간소화한다", "스타터 의존성으로 라이브러리 관리가 쉽다", "복잡한 XML 설정이 필요하다"]'::jsonb
        WHEN 6 THEN '["REQUIRED", "REQUIRES_NEW", "SUPPORTS", "MANDATORY"]'::jsonb
        WHEN 7 THEN '["인증은 스프링 전용 기능이고, 인가는 Java EE 표준 기능이다", "인증은 사용자가 누구인지 확인하는 과정이고, 인가는 인증된 사용자가 특정 자원에 접근할 권한이 있는지 확인하는 과정이다", "인증은 정적 리소스에 대한 접근을 관리하고, 인가는 동적 리소스에 대한 접근을 관리한다", "인증은 서버 측에서만 수행되고, 인가는 클라이언트 측에서도 수행될 수 있다"]'::jsonb
        WHEN 8 THEN '["JPA와 Hibernate는 동일한 것이다", "JPA는 Hibernate의 발전된 버전이다", "JPA는 자바 ORM 표준 스펙이고, Hibernate는 JPA의 구현체 중 하나이다", "Hibernate는 JPA의 상위 개념이다"]'::jsonb
        WHEN 9 THEN '["@Primary 어노테이션으로 기본 빈 지정", "@Qualifier 어노테이션으로 특정 빈 선택", "이름으로 자동 매칭", "두 빈 모두 사용하기 위해 @MixedAutowired 어노테이션 사용"]'::jsonb
        WHEN 10 THEN '["application.properties 또는 application.yml 파일 사용", "@Value 어노테이션으로 프로퍼티 값 주입", "@ConfigurationProperties 어노테이션으로 프로퍼티 클래스 바인딩", "@ConfigurationValue 어노테이션 사용"]'::jsonb
        WHEN 11 THEN '["빈 생성 시간이 길어진다", "메모리 사용량이 증가한다", "멀티스레드 환경에서 동시성 문제가 발생할 수 있다", "다른 빈과 결합도가 높아진다"]'::jsonb
        WHEN 12 THEN '["적절한 프로파일 설정", "보안 설정 강화", "모니터링 및 헬스 체크 설정", "모든 로깅을 비활성화하여 성능 향상"]'::jsonb
        WHEN 13 THEN '["@RequestBody는 URL 파라미터를 자바 객체로 변환하고, @ResponseBody는 자바 객체를 JSON으로 변환한다", "@RequestBody는 HTTP 요청 본문을 자바 객체로 변환하고, @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환한다", "@RequestBody는 HTTP 요청을 검증하고, @ResponseBody는 HTTP 응답을 압축한다", "@RequestBody와 @ResponseBody는 모두 RESTful 서비스에서만 사용할 수 있다"]'::jsonb
        WHEN 14 THEN '["빈의 생명주기를 관리한다", "프론트 컨트롤러로서 모든 웹 요청을 받아 적절한 핸들러로 분배한다", "데이터베이스 연결을 관리한다", "뷰를 렌더링한다"]'::jsonb
        WHEN 15 THEN '["모든 가능한 빈을 무조건 생성한 후 필요 없는 것을 제거한다", "@Conditional 어노테이션을 기반으로 classpath의 라이브러리, 기존 설정, 환경 등을 고려하여 자동으로 빈을 구성한다", "사용자가 작성한 XML 설정 파일을 분석한다", "모든 설정을 런타임에 결정한다"]'::jsonb
        WHEN 16 THEN '["컨트롤러 메소드의 @RequestBody 매개변수 검증", "폼 제출 데이터 검증", "JPA 엔티티 속성 검증", "private 메소드 매개변수 검증"]'::jsonb
        WHEN 17 THEN '["public 메소드에 적용한 경우", "외부에서 해당 메소드를 호출한 경우", "같은 클래스 내에서 @Transactional 메소드 호출", "DataSource가 제대로 설정된 경우"]'::jsonb
        WHEN 18 THEN '["빈 인스턴스화 → 빈 정의 로딩 → 의존성 주입 → 초기화 콜백 호출", "빈 정의 로딩 → 빈 정의 검증 → 빈 전처리 → 빈 인스턴스화 및 의존성 주입 → 초기화 콜백 호출", "의존성 주입 → 빈 인스턴스화 → 초기화 콜백 호출 → 빈 정의 검증", "빈 정의 로딩 → 빈 인스턴스화 → 초기화 콜백 호출 → 의존성 주입"]'::jsonb
        WHEN 19 THEN '["애플리케이션 상태 및 헬스 체크", "메트릭 수집 및 모니터링", "자동으로 데이터베이스 스키마 생성", "환경 정보 및 구성 속성 조회"]'::jsonb
        END,
    CASE mod(seq, 20)
        WHEN 0 THEN 'IoC(Inversion of Control, 제어의 역전)는 스프링의 핵심 개념으로, 전통적인 프로그래밍에서 개발자가 직접 객체를 생성하고 의존 관계를 설정하던 제어 흐름을 역전시켜 프레임워크가 이를 대신 담당하는 것을 의미합니다. 스프링에서는 IoC 컨테이너가 빈(Bean)을 생성, 관리하고 필요한 곳에 주입합니다. 이를 통해 객체 간의 결합도를 낮추고, 코드의 재사용성과 테스트 용이성을 높이며, 객체 지향 설계 원칙을 더 쉽게 적용할 수 있습니다.'
        WHEN 1 THEN '스프링에서 의존성 주입(DI)은 주로 세 가지 방법으로 이루어집니다: 1) 생성자 주입(Constructor Injection): 생성자를 통해 의존성을 주입받는 방식으로, 필수적인 의존성을 명확히 하고 불변성을 보장합니다. 2) 세터 주입(Setter Injection): setter 메소드를 통해 의존성을 주입받는 방식으로, 선택적인 의존성이나 런타임에 의존성을 변경해야 할 때 유용합니다. 3) 필드 주입(Field Injection): @Autowired 어노테이션을 필드에 직접 사용하는 방식으로, 코드는 간결하지만 테스트하기 어렵고 순환 의존성을 감지하기 어려워 권장되지 않습니다. "메소드 오버라이딩 주입"은 존재하지 않는 의존성 주입 방법입니다.'
        WHEN 2 THEN '스프링 빈의 기본 스코프는 singleton입니다. 이는 스프링 IoC 컨테이너당 하나의 인스턴스만 생성되어 공유됨을 의미합니다. 다른 스코프로는 1) prototype: 요청할 때마다 새로운 인스턴스 생성 2) request: HTTP 요청마다 새로운 인스턴스 생성 3) session: HTTP 세션마다 새로운 인스턴스 생성 4) application: ServletContext 생명주기 동안 하나의 인스턴스 5) websocket: WebSocket 생명주기 동안 하나의 인스턴스 등이 있습니다. 대부분의 경우 상태를 유지하지 않는 서비스 객체는 싱글톤으로 관리하는 것이 메모리 효율성과 성능 면에서 유리합니다.'
        WHEN 3 THEN '@Controller와 @RestController의 주요 차이점은 반환 값의 처리 방식입니다. @Controller는 전통적인 스프링 MVC 컨트롤러로, 주로 뷰 이름을 반환하고 ViewResolver를 통해 해당 뷰를 찾아 렌더링합니다. 메소드에 @ResponseBody를 추가하면 뷰 대신 데이터를 직접 반환할 수도 있습니다. @RestController는 @Controller + @ResponseBody의 조합으로, 모든 메소드가 기본적으로 데이터를 직접 HTTP 응답 본문으로 반환합니다. 주로 RESTful 웹 서비스에서 JSON이나 XML 형태의 데이터를 반환할 때 사용됩니다.'
        WHEN 4 THEN 'AOP(Aspect-Oriented Programming, 관점 지향 프로그래밍)는 OOP를 보완하는 프로그래밍 패러다임으로, 애플리케이션 전반에 걸쳐 나타나는 횡단 관심사(cross-cutting concerns)를 모듈화하는 방법을 제공합니다. 스프링에서 AOP의 주요 용도로는 1) 트랜잭션 관리: @Transactional 어노테이션을 통해 메소드 실행 전후로 트랜잭션을 시작하고 커밋/롤백합니다. 2) 로깅: 메소드 호출과 리턴 값을 로깅합니다. 3) 보안: 메소드 실행 전에 권한을 확인합니다. 4) 캐싱: 메소드 결과를 캐시하고 재사용합니다. 5) 에러 처리: 예외 발생 시 일관된 방식으로 처리합니다. AOP를 사용하면 이러한 관심사를 비즈니스 로직에서 분리하여 코드의 재사용성과 모듈성을 높일 수 있습니다.'
        WHEN 5 THEN '스프링 부트는 기존 스프링 프레임워크의 복잡성을 해소하고 더 빠른 개발을 가능하게 하는 프로젝트입니다. 주요 장점으로는 1) 내장 서버(Tomcat, Jetty, Undertow 등)를 제공하여 독립 실행 가능한 애플리케이션을 만들 수 있습니다. 2) 자동 설정(Auto Configuration)을 통해 classpath와 설정에 기반하여 필요한 빈들을 자동으로 구성합니다. 3) 스타터 의존성(Spring Boot Starters)으로 필요한 라이브러리를 쉽게 추가할 수 있습니다. 4) 프로덕션 준비 기능(actuator, metrics, health checks 등)을 제공합니다. 5) XML 설정 없이 Java 기반 설정이나 properties/YAML 파일로 간단하게 설정할 수 있습니다. "복잡한 XML 설정이 필요하다"는 스프링 부트의 장점이 아니라 오히려 스프링 부트가 해결하고자 하는 기존 스프링의 단점입니다.'
        WHEN 6 THEN '@Transactional 어노테이션의 전파 속성(propagation)은 트랜잭션 경계에서 이미 진행 중인 트랜잭션이 있을 때 어떻게 동작할지 결정합니다. 기본값은 REQUIRED로, 현재 진행 중인 트랜잭션이 있으면 그 트랜잭션을 사용하고, 없으면 새 트랜잭션을 시작합니다. 다른 속성으로는 1) REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하고, 진행 중이던 트랜잭션은 일시 중단합니다. 2) SUPPORTS: 진행 중인 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 실행합니다. 3) MANDATORY: 진행 중인 트랜잭션이 반드시 있어야 하며, 없으면 예외가 발생합니다. 4) NEVER: 트랜잭션 없이 실행되어야 하며, 진행 중인 트랜잭션이 있으면 예외가 발생합니다. 5) NOT_SUPPORTED: 트랜잭션 없이 실행하며, 진행 중인 트랜잭션이 있으면 일시 중단합니다. 6) NESTED: 진행 중인 트랜잭션이 있으면 중첩 트랜잭션을 생성하고, 없으면 REQUIRED처럼 동작합니다.'
        WHEN 7 THEN '스프링 시큐리티에서 인증(Authentication)과 인가(Authorization)는 보안의 두 가지 핵심 개념입니다. 인증은 "당신이 누구인지 증명하는 과정"으로, 사용자의 신원을 확인합니다. 일반적으로 사용자 이름과 비밀번호, 토큰, 인증서 등을 통해 이루어집니다. 인가는 "당신이 무엇을 할 수 있는지 결정하는 과정"으로, 인증된 사용자가 특정 리소스에 접근하거나 작업을 수행할 권한이 있는지 확인합니다. 스프링 시큐리티에서는 인증은 AuthenticationManager를 통해 처리되며, 인가는 AccessDecisionManager를 통해 처리됩니다. 간단히 말해, 인증은 로그인 과정이고, 인가는 로그인 후 특정 페이지나 기능에 접근할 수 있는지 결정하는 과정입니다.'
        WHEN 8 THEN 'JPA(Java Persistence API)는 자바 애플리케이션에서 관계형 데이터베이스를 사용하는 방식을 정의한 인터페이스 모음인 자바 ORM 표준 스펙입니다. JPA는 객체와 테이블 간의 매핑, 엔티티의 생명주기 관리, JPQL(객체 지향 쿼리 언어) 등을 정의합니다. Hibernate는 JPA 명세의 구현체 중 하나로, 가장 널리 사용되는 ORM 프레임워크입니다. Hibernate는 JPA의 모든 기능을 구현할 뿐 아니라, JPA 표준을 넘어서는 추가 기능(예: 독자적인 HQL, 두 번째 수준 캐시 등)도 제공합니다. 스프링에서는 Spring Data JPA를 통해 JPA를 더 쉽게 사용할 수 있으며, 기본적으로 Hibernate를 JPA 구현체로 사용합니다.'
        WHEN 9 THEN '스프링에서 @Autowired를 사용할 때 동일한 타입의 빈이 여러 개 있으면 "NoUniqueBeanDefinitionException" 예외가 발생할 수 있습니다. 이를 해결하는 방법으로는 1) @Primary: 동일 타입의 빈 중 하나에 @Primary를 지정하여 우선적으로 주입되게 합니다. 2) @Qualifier: 주입 지점에서 @Qualifier("빈이름")으로 특정 빈을 명시적으로 선택합니다. 3) 필드/메소드 이름으로 매칭: 필드나 메소드 매개변수 이름을 빈의 이름과 일치시켜 자동으로 매칭되게 합니다. 4) 컬렉션 주입: List<Interface>나 Map<String, Interface> 타입으로 주입받아 모든 구현체를 사용합니다. "@MixedAutowired"는 존재하지 않는 어노테이션입니다.'
        WHEN 10 THEN '스프링 부트에서 외부 설정을 가져오는 일반적인 방법으로는 1) application.properties 또는 application.yml 파일을 사용하여 설정 값을 정의합니다. 2) @Value 어노테이션으로 개별 프로퍼티 값을 주입합니다(예: @Value("${app.name}")). 3) @ConfigurationProperties 어노테이션을 사용하여 프로퍼티 그룹을 자바 클래스에 바인딩합니다. 4) Environment 객체를 통해 프로그래밍 방식으로 프로퍼티에 접근합니다. 5) 명령행 인자, 환경 변수, OS 환경변수 등 다양한 소스에서 설정을 가져올 수 있습니다. "@ConfigurationValue"는 존재하지 않는 어노테이션입니다.'
        WHEN 11 THEN '스프링의 기본 스코프인 싱글톤 빈이 상태를 가질 때(즉, 인스턴스 변수를 사용할 때) 발생할 수 있는 주요 문제는 멀티스레드 환경에서의 동시성 문제입니다. 싱글톤 빈은 애플리케이션 컨텍스트에서 하나의 인스턴스만 생성되어 모든 요청이 이를 공유하기 때문에, 여러 스레드가 동시에 같은 인스턴스 변수에 접근하면 경쟁 상태(race condition), 데이터 불일치, 예측 불가능한 동작 등이 발생할 수 있습니다. 이러한 문제를 해결하기 위해서는 1) 싱글톤 빈을 상태가 없는(stateless) 방식으로 설계하고, 상태가 필요한 경우 메소드 지역 변수를 사용합니다. 2) 꼭 상태가 필요하다면 스레드 로컬 변수(ThreadLocal)를 사용하거나 동기화 메커니즘을 적용합니다. 3) prototype, request, session 등 다른 스코프를 사용하여 각 요청이나 세션마다 별도의 인스턴스를 사용하도록 합니다.'
        WHEN 12 THEN '스프링 부트 애플리케이션을 프로덕션 환경에 배포할 때 고려해야 할 주요 사항으로는 1) 프로파일 관리: 개발, 테스트, 프로덕션 환경에 맞는 설정을 프로파일로 분리하여 관리합니다. 2) 보안 설정: HTTPS 적용, 민감한 정보(비밀번호, API 키 등) 암호화, 적절한 인증/인가 설정을 합니다. 3) 모니터링 및 관찰성: 액추에이터(Actuator) 엔드포인트를 활용하여 애플리케이션 상태, 메트릭, 헬스 체크를 설정하고, 모니터링 도구와 통합합니다. 4) 로깅 설정: 적절한 로그 레벨 설정과 로그 관리 전략이 필요합니다. 5) 성능 최적화: JVM 옵션, 캐싱, 커넥션 풀 설정 등을 통해 성능을 최적화합니다. 6) 무중단 배포 전략: 블루-그린 배포, 카나리 배포 등을 고려합니다. "모든 로깅을 비활성화"하는 것은 문제 발생 시 진단을 어렵게 만들어 좋은 방법이 아닙니다. 대신 필요한 정보만 적절한 레벨로 로깅하는 것이 중요합니다.'
        WHEN 13 THEN '@RequestBody와 @ResponseBody는 HTTP 메시지 변환과 관련된 스프링 MVC 어노테이션입니다. @RequestBody는 HTTP 요청 본문(body)을 자바 객체로 변환합니다. 주로 POST나 PUT 요청에서 JSON이나 XML 같은 데이터를 자바 객체로 역직렬화할 때 사용합니다. 내부적으로 HttpMessageConverter를 사용하여 변환이 이루어집니다. @ResponseBody는 자바 객체를 HTTP 응답 본문으로 변환합니다. 컨트롤러 메소드의 반환 값을 뷰를 통해 렌더링하지 않고, 직접 HTTP 응답 본문으로 변환하여 클라이언트에게 전송합니다. 마찬가지로 HttpMessageConverter를 사용하여 객체를 JSON, XML 등으로 직렬화합니다. @RestController를 사용하면 모든 메소드에 @ResponseBody가 자동으로 적용됩니다.'
        WHEN 14 THEN 'DispatcherServlet은 스프링 MVC의 핵심 컴포넌트로, 프론트 컨트롤러(Front Controller) 패턴을 구현합니다. 주요 역할은 1) 모든 웹 요청을 중앙에서 받아들입니다. 2) 요청을 처리할 적절한 핸들러(컨트롤러)를 찾아 요청을 위임합니다. 3) 핸들러가 반환한 결과를 적절한 뷰에 전달하거나 직접 응답을 생성합니다. 4) 예외 처리, 지역화, 테마 결정 등의 작업을 처리합니다. DispatcherServlet의 동작 흐름은 다음과 같습니다: 요청 접수 → HandlerMapping으로 핸들러 결정 → HandlerAdapter를 통해 핸들러 실행 → 핸들러가 ModelAndView 반환 → ViewResolver로 뷰 결정 → 뷰 렌더링 → 응답 반환. 이러한 아키텍처는 웹 요청 처리 과정을 모듈화하고 확장성을 높이는 데 기여합니다.'
        WHEN 15 THEN '스프링 부트의 자동 설정(Auto-configuration)은 개발자가 최소한의 설정으로 애플리케이션을 실행할 수 있도록 하는 핵심 기능입니다. 주요 원리는 다음과 같습니다: 1) @SpringBootApplication 어노테이션에 포함된 @EnableAutoConfiguration이 자동 설정을 활성화합니다. 2) 스프링 부트는 classpath에 있는 spring.factories 파일에서 AutoConfiguration 클래스 목록을 로드합니다. 3) 각 AutoConfiguration 클래스는 @Conditional 계열 어노테이션(@ConditionalOnClass, @ConditionalOnBean, @ConditionalOnProperty 등)을 사용하여 특정 조건이 충족될 때만 설정이 적용되도록 합니다. 4) 조건에는 특정 클래스의 존재 여부, 특정 빈의 존재 여부, 특정 프로퍼티 값 등이 포함됩니다. 5) 사용자가 명시적으로 빈을 정의하면 자동 설정보다 우선 적용됩니다. 이 방식을 통해 스프링 부트는 개발자가 필요한 라이브러리만 추가하면, 해당 라이브러리가 동작하는 데 필요한 빈들을 자동으로 구성해줍니다.'
        WHEN 16 THEN 'Bean Validation API는 자바 빈 검증을 위한 표준 API로, 스프링에서는 주로 다음 위치에서 사용됩니다: 1) 컨트롤러 메소드의 매개변수 검증: @Valid/@Validated를 @RequestBody, @ModelAttribute 등과 함께 사용하여 입력 데이터를 검증합니다. 2) 폼 제출 데이터 검증: 웹 폼에서 제출된 데이터를 검증합니다. 3) JPA 엔티티 속성 검증: 엔티티가 저장되기 전에 속성을 검증합니다. 그러나 private 메소드 매개변수 검증은 Bean Validation API로 직접 지원되지 않습니다. Bean Validation은 기본적으로 public 메소드와 필드를 대상으로 동작하며, 메소드 검증을 위해서는 @Validated 어노테이션이 적용된 클래스의 public 메소드에만 적용됩니다. private 메소드 매개변수 검증은 개발자가 직접 코드로 구현해야 합니다.'
        WHEN 17 THEN '@Transactional 어노테이션이 동작하지 않을 수 있는 주요 경우들은 다음과 같습니다: 1) 같은 클래스 내의 메소드 호출: 스프링의 트랜잭션은 프록시 기반으로 동작하기 때문에, 같은 클래스 내에서 @Transactional 메소드를 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않습니다. 2) private, protected, 또는 default 가시성 메소드: @Transactional은 public 메소드에만 기본적으로 적용됩니다. 3) 런타임 예외가 아닌 체크 예외 발생: 기본적으로 런타임 예외(unchecked exception)에서만 롤백이 발생합니다. 4) 트랜잭션 관리자 미설정: 적절한 PlatformTransactionManager가 설정되지 않은 경우. 5) @EnableTransactionManagement 미설정: XML 설정이나 자바 설정에서 트랜잭션 관리를 활성화하지 않은 경우. 이러한 문제를 해결하기 위해서는 별도의 서비스 클래스로 분리하거나, 자기 주입(self-injection), 또는 AopContext.currentProxy()를 사용하는 방법 등이 있습니다.'
        WHEN 18 THEN '스프링 애플리케이션 컨텍스트 로딩 과정은 여러 단계로 이루어집니다: 1) 빈 정의 로딩: XML, 어노테이션, Java Config 등에서 빈 정의를 읽어옵니다. 2) 빈 정의 검증: 로드된 빈 정의의 유효성을 검사합니다. 3) 빈 전처리: BeanFactoryPostProcessor를 실행하여 빈 정의를 수정할 기회를 제공합니다(예: PropertyPlaceholderConfigurer가 이 단계에서 프로퍼티 값을 해석합니다). 4) 빈 인스턴스화: 빈 인스턴스를 생성합니다. 5) 의존성 주입: 생성자, 세터, 필드 주입을 통해 의존성을 설정합니다. 6) BeanPostProcessor 적용: 빈 후처리기를 실행하여 빈 인스턴스를 수정합니다. 7) 초기화 콜백 호출: InitializingBean의 afterPropertiesSet() 메소드나 @PostConstruct, init-method 등의 초기화 메소드를 호출합니다. 8) 빈 사용 준비 완료: 이제 빈을 사용할 수 있습니다. 컨텍스트가 종료될 때는 소멸 전 콜백(@PreDestroy, DisposableBean의 destroy() 등)이 호출됩니다.'
        WHEN 19 THEN '스프링 부트 액추에이터(Spring Boot Actuator)는 프로덕션 환경에서 애플리케이션을 모니터링하고 관리하기 위한 기능을 제공합니다. 주요 기능으로는 1) 애플리케이션 상태 및 헬스 체크: /actuator/health 엔드포인트를 통해 애플리케이션의 상태를 확인할 수 있습니다. 2) 메트릭 수집 및 모니터링: /actuator/metrics 엔드포인트로 JVM 메모리, CPU 사용량, HTTP 요청 등의 메트릭을 제공합니다. 3) 환경 정보 및 구성 속성: /actuator/env, /actuator/configprops 등으로 현재 환경 및 설정 정보를 조회할 수 있습니다. 4) 로깅 레벨 조회 및 변경: /actuator/loggers를 통해 로그 레벨을 실시간으로 조정할 수 있습니다. 5) 스레드 덤프, 힙 덤프: 디버깅에 필요한 정보를 제공합니다. 6) HTTP 추적: 최근 HTTP 요청/응답 정보를 조회할 수 있습니다. "자동으로 데이터베이스 스키마 생성"은 액추에이터의 기능이 아니라, 스프링 부트의 데이터 관련 기능(spring.jpa.hibernate.ddl-auto 등)에 해당합니다.'
        END,
    CASE
        WHEN seq < 7 THEN 5 -- 초급 문제 5점
        WHEN seq < 14 THEN 10 -- 중급 문제 10점
        ELSE 15 -- 고급 문제 15점
        END,
    CASE
        WHEN seq < 7 THEN 45 -- 초급 45초
        WHEN seq < 14 THEN 60 -- 중급 60초
        ELSE 75 -- 고급 75초
        END,
    CASE
        WHEN seq < 7 THEN (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%스프링%' AND q.difficulty_level = 'BEGINNER' ORDER BY random() LIMIT 1)
        WHEN seq < 14 THEN (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%스프링%' AND q.difficulty_level = 'INTERMEDIATE' ORDER BY random() LIMIT 1)
        ELSE (SELECT q.id FROM public.quizzes q WHERE q.title LIKE '%스프링%' AND q.difficulty_level = 'ADVANCED' ORDER BY random() LIMIT 1)
        END,
    NULL
FROM generate_series(0, 19) AS seq;


        -- 17. 자바 추가 퀴즈와 스프링 하위 태그 연결
WITH java_quizzes AS (
    SELECT id FROM public.quizzes WHERE title LIKE '%자바%' OR title LIKE '%Java%'
)
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT jq.id, t.id
FROM java_quizzes jq
         CROSS JOIN (
    SELECT id FROM public.tags WHERE name IN ('Java 람다', 'Java 디자인패턴', 'Java JVM', 'Java 컬렉션')
    ORDER BY random() LIMIT 1
) t
WHERE NOT EXISTS (
    SELECT 1 FROM public.quiz_tags WHERE quiz_id = jq.id AND tag_id = t.id
);

WITH spring_quizzes AS (
    SELECT id FROM public.quizzes WHERE title LIKE '%스프링%' OR title LIKE '%Spring%'
)
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT sq.id, t.id
FROM spring_quizzes sq
         CROSS JOIN (
    SELECT id FROM public.tags WHERE name IN ('Spring AOP', 'Spring Security', 'Spring Cloud')
    ORDER BY random() LIMIT 1
) t
WHERE NOT EXISTS (
    SELECT 1 FROM public.quiz_tags WHERE quiz_id = sq.id AND tag_id = t.id
);

-- 더미 데이터 생성 완료 확인
SELECT 'Java 및 Spring 관련 더미 데이터 생성 완료' as result;
