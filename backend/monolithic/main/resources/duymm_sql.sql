-- 사용자 생성 (관리자 1명, 일반 사용자 2명)
INSERT INTO public.users (created_at, email, experience, is_active, level, provider, provider_id, required_experience, role, total_points, updated_at, username)
VALUES
    (NOW(), 'admin@javaquiz.com', 1000, true, 10, 'GITHUB', 'admin123', 2000, 'ADMIN', 5000, NOW(), 'JavaQuizAdmin'),
    (NOW(), 'user1@example.com', 500, true, 5, 'GOOGLE', 'user123', 1000, 'USER', 2500, NOW(), 'JavaLearner'),
    (NOW(), 'user2@example.com', 300, true, 3, 'GITHUB', 'user456', 500, 'USER', 1200, NOW(), 'SpringMaster');

-- 태그 생성
-- 최상위 태그
INSERT INTO public.tags (created_at, description, name, parent_id)
VALUES
    -- 주요 카테고리 태그
    (NOW(), 'Java 언어 관련 모든 퀴즈', 'Java', NULL),
    (NOW(), 'Spring 프레임워크 관련 모든 퀴즈', 'Spring', NULL);

-- 하위 태그 (Java)
INSERT INTO public.tags (created_at, description, name, parent_id)
VALUES
    (NOW(), 'Java 기본 문법과 개념', 'Core Java', (SELECT id FROM public.tags WHERE name = 'Java')),
    (NOW(), 'Java 8 이상의 새로운 기능들', 'Java 8+', (SELECT id FROM public.tags WHERE name = 'Java')),
    (NOW(), 'Java Collections Framework', 'Java Collections', (SELECT id FROM public.tags WHERE name = 'Java')),
    (NOW(), '멀티쓰레딩과 동시성 프로그래밍', 'Java Concurrency', (SELECT id FROM public.tags WHERE name = 'Java')),
    (NOW(), '객체지향 프로그래밍 개념', 'Java OOP', (SELECT id FROM public.tags WHERE name = 'Java')),
    (NOW(), 'JVM 구조와 메모리 관리', 'JVM', (SELECT id FROM public.tags WHERE name = 'Java'));

-- 하위 태그 (Spring)
INSERT INTO public.tags (created_at, description, name, parent_id)
VALUES
    (NOW(), 'Spring 프레임워크 핵심 개념', 'Spring Core', (SELECT id FROM public.tags WHERE name = 'Spring')),
    (NOW(), 'Spring Boot 자동 구성과 기능', 'Spring Boot', (SELECT id FROM public.tags WHERE name = 'Spring')),
    (NOW(), '웹 애플리케이션 개발을 위한 Spring MVC', 'Spring MVC', (SELECT id FROM public.tags WHERE name = 'Spring')),
    (NOW(), '데이터 액세스 기술', 'Spring Data', (SELECT id FROM public.tags WHERE name = 'Spring')),
    (NOW(), '보안 및 인증 관련 기능', 'Spring Security', (SELECT id FROM public.tags WHERE name = 'Spring'));

-- 태그 동의어
INSERT INTO public.tag_synonyms (tag_id, synonym)
VALUES
    ((SELECT id FROM public.tags WHERE name = 'Java'), 'JDK'),
    ((SELECT id FROM public.tags WHERE name = 'Java'), 'J2SE'),
    ((SELECT id FROM public.tags WHERE name = 'Spring'), 'SpringFramework'),
    ((SELECT id FROM public.tags WHERE name = 'Spring Boot'), 'SpringBoot'),
    ((SELECT id FROM public.tags WHERE name = 'Java 8+'), 'Modern Java');

-- 퀴즈 생성
INSERT INTO public.quizzes (attempt_count, avg_score, created_at, description, difficulty_level, is_public, question_count, quiz_type, time_limit, title, updated_at, creator_id)
VALUES
    -- Java 퀴즈
    (0, 0.0, NOW(), 'Java 기초 개념과 문법에 대한 기업 면접용 퀴즈입니다. 객체지향 프로그래밍의 기본과 Java 기초 문법을 다룹니다.', 'BEGINNER', true, 10, 'REGULAR', 600, 'Java 면접 기초 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin')),
    (0, 0.0, NOW(), 'Java 중급 개념에 대한 기업 면접용 퀴즈입니다. Collections, Exception handling, 제네릭 등을 다룹니다.', 'INTERMEDIATE', true, 10, 'REGULAR', 900, 'Java 면접 중급 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin')),
    (0, 0.0, NOW(), 'Java 고급 개념에 대한 기업 면접용 퀴즈입니다. 멀티쓰레딩, 동시성, JVM 내부 구조 등 심화 개념을 다룹니다.', 'ADVANCED', true, 10, 'REGULAR', 1200, 'Java 면접 고급 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin')),

    -- Spring 퀴즈
    (0, 0.0, NOW(), 'Spring 프레임워크의 기초 개념에 대한 기업 면접용 퀴즈입니다. IoC, DI 등 Spring의 핵심 개념을 다룹니다.', 'BEGINNER', true, 10, 'REGULAR', 600, 'Spring 면접 기초 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin')),
    (0, 0.0, NOW(), 'Spring 프레임워크의 중급 개념에 대한 기업 면접용 퀴즈입니다. AOP, 트랜잭션 관리 등을 다룹니다.', 'INTERMEDIATE', true, 10, 'REGULAR', 900, 'Spring 면접 중급 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin')),
    (0, 0.0, NOW(), 'Spring 프레임워크의 고급 개념에 대한 기업 면접용 퀴즈입니다. Spring Security, 마이크로서비스 아키텍처 등 심화 개념을 다룹니다.', 'ADVANCED', true, 10, 'REGULAR', 1200, 'Spring 면접 고급 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin'));

-- 퀴즈-태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
VALUES
    -- Java 기초 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Core Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java OOP')),

    -- Java 중급 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java Collections')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java 8+')),

    -- Java 고급 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java Concurrency')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'JVM')),

    -- Spring 기초 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Core')),

    -- Spring 중급 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Boot')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring MVC')),

    -- Spring 고급 퀴즈 태그
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Data')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Security'));

-- Java 면접 기초 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'c', NOW(), 'BEGINNER', 'Java에서 객체지향 프로그래밍의 4가지 주요 원칙은 캡슐화, 상속, 다형성, 추상화입니다.',
     '[{"key": "a", "value": "캡슐화, 상속, 접근성, 가시성"}, {"key": "b", "value": "추상화, 상속, 오버로딩, 오버라이딩"}, {"key": "c", "value": "캡슐화, 상속, 다형성, 추상화"}, {"key": "d", "value": "추상화, 가상화, 다형성, 제네릭"}]',
     10, 'Java에서 객체지향 프로그래밍의 4가지 주요 원칙은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'b', NOW(), 'BEGINNER', 'JVM(Java Virtual Machine)은 Java 바이트 코드(.class 파일)을 실행하는 가상 머신으로, 플랫폼 독립성을 제공합니다.',
     '[{"key": "a", "value": "Java 코드를 C++로 변환하는 컴파일러"}, {"key": "b", "value": "Java 바이트 코드를 실행하는 가상 머신"}, {"key": "c", "value": "Java 개발 환경(IDE)"}, {"key": "d", "value": "Java 소스 코드를 관리하는 버전 관리 시스템"}]',
     10, 'JVM(Java Virtual Machine)의 주요 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    ('public class Main {\n    public static void main(String[] args) {\n        String str1 = \"Hello\";\n        String str2 = \"Hello\";\n        String str3 = new String(\"Hello\");\n        \n        System.out.println(str1 == str2);\n        System.out.println(str1 == str3);\n        System.out.println(str1.equals(str3));\n    }\n}',
     'c', NOW(), 'BEGINNER', 'str1과 str2는 문자열 리터럴로 String Pool에서 같은 객체를 참조합니다. str3는 new 키워드로 생성되어 Heap에 새로운 객체를 생성합니다. == 연산자는 참조를 비교하고, equals()는 내용을 비교합니다.',
     '[{"key": "a", "value": "false, false, false"}, {"key": "b", "value": "true, true, true"}, {"key": "c", "value": "true, false, true"}, {"key": "d", "value": "false, true, false"}]',
     10, '다음 코드의 출력 결과는 무엇인가요?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'a', NOW(), 'BEGINNER', '자바의 기본 데이터 타입은 boolean, byte, char, short, int, long, float, double 총 8가지입니다. String은 기본 타입이 아닌 클래스입니다.',
     '[{"key": "a", "value": "8개"}, {"key": "b", "value": "9개"}, {"key": "c", "value": "6개"}, {"key": "d", "value": "10개"}]',
     10, 'Java에서 기본 데이터 타입(Primitive Data Type)은 몇 개인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'd', NOW(), 'BEGINNER', 'final 키워드는 클래스, 메서드, 변수에 사용될 수 있으며 각각 다른 의미를 가집니다. 클래스에 사용될 경우 상속 불가, 메서드에 사용될 경우 오버라이딩 불가, 변수에 사용될 경우 값 변경 불가를 의미합니다.',
     '[{"key": "a", "value": "private과 동일한 접근 제어자"}, {"key": "b", "value": "항상 static과 함께 사용해야 하는 키워드"}, {"key": "c", "value": "예외 처리를 위한 키워드"}, {"key": "d", "value": "변수, 메서드, 클래스가 변경/확장될 수 없음을 나타내는 키워드"}]',
     10, 'Java에서 final 키워드의 의미는 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'b', NOW(), 'BEGINNER', 'public은 모든 클래스에서 접근 가능, protected는 같은 패키지와 하위 클래스에서 접근 가능, default(접근 제어자 없음)는 같은 패키지에서만 접근 가능, private은 같은 클래스 내에서만 접근 가능합니다.',
     '[{"key": "a", "value": "public, protected, default, constant"}, {"key": "b", "value": "public, protected, default, private"}, {"key": "c", "value": "global, protected, package, private"}, {"key": "d", "value": "public, secure, package, personal"}]',
     10, 'Java의 접근 제어자(Access Modifier)의 종류를 올바르게 나열한 것은?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    ('public class Parent {\n    void display() {\n        System.out.println(\"Parent\");\n    }\n}\n\npublic class Child extends Parent {\n    void display() {\n        System.out.println(\"Child\");\n    }\n    \n    public static void main(String[] args) {\n        Parent obj = new Child();\n        obj.display();\n    }\n}',
     'b', NOW(), 'BEGINNER', '이것은 다형성의 예입니다. 런타임에 호출되는 메서드는 참조 변수의 타입이 아닌 실제 객체의 타입에 의해 결정됩니다. 따라서 Child 클래스의 display() 메서드가 호출됩니다.',
     '[{"key": "a", "value": "Parent"}, {"key": "b", "value": "Child"}, {"key": "c", "value": "컴파일 에러"}, {"key": "d", "value": "런타임 에러"}]',
     10, '다음 코드의 출력 결과는 무엇인가요?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'a', NOW(), 'BEGINNER', '추상 클래스는 추상 메서드를 가질 수 있고, 인스턴스화할 수 없습니다. 인터페이스는 메서드 시그니처만 정의하고 모든 메서드가 기본적으로 public abstract입니다. Java 8부터 인터페이스는 default와 static 메서드를 가질 수 있습니다.',
     '[{"key": "a", "value": "추상 클래스는 일부 구현된 메서드를 가질 수 있지만, 인터페이스는 Java 8 이전에는 모든 메서드가 추상적"}, {"key": "b", "value": "추상 클래스는 다중 상속이 가능하지만, 인터페이스는 단일 구현만 가능"}, {"key": "c", "value": "추상 클래스는 상태를 가질 수 없지만, 인터페이스는 상태를 가질 수 있음"}, {"key": "d", "value": "추상 클래스는 private 메서드를 가질 수 없지만, 인터페이스는 가질 수 있음"}]',
     10, '추상 클래스(Abstract Class)와 인터페이스(Interface)의 차이점으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'c', NOW(), 'BEGINNER', 'Java는 객체를 가비지 컬렉터가 자동으로 메모리에서 해제합니다. 프로그래머가 명시적으로 메모리를 해제하지 않아도 됩니다.',
     '[{"key": "a", "value": "delete 연산자 사용"}, {"key": "b", "value": "free() 메서드 호출"}, {"key": "c", "value": "가비지 컬렉터가 자동으로 처리"}, {"key": "d", "value": "dispose() 메서드 호출"}]',
     10, 'Java에서 더 이상 사용하지 않는 객체의 메모리는 어떻게 해제되나요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈')),

    (NULL, 'd', NOW(), 'BEGINNER', '생성자는 클래스와 이름이 같아야 하고, 반환 타입이 없어야 하며, 오버로딩이 가능하고, 상속되지 않습니다.',
     '[{"key": "a", "value": "생성자는 반환 타입이 void여야 한다"}, {"key": "b", "value": "모든 클래스는 정확히 하나의 생성자만 가질 수 있다"}, {"key": "c", "value": "생성자는 반드시 public이어야 한다"}, {"key": "d", "value": "생성자는 상속되지 않는다"}]',
     10, 'Java 생성자(Constructor)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'));

-- Java 면접 중급 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'c', NOW(), 'INTERMEDIATE', 'Java의 컬렉션 프레임워크는 List, Set, Queue, Map 인터페이스를 제공합니다. Vector는 List 인터페이스의 구현체입니다.',
     '[{"key": "a", "value": "List, Array, Set, Map"}, {"key": "b", "value": "ArrayList, LinkedList, HashSet, HashMap"}, {"key": "c", "value": "List, Set, Queue, Map"}, {"key": "d", "value": "Collection, List, Set, Map"}]',
     15, 'Java 컬렉션 프레임워크의 주요 인터페이스는 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'equals()는 두 객체의 내용이 같은지 비교하고, hashCode()는 객체의 해시 코드 값을 반환합니다. equals()가 true를 반환하면 hashCode()도 같은 값을 반환해야 합니다.',
     '[{"key": "a", "value": "두 객체가 equal하다면 hashCode도 같아야 한다"}, {"key": "b", "value": "두 객체의 hashCode가 같다면 반드시 equals도 true여야 한다"}, {"key": "c", "value": "equals() 메서드는 hashCode()를 항상 내부적으로 호출한다"}, {"key": "d", "value": "hashCode()는 항상 객체의 메모리 주소를 반환한다"}]',
     15, 'Java에서 equals()와 hashCode() 메서드의 관계로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    ('import java.util.ArrayList;\nimport java.util.List;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<String> list = new ArrayList<>();\n        list.add(\"A\");\n        list.add(\"B\");\n        list.add(\"C\");\n        \n        for(String str : list) {\n            if(str.equals(\"B\")) {\n                list.remove(str);\n            }\n        }\n        \n        System.out.println(list);\n    }\n}',
     'd', NOW(), 'INTERMEDIATE', '리스트를 순회하면서 요소를 제거하면 ConcurrentModificationException이 발생합니다. Iterator를 사용하거나 removeIf() 메서드를 사용해야 합니다.',
     '[{"key": "a", "value": "[A, C]"}, {"key": "b", "value": "[A, B, C]"}, {"key": "c", "value": "[]"}, {"key": "d", "value": "ConcurrentModificationException 발생"}]',
     15, '다음 코드의 실행 결과는?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'b', NOW(), 'INTERMEDIATE', 'Stream API는 Java 8에서 추가된 기능으로, 컬렉션의 요소를 선언적으로 처리할 수 있게 해줍니다. 데이터 소스를 변경하지 않고, 병렬 처리를 지원하며, 중간 연산과 최종 연산으로 구분됩니다.',
     '[{"key": "a", "value": "I/O 스트림을 간소화하기 위한 API"}, {"key": "b", "value": "컬렉션 요소를 선언적으로 처리하기 위한 API"}, {"key": "c", "value": "멀티스레딩을 간소화하기 위한 API"}, {"key": "d", "value": "네트워크 통신을 위한 API"}]',
     15, 'Java 8에서 추가된 Stream API의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', '제네릭은 컴파일 타임에 타입 안전성을 제공하지만, 런타임에는 타입 정보가 소거(Type Erasure)됩니다. 이는 하위 호환성을 위한 설계 결정이었습니다.',
     '[{"key": "a", "value": "제네릭은 런타임에만 타입 검사를 수행한다"}, {"key": "b", "value": "제네릭은 원시 타입(primitive type)에 직접 사용할 수 있다"}, {"key": "c", "value": "제네릭은 런타임에 타입 정보가 소거된다(Type Erasure)"}, {"key": "d", "value": "제네릭은 메서드에는 적용할 수 없고 클래스에만 적용할 수 있다"}]',
     15, 'Java 제네릭(Generics)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'd', NOW(), 'INTERMEDIATE', '함수형 인터페이스는 정확히 하나의 추상 메서드를 가진 인터페이스입니다. Java 8에서는 @FunctionalInterface 어노테이션을 제공하여 컴파일러가 이를 검증하도록 합니다.',
     '[{"key": "a", "value": "default 메서드만 가지는 인터페이스"}, {"key": "b", "value": "static 메서드만 가지는 인터페이스"}, {"key": "c", "value": "추상 메서드를 가지지 않는 인터페이스"}, {"key": "d", "value": "정확히 하나의 추상 메서드를 가진 인터페이스"}]',
     15, 'Java 8에서 소개된 함수형 인터페이스(Functional Interface)의 정의는?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'checked exception은 컴파일러가 처리를 강제하고, unchecked exception은 런타임에 발생합니다. RuntimeException과 그 하위 클래스, Error와 그 하위 클래스가 unchecked exception에 해당합니다.',
     '[{"key": "a", "value": "Checked Exception은 컴파일 시점에 확인되고, Unchecked Exception은 런타임에 확인된다"}, {"key": "b", "value": "Checked Exception은 try-catch로 처리할 수 없고, Unchecked Exception은 처리할 수 있다"}, {"key": "c", "value": "Checked Exception은 Error 클래스의 하위 클래스이고, Unchecked Exception은 Exception 클래스의 하위 클래스이다"}, {"key": "d", "value": "Checked Exception은 항상 복구 가능하고, Unchecked Exception은 항상 복구 불가능하다"}]',
     15, 'Java에서 Checked Exception과 Unchecked Exception의 차이점으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    ('import java.util.Optional;\n\npublic class Main {\n    public static void main(String[] args) {\n        Optional<String> opt = Optional.ofNullable(null);\n        String result = opt.orElse(\"Default\");\n        System.out.println(result);\n    }\n}',
     'b', NOW(), 'INTERMEDIATE', 'Optional.ofNullable()은 값이 null일 수 있는 경우 사용합니다. orElse() 메서드는 Optional이 비어있을 때 기본값을 반환합니다.',
     '[{"key": "a", "value": "null"}, {"key": "b", "value": "Default"}, {"key": "c", "value": "NullPointerException 발생"}, {"key": "d", "value": "NoSuchElementException 발생"}]',
     15, '다음 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', 'wait(), notify(), notifyAll()은 Object 클래스의 메서드로 스레드 간 통신에 사용됩니다. 이 메서드들은 반드시 synchronized 블록 내에서 호출되어야 합니다.',
     '[{"key": "a", "value": "Thread 클래스의 메서드이다"}, {"key": "b", "value": "이 메서드들은 sleep() 메서드처럼 static 메서드이다"}, {"key": "c", "value": "synchronized 블록 내에서만 호출될 수 있다"}, {"key": "d", "value": "wait() 메서드는 스레드를 영원히 중지시킨다"}]',
     15, 'Java의 wait(), notify(), notifyAll() 메서드에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'Java 8에서 소개된 메서드 참조는 람다 표현식을 더 간결하게 표현하는 방법입니다. 메서드 참조의 타입으로는 정적 메서드 참조, 인스턴스 메서드 참조, 객체의 인스턴스 메서드 참조, 생성자 참조가 있습니다.',
     '[{"key": "a", "value": "ClassName::staticMethod, object::instanceMethod, ClassName::new"}, {"key": "b", "value": "ClassName->staticMethod, object->instanceMethod, ClassName->new"}, {"key": "c", "value": "ClassName.staticMethod(), object.instanceMethod(), new ClassName()"}, {"key": "d", "value": "@reference(ClassName.staticMethod), @reference(object.instanceMethod)"}]',
     15, 'Java 8의 메서드 참조(Method Reference) 문법으로 올바른 것은?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'));

-- Java 면접 고급 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'b', NOW(), 'ADVANCED', 'JVM 메모리 구조는 크게 Method Area, Heap, Stack, PC Registers, Native Method Stack으로 나뉩니다. Heap은 다시 Young Generation, Old Generation으로 나뉘고, Young Generation은 Eden, Survivor 영역으로 구성됩니다.',
     '[{"key": "a", "value": "Method Area, Heap, Stack, Registry, Cache"}, {"key": "b", "value": "Method Area, Heap, Stack, PC Registers, Native Method Stack"}, {"key": "c", "value": "Code Cache, Heap, Execution Stack, Registers, Native Area"}, {"key": "d", "value": "Class Area, Object Heap, Call Stack, Program Counter, System Registers"}]',
     20, 'JVM의 메모리 구조를 올바르게 나열한 것은?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'c', NOW(), 'ADVANCED', 'G1(Garbage First) 가비지 컬렉터는 큰 힙 메모리에서 짧은 GC 중지 시간을 달성하기 위해 설계되었습니다. 힙을 균등한 크기의 여러 영역(Region)으로 나누고, 가장 많은 가비지를 포함한 영역부터 수집합니다.',
     '[{"key": "a", "value": "자동으로 메모리 누수를 감지하고 수정하는 고급 가비지 컬렉터"}, {"key": "b", "value": "메모리를 할당하지만 절대 해제하지 않는 특수 목적 가비지 컬렉터"}, {"key": "c", "value": "힙을 여러 영역으로 나누고 가장 많은 가비지를 포함한 영역부터 수집하는 가비지 컬렉터"}, {"key": "d", "value": "C와 유사하게 프로그래머가 명시적으로 메모리 해제를 제어할 수 있게 하는 가비지 컬렉터"}]',
     20, 'G1(Garbage First) 가비지 컬렉터에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'd', NOW(), 'ADVANCED', 'Java의 ClassLoader는 계층적 구조로 되어있으며, Bootstrap ClassLoader, Extension ClassLoader, Application ClassLoader로 구성됩니다. 로딩, 링크, 초기화 단계를 거쳐 클래스를 메모리에 로드합니다.',
     '[{"key": "a", "value": "JVM이 종료될 때 클래스를 메모리에서 해제하는 프로세스"}, {"key": "b", "value": "컴파일 시점에 모든 클래스를 메모리에 미리 로드하는 최적화 기술"}, {"key": "c", "value": "클래스 파일의 크기를 줄여 메모리 사용량을 최적화하는 도구"}, {"key": "d", "value": "JVM이 클래스 파일을 읽어 메모리에 로드하는 프로세스"}]',
     20, 'Java의 ClassLoader에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    ('import java.util.concurrent.CompletableFuture;\n\npublic class Main {\n    public static void main(String[] args) throws Exception {\n        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {\n            try {\n                Thread.sleep(1000);\n            } catch (Exception e) {}\n            return \"Future1\";\n        });\n        \n        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {\n            try {\n                Thread.sleep(500);\n            } catch (Exception e) {}\n            return \"Future2\";\n        });\n        \n        CompletableFuture<String> result = CompletableFuture.anyOf(future1, future2)\n                                          .thenApply(o -> (String)o);\n        \n        System.out.println(result.get());\n    }\n}',
     'c', NOW(), 'ADVANCED', 'CompletableFuture.anyOf()는 주어진 CompletableFuture 중 가장 먼저 완료되는 것의 결과를 반환합니다. future2가 future1보다 더 빨리 완료되므로 "Future2"가 출력됩니다.',
     '[{"key": "a", "value": "Future1"}, {"key": "b", "value": "Future1Future2"}, {"key": "c", "value": "Future2"}, {"key": "d", "value": "실행 순서에 따라 달라진다"}]',
     20, '다음 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 50, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'b', NOW(), 'ADVANCED', '동시성 해시맵(ConcurrentHashMap)은 여러 스레드에서 안전하게 사용할 수 있는 Map 구현체입니다. Hashtable과 달리 세분화된 락을 사용하여 더 나은 성능을 제공하고, null 키나 값을 허용하지 않습니다.',
     '[{"key": "a", "value": "내부적으로 synchronized 키워드를 사용하여 모든 메서드를 동기화한다"}, {"key": "b", "value": "세분화된 락(lock)을 사용하여 동시성을 향상시킨다"}, {"key": "c", "value": "읽기 작업도 항상 락을 획득해야 한다"}, {"key": "d", "value": "put과 remove 작업 중에는 모든 읽기 작업이 차단된다"}]',
     20, 'Java의 ConcurrentHashMap에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'a', NOW(), 'ADVANCED', '디자인 패턴은 소프트웨어 설계에서 자주 발생하는 문제에 대한 재사용 가능한 해결책입니다. 생성 패턴은 객체 생성 메커니즘을 다루고, 구조 패턴은 객체 조합을 다루며, 행동 패턴은 객체 간 통신을 다룹니다.',
     '[{"key": "a", "value": "생성 패턴, 구조 패턴, 행동 패턴"}, {"key": "b", "value": "생성 패턴, 실행 패턴, 종료 패턴"}, {"key": "c", "value": "초기화 패턴, 구조 패턴, 연산 패턴"}, {"key": "d", "value": "싱글톤 패턴, 팩토리 패턴, 옵저버 패턴"}]',
     20, 'GoF(Gang of Four) 디자인 패턴의 주요 분류는?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'd', NOW(), 'ADVANCED', '리플렉션(Reflection)은 실행 중인 자바 프로그램이 자신의 구조를 검사하고 수정할 수 있게 해주는 기능입니다. Class 객체를 통해 클래스의 정보를 얻고, 생성자, 메서드, 필드에 접근할 수 있습니다. 성능 저하와 보안 위험이 단점입니다.',
     '[{"key": "a", "value": "컴파일 시간에 타입 안전성을 보장하는 기능"}, {"key": "b", "value": "소스 코드를 자동으로 최적화하는 기능"}, {"key": "c", "value": "메모리 누수를 자동으로 감지하는 기능"}, {"key": "d", "value": "런타임에 클래스, 메서드, 필드 정보에 접근하고 조작하는 기능"}]',
     20, 'Java 리플렉션(Reflection)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'c', NOW(), 'ADVANCED', 'ForkJoinPool은 Java 7에서 도입된 ExecutorService의 구현체로, work-stealing 알고리즘을 사용합니다. 작업을 여러 작은 작업으로 분할하고 각 작업이 완료되면 결과를 병합합니다.',
     '[{"key": "a", "value": "스레드 간 데이터 교환을 위한 특수 목적 컬렉션"}, {"key": "b", "value": "스레드 로컬 변수를 전역적으로 사용할 수 있게 해주는 풀"}, {"key": "c", "value": "work-stealing 알고리즘을 사용하여 병렬 처리를 지원하는 ExecutorService 구현체"}, {"key": "d", "value": "데이터베이스 연결을 관리하기 위한 커넥션 풀"}]',
     20, 'Java의 ForkJoinPool에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 45, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'b', NOW(), 'ADVANCED', '불변 객체는 생성 후 상태가 변경되지 않는 객체입니다. 모든 필드가 final이고, 적절한 방어적 복사를 사용해야 합니다. 스레드 안전하고 사이드 이펙트가 없어 병렬 프로그래밍에 유용합니다.',
     '[{"key": "a", "value": "불변 객체는 필드 값을 변경할 수 있지만 객체 참조는 변경할 수 없다"}, {"key": "b", "value": "불변 객체는 생성 후 그 상태가 절대 변하지 않는 객체다"}, {"key": "c", "value": "불변 객체는 항상 싱글톤 패턴으로 구현해야 한다"}, {"key": "d", "value": "불변 객체는 자동으로 스레드 풀을 사용하여 성능을 최적화한다"}]',
     20, 'Java에서 불변 객체(Immutable Object)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈')),

    (NULL, 'a', NOW(), 'ADVANCED', 'JIT(Just-In-Time) 컴파일러는 자바 바이트코드를 런타임에 네이티브 코드로 컴파일하여 성능을 향상시킵니다. 자주 실행되는 코드(핫 스팟)를 식별하고 최적화합니다.',
     '[{"key": "a", "value": "자주 사용되는 바이트코드를 런타임에 네이티브 코드로 컴파일하여 성능을 향상시킨다"}, {"key": "b", "value": "컴파일 시간에 모든 자바 코드를 미리 최적화한다"}, {"key": "c", "value": "소스 코드를 직접 기계어로 변환하여 인터프리터를 우회한다"}, {"key": "d", "value": "C/C++ 코드와 Java 코드 사이의 인터페이스를 제공한다"}]',
     20, 'JVM의 JIT(Just-In-Time) 컴파일러에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈'));

-- Spring 면접 기초 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'c', NOW(), 'BEGINNER', 'IoC(Inversion of Control)는 객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것을 의미합니다. 이는 객체 간의 결합도를 낮추고 코드의 재사용성을 높입니다.',
     '[{"key": "a", "value": "객체를 직접 생성하고 관리하는 디자인 패턴"}, {"key": "b", "value": "예외 처리를 자동화하는 Spring의 기능"}, {"key": "c", "value": "객체 생성과 의존성 관리를 개발자가 아닌 프레임워크가 담당하는 것"}, {"key": "d", "value": "데이터베이스 트랜잭션을 관리하는 기술"}]',
     10, 'Spring 프레임워크의 IoC(Inversion of Control)란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'b', NOW(), 'BEGINNER', 'DI(Dependency Injection)는 외부에서 객체를 생성하여 다른 객체에 주입하는 방식입니다. Spring에서는 생성자 주입, 필드 주입, 세터 주입 방식을 지원합니다.',
     '[{"key": "a", "value": "컴포넌트의 소스 코드를 수정하는 기법"}, {"key": "b", "value": "객체가 필요로 하는 의존성을 외부에서 주입해주는 기법"}, {"key": "c", "value": "스프링에서 제공하는 데이터베이스 연결 기술"}, {"key": "d", "value": "프로그램 실행 중 발생하는 오류를 주입하는 테스트 기법"}]',
     10, 'Spring의 DI(Dependency Injection)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'd', NOW(), 'BEGINNER', 'Bean은 Spring IoC 컨테이너에 의해 관리되는 객체입니다. XML 설정, 자바 설정, 컴포넌트 스캔 등의 방법으로 등록할 수 있습니다.',
     '[{"key": "a", "value": "Java의 데이터 전송 객체(DTO)"}, {"key": "b", "value": "데이터베이스 테이블과 매핑되는 객체"}, {"key": "c", "value": "Spring에서 제공하는 특별한 인터페이스"}, {"key": "d", "value": "Spring IoC 컨테이너에 의해 생성되고 관리되는 객체"}]',
     10, 'Spring에서 Bean이란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'a', NOW(), 'BEGINNER', '@Autowired는 Spring에서 의존성을 자동으로 주입하기 위한 어노테이션입니다. 필드, 생성자, 세터 메서드에 사용할 수 있습니다.',
     '[{"key": "a", "value": "Spring 컨테이너가 자동으로 의존성을 주입하도록 지시"}, {"key": "b", "value": "클래스를 자동으로 Bean으로 등록"}, {"key": "c", "value": "메서드를 RESTful API의 엔드포인트로 지정"}, {"key": "d", "value": "트랜잭션을 자동으로 관리하도록 지시"}]',
     10, '@Autowired 어노테이션의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'c', NOW(), 'BEGINNER', 'Spring Bean의 기본 스코프는 싱글톤입니다. 이는 Spring 컨테이너가 Bean을 하나만 생성하고 모든 요청에 같은 인스턴스를 반환한다는 의미입니다.',
     '[{"key": "a", "value": "prototype"}, {"key": "b", "value": "request"}, {"key": "c", "value": "singleton"}, {"key": "d", "value": "session"}]',
     10, 'Spring Bean의 기본 스코프(scope)는 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'b', NOW(), 'BEGINNER', 'Spring Boot는 Spring 애플리케이션을 쉽게 만들 수 있도록 도와주는 도구로, 자동 구성, 내장 서버, 스타터 의존성 등의 기능을 제공합니다.',
     '[{"key": "a", "value": "Spring의 다른 이름"}, {"key": "b", "value": "Spring 개발을 더 쉽게 만드는 도구로 자동 구성 등의 기능을 제공"}, {"key": "c", "value": "Spring 프로젝트를 부팅(시작)하는 명령어"}, {"key": "d", "value": "Spring에서 사용하는 부트스트랩 UI 프레임워크"}]',
     10, 'Spring Boot란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    ('import org.springframework.stereotype.Component;\n\n@Component\npublic class MyService {\n    public String getMessage() {\n        return "Hello from MyService";\n    }\n}',
     'a', NOW(), 'BEGINNER', '@Component 어노테이션은 클래스를 Spring Bean으로 등록하도록 지시합니다. 컴포넌트 스캔 기능이 활성화되어 있으면 Spring이 이 클래스를 자동으로 Bean으로 등록합니다.',
     '[{"key": "a", "value": "MyService 클래스를 Spring Bean으로 등록"}, {"key": "b", "value": "MyService 클래스를 싱글톤으로 만듦"}, {"key": "c", "value": "MyService 클래스를 추상 클래스로 선언"}, {"key": "d", "value": "MyService 클래스를 불변 객체로 만듦"}]',
     10, '다음 코드에서 @Component 어노테이션의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'd', NOW(), 'BEGINNER', 'Spring MVC는 Model-View-Controller 패턴을 구현한 웹 프레임워크입니다. DispatcherServlet이 중앙 서블릿 역할을 하여 요청을 적절한 컨트롤러로 라우팅합니다.',
     '[{"key": "a", "value": "데이터베이스 접근을 위한 Spring 프레임워크"}, {"key": "b", "value": "Spring에서 메시지를 처리하기 위한 시스템"}, {"key": "c", "value": "마이크로서비스 아키텍처를 구현하기 위한 프레임워크"}, {"key": "d", "value": "웹 애플리케이션 개발을 위한 MVC 패턴 구현 프레임워크"}]',
     10, 'Spring MVC란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'b', NOW(), 'BEGINNER', '@Controller는 Spring MVC의 컨트롤러를 표시하는 어노테이션으로, 주로 View를 반환합니다. @RestController는 @Controller와 @ResponseBody가 결합된 것으로, 데이터(주로 JSON)를 직접 반환합니다.',
     '[{"key": "a", "value": "@Controller는 RESTful 서비스를 위한 것이고, @RestController는 전통적인 웹 애플리케이션을 위한 것이다"}, {"key": "b", "value": "@Controller는 주로 View를 반환하고, @RestController는 데이터(JSON/XML)를 직접 반환한다"}, {"key": "c", "value": "@Controller는 싱글톤으로 관리되고, @RestController는 항상 새 인스턴스를 생성한다"}, {"key": "d", "value": "@Controller는 GET 요청만 처리할 수 있고, @RestController는 모든 HTTP 메서드를 처리할 수 있다"}]',
     10, 'Spring MVC에서 @Controller와 @RestController의 차이점은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈')),

    (NULL, 'c', NOW(), 'BEGINNER', 'application.properties와 application.yml은 Spring Boot 애플리케이션의 설정을 저장하는 파일입니다. 둘 다 같은 목적을 가지지만 형식이 다릅니다.',
     '[{"key": "a", "value": "Spring Boot 애플리케이션의 소스 코드를 저장하는 파일"}, {"key": "b", "value": "Spring Boot에서 자동으로 생성되는 로그 파일"}, {"key": "c", "value": "Spring Boot 애플리케이션의 설정을 저장하는 파일"}, {"key": "d", "value": "Spring Boot 애플리케이션의 배포 스크립트"}]',
     10, 'Spring Boot의 application.properties 또는 application.yml 파일의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈'));

-- Spring 면접 중급 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'b', NOW(), 'INTERMEDIATE', 'AOP(Aspect-Oriented Programming)는 관심사를 분리하여 모듈성을 증가시키는 프로그래밍 패러다임입니다. 로깅, 트랜잭션 관리 등 여러 모듈에서 공통으로 사용되는 기능을 분리할 수 있습니다.',
     '[{"key": "a", "value": "객체지향 프로그래밍의 다른 이름"}, {"key": "b", "value": "횡단 관심사를 분리하여 모듈성을 높이는 프로그래밍 패러다임"}, {"key": "c", "value": "Spring에서 데이터베이스를 관리하는 방법"}, {"key": "d", "value": "Spring Boot에서 자동 구성을 처리하는 방법"}]',
     15, 'Spring의 AOP(Aspect-Oriented Programming)란 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', '@Transactional 어노테이션은 메서드나 클래스에 트랜잭션 기능을 부여합니다. 이를 통해 메서드 실행 전에 트랜잭션을 시작하고, 메서드가 정상적으로 완료되면 커밋하며, 예외가 발생하면 롤백합니다.',
     '[{"key": "a", "value": "데이터베이스 연결을 관리"}, {"key": "b", "value": "메서드 실행 시간을 로깅"}, {"key": "c", "value": "메서드 실행을 트랜잭션으로 처리"}, {"key": "d", "value": "비동기 메서드 호출을 활성화"}]',
     15, 'Spring의 @Transactional 어노테이션의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'Spring Data JPA는 JPA(Java Persistence API)를 더 쉽게 사용할 수 있게 해주는 프로젝트입니다. Repository 인터페이스를 정의하면 구현체를 자동으로 생성해주고, 메서드 이름 규칙을 통해 쿼리를 자동으로 생성할 수 있습니다.',
     '[{"key": "a", "value": "JPA 기반 저장소를 쉽게 구현할 수 있게 해주는 Spring 프로젝트"}, {"key": "b", "value": "Spring에서 제공하는 자체 ORM 프레임워크"}, {"key": "c", "value": "JPA의 다른 이름"}, {"key": "d", "value": "JDBC를 직접 사용하여 데이터베이스에 접근하는 방법"}]',
     15, 'Spring Data JPA란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    ('import org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.context.annotation.Bean;\nimport org.springframework.context.annotation.Configuration;\n\n@Configuration\npublic class AppConfig {\n    \n    @Autowired\n    private DataSource dataSource;\n    \n    @Bean\n    public UserService userService() {\n        return new UserServiceImpl(userRepository());\n    }\n    \n    @Bean\n    public UserRepository userRepository() {\n        return new JdbcUserRepository(dataSource);\n    }\n}',
     'd', NOW(), 'INTERMEDIATE', '@Configuration 클래스는 Bean 정의의 소스로, 클래스 내의 @Bean 메서드가 Spring Bean을 생성하고 구성합니다. 자바 기반 설정 방식의 핵심 요소입니다.',
     '[{"key": "a", "value": "Spring 애플리케이션의 메인 클래스임을 표시"}, {"key": "b", "value": "HTTP 요청을 처리하는 컨트롤러임을 표시"}, {"key": "c", "value": "클래스가 데이터베이스 구성을 담당함을 표시"}, {"key": "d", "value": "Spring IoC 컨테이너의 Bean 정의 소스임을 표시"}]',
     15, '다음 코드에서 @Configuration 어노테이션의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'b', NOW(), 'INTERMEDIATE', 'Spring Boot Actuator는 운영 환경에서 애플리케이션을 모니터링하고 관리하기 위한 기능을 제공합니다. 헬스 체크, 메트릭, 로그 등 다양한 정보를 제공합니다.',
     '[{"key": "a", "value": "Spring Boot 애플리케이션을 자동으로 시작하는 도구"}, {"key": "b", "value": "운영 환경에서 애플리케이션을 모니터링하고 관리하기 위한 기능"}, {"key": "c", "value": "Spring Boot 애플리케이션을 빌드하고 배포하는 도구"}, {"key": "d", "value": "Spring Boot 애플리케이션의 보안을 강화하는 모듈"}]',
     15, 'Spring Boot Actuator의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', 'Spring Boot Starter는 의존성과 자동 구성을 포함하는 편리한 의존성 디스크립터입니다. 특정 기능(예: 웹, 데이터 액세스)을 쉽게 추가할 수 있습니다.',
     '[{"key": "a", "value": "Spring Boot 애플리케이션을 시작하는 메인 클래스"}, {"key": "b", "value": "Spring Boot 프로젝트를 생성하는 명령어"}, {"key": "c", "value": "특정 기능을 쉽게 추가할 수 있는 의존성 세트"}, {"key": "d", "value": "Spring Boot의 내장 웹 서버"}]',
     15, 'Spring Boot Starter란 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'Spring Bean 생명주기는 초기화와 소멸 단계를 포함합니다. @PostConstruct와 @PreDestroy 어노테이션을 사용하거나 InitializingBean, DisposableBean 인터페이스를 구현하여 이 단계에 로직을 추가할 수 있습니다.',
     '[{"key": "a", "value": "인스턴스화, 프로퍼티 설정, 초기화, 사용, 소멸"}, {"key": "b", "value": "컴파일, 로딩, 실행, 가비지 컬렉션"}, {"key": "c", "value": "설계, 개발, 테스트, 배포, 유지보수"}, {"key": "d", "value": "생성, 실행, 휴면, 재개, 중지"}]',
     15, 'Spring Bean의 라이프사이클 단계를 올바르게 나열한 것은?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'd', NOW(), 'INTERMEDIATE', 'Spring의 ApplicationContext는 BeanFactory의 확장으로, 더 많은 기능을 제공합니다. 국제화, 이벤트 발행, 리소스 로딩 등의 기능이 추가되어 있습니다.',
     '[{"key": "a", "value": "ApplicationContext는 경량 컨테이너이고, BeanFactory는 고급 기능을 제공하는 확장 컨테이너다"}, {"key": "b", "value": "ApplicationContext는 싱글톤 Bean만 관리하고, BeanFactory는 모든 스코프의 Bean을 관리한다"}, {"key": "c", "value": "ApplicationContext는 XML 설정만 지원하고, BeanFactory는 자바 설정도 지원한다"}, {"key": "d", "value": "ApplicationContext는 BeanFactory의 확장으로, 더 많은 기업 수준의 기능을 제공한다"}]',
     15, 'Spring의 ApplicationContext와 BeanFactory의 차이점은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'b', NOW(), 'INTERMEDIATE', 'Spring MVC의 DispatcherServlet은 프론트 컨트롤러 역할을 하는 중앙 서블릿입니다. 모든 요청을 받아 적절한 핸들러로 라우팅하고, 뷰 해석, 예외 처리 등을 담당합니다.',
     '[{"key": "a", "value": "JDBC 연결을 관리하는 서블릿"}, {"key": "b", "value": "모든 요청을 받아 적절한 컨트롤러로 라우팅하는 프론트 컨트롤러"}, {"key": "c", "value": "정적 자원을 처리하는 서블릿"}, {"key": "d", "value": "비동기 요청만 처리하는 특수 서블릿"}]',
     15, 'Spring MVC의 DispatcherServlet의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', '@SpringBootApplication은 @Configuration, @EnableAutoConfiguration, @ComponentScan을 결합한 편의 어노테이션입니다. Spring Boot 애플리케이션의 메인 클래스에 사용됩니다.',
     '[{"key": "a", "value": "@Component, @Controller, @Service를 결합"}, {"key": "b", "value": "@Bean, @Autowired, @Qualifier를 결합"}, {"key": "c", "value": "@Configuration, @EnableAutoConfiguration, @ComponentScan을 결합"}, {"key": "d", "value": "@Repository, @Entity, @Table을 결합"}]',
     15, 'Spring Boot의 @SpringBootApplication 어노테이션은 다음 중 어떤 어노테이션들을 결합한 것인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈'));

-- Spring 면접 고급 퀴즈 문제
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'c', NOW(), 'ADVANCED', 'Spring Security는 인증과 권한 부여를 위한 강력한 프레임워크입니다. 여러 인증 방식을 지원하고, 메서드 수준 보안, 웹 요청 보안 등 다양한 보안 기능을 제공합니다.',
     '[{"key": "a", "value": "SSL/TLS를 자동으로 구성하는 도구"}, {"key": "b", "value": "Spring 애플리케이션의 데이터를 암호화하는 도구"}, {"key": "c", "value": "인증, 권한 부여 및 보호 기능을 제공하는 보안 프레임워크"}, {"key": "d", "value": "취약점을 자동으로 탐지하고 수정하는 도구"}]',
     20, 'Spring Security의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'b', NOW(), 'ADVANCED', '스프링 클라우드(Spring Cloud)는 분산 시스템과 마이크로서비스 아키텍처를 구축하기 위한 도구를 제공합니다. 서비스 디스커버리, 구성 관리, 서킷 브레이커 등의 기능을 포함합니다.',
     '[{"key": "a", "value": "Spring 애플리케이션을 클라우드에 배포하는 도구"}, {"key": "b", "value": "분산 시스템과 마이크로서비스 개발을 위한 도구 모음"}, {"key": "c", "value": "클라우드 저장소에 데이터를 보관하는 라이브러리"}, {"key": "d", "value": "AWS, Azure, GCP와 같은 클라우드 서비스를 관리하는 인터페이스"}]',
     20, 'Spring Cloud란 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'd', NOW(), 'ADVANCED', '스프링 WebFlux는 리액티브 프로그래밍 모델을 지원하는 웹 프레임워크입니다. 적은 스레드로 높은 동시성을 처리할 수 있고, 논블로킹 I/O를 활용합니다.',
     '[{"key": "a", "value": "Spring MVC의 다른 이름"}, {"key": "b", "value": "웹소켓만 지원하는 특수 목적 프레임워크"}, {"key": "c", "value": "UI 컴포넌트와 애니메이션을 위한 프론트엔드 프레임워크"}, {"key": "d", "value": "리액티브 프로그래밍 모델을 사용하는 비동기 웹 프레임워크"}]',
     20, 'Spring WebFlux는 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'a', NOW(), 'ADVANCED', '스프링 배치(Spring Batch)는 대량의 데이터 처리를 위한 프레임워크입니다. 작업을 청크 단위로 처리하고, 재시작 기능, 트랜잭션 관리 등을 제공합니다.',
     '[{"key": "a", "value": "대용량 데이터 일괄 처리를 위한 프레임워크"}, {"key": "b", "value": "Spring 애플리케이션의 배포 자동화 도구"}, {"key": "c", "value": "여러 Spring 애플리케이션을 동시에 시작하는 도구"}, {"key": "d", "value": "Spring 컴포넌트를 일괄 등록하는 기능"}]',
     20, 'Spring Batch의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    ('import org.springframework.cache.annotation.Cacheable;\nimport org.springframework.stereotype.Service;\n\n@Service\npublic class UserService {\n    \n    @Cacheable(value = "users", key = "#id")\n    public User getUserById(Long id) {\n        // DB에서 사용자 조회 로직\n        return userRepository.findById(id).orElse(null);\n    }\n}',
     'c', NOW(), 'ADVANCED', '@Cacheable 어노테이션은 메서드의 결과를 캐시하는 데 사용됩니다. 같은 인자로 메서드가 다시 호출될 때 실제 메서드를 실행하지 않고 캐시된 결과를 반환합니다.',
     '[{"key": "a", "value": "메서드 실행 결과를 로그로 기록"}, {"key": "b", "value": "메서드를 비동기적으로 실행"}, {"key": "c", "value": "메서드 결과를 캐시하여 동일한 요청에 대해 메서드 실행을 건너뜀"}, {"key": "d", "value": "메서드 실행 전후에 특정 작업을 수행"}]',
     20, '다음 코드에서 @Cacheable 어노테이션의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'b', NOW(), 'ADVANCED', '스프링 프로파일(Spring Profile)은 다양한 환경(개발, 테스트, 프로덕션)에 맞게 빈을 구성할 수 있는 기능입니다. @Profile 어노테이션이나 application.properties의 spring.profiles.active 속성을 통해 활성화할 수 있습니다.',
     '[{"key": "a", "value": "애플리케이션 성능 프로파일링을 위한 도구"}, {"key": "b", "value": "다양한 환경(개발, 테스트, 프로덕션)에 맞게 빈을 구성하기 위한 메커니즘"}, {"key": "c", "value": "사용자 프로필 정보를 관리하기 위한 시스템"}, {"key": "d", "value": "JVM 메모리 사용량을 프로파일링하는 기능"}]',
     20, 'Spring Profile의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'd', NOW(), 'ADVANCED', 'Spring의 @Async 어노테이션은 메서드를 비동기적으로 실행하도록 지시합니다. 메서드 호출이 즉시 반환되고 실제 실행은 별도의 스레드에서 이루어집니다.',
     '[{"key": "a", "value": "메서드를 동기적으로 실행하도록 강제"}, {"key": "b", "value": "메서드 실행을 지연시키는 어노테이션"}, {"key": "c", "value": "메서드의 실행 결과를 캐시"}, {"key": "d", "value": "메서드를 별도의 스레드에서 비동기적으로 실행"}]',
     20, 'Spring의 @Async 어노테이션의 역할은 무엇인가요?', 'MULTIPLE_CHOICE', 30, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'a', NOW(), 'ADVANCED', 'Spring Event는 컴포넌트 간 결합도를 낮추기 위한 메커니즘입니다. ApplicationEventPublisher를 사용하여 이벤트를 발행하고, @EventListener 어노테이션을 사용하여 이벤트를 수신할 수 있습니다.',
     '[{"key": "a", "value": "컴포넌트 간 결합도를 낮추기 위한 발행-구독 메커니즘"}, {"key": "b", "value": "사용자 인터페이스의 이벤트(클릭, 키 입력 등)를 처리하기 위한 시스템"}, {"key": "c", "value": "일정 시간마다 주기적으로 실행되는 작업을 위한 시스템"}, {"key": "d", "value": "외부 시스템과의 통합을 위한 메시징 시스템"}]',
     20, 'Spring Event의 주요 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'c', NOW(), 'ADVANCED', 'Spring Boot Auto-Configuration은 classpath에 있는 jar 의존성, 이미 정의된 빈, 환경 변수 등을 기반으로 애플리케이션을 자동으로 구성하는 메커니즘입니다.',
     '[{"key": "a", "value": "애플리케이션 시작 시간을 최적화하는 기능"}, {"key": "b", "value": "사용자 인터페이스를 자동으로 생성하는 기능"}, {"key": "c", "value": "classpath, 기존 빈, 환경 변수 등을 기반으로 애플리케이션을 자동으로 구성하는 메커니즘"}, {"key": "d", "value": "데이터베이스 스키마를 자동으로 생성하고 관리하는 기능"}]',
     20, 'Spring Boot의 Auto-Configuration이란 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈')),

    (NULL, 'b', NOW(), 'ADVANCED', 'Circuit Breaker 패턴은 분산 시스템에서 장애 전파를 방지하기 위한 패턴입니다. Spring Cloud Circuit Breaker는 이 패턴을 구현하는 API를 제공하며, Netflix Hystrix, Resilience4j 등을 지원합니다.',
     '[{"key": "a", "value": "애플리케이션 내의 무한 루프를 감지하고 중단하는 패턴"}, {"key": "b", "value": "외부 서비스 호출 실패 시 장애 전파를 방지하는 패턴"}, {"key": "c", "value": "데이터베이스 연결 풀을 관리하는 패턴"}, {"key": "d", "value": "병렬 처리 시 교착 상태를 방지하는 패턴"}]',
     20, 'Spring Cloud에서 사용되는 Circuit Breaker 패턴의 목적은 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(), (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈'));

-- 퀴즈 시도 더미 데이터 생성
INSERT INTO public.quiz_attempts (created_at, end_time, is_completed, score, start_time, time_taken, quiz_id, user_id)
VALUES
    (NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '25 minutes', true, 85, NOW() - INTERVAL '10 days', 1500,
     (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '40 minutes', true, 70, NOW() - INTERVAL '8 days', 2400,
     (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '15 minutes', true, 90, NOW() - INTERVAL '5 days', 900,
     (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'SpringMaster')),

    (NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '30 minutes', true, 75, NOW() - INTERVAL '3 days', 1800,
     (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'SpringMaster')),

    (NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '45 minutes', true, 60, NOW() - INTERVAL '1 day', 2700,
     (SELECT id FROM public.quizzes WHERE title = 'Java 면접 고급 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (NOW() - INTERVAL '4 hours', NOW() - INTERVAL '3 hours', true, 65, NOW() - INTERVAL '4 hours', 3600,
     (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 고급 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'SpringMaster'));

-- 퀴즈 문제 시도 더미 데이터 생성 (첫 번째 시도에 대해서만)
INSERT INTO public.question_attempts (created_at, is_correct, time_taken, user_answer, question_id, quiz_attempt_id)
VALUES
    -- Java 면접 기초 퀴즈 첫 번째 시도의 문제 시도
    (NOW() - INTERVAL '10 days' + INTERVAL '2 minutes', true, 20, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') LIMIT 1 OFFSET 0),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '10 days' + INTERVAL '4 minutes', true, 25, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') LIMIT 1 OFFSET 1),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '10 days' + INTERVAL '7 minutes', false, 35, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') LIMIT 1 OFFSET 2),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner')));

-- 퀴즈 리뷰 더미 데이터 생성
INSERT INTO public.quiz_reviews (content, created_at, rating, quiz_id, reviewer_id)
VALUES
    ('Java 기초 개념에 대한 좋은 퀴즈입니다. 면접 준비에 많은 도움이 되었습니다.',
     NOW() - INTERVAL '9 days', 4,
     (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    ('Spring의 핵심 개념을 잘 정리한 퀴즈입니다. 특히 Spring의 DI와 IoC에 대한 문제들이 유익했습니다.',
     NOW() - INTERVAL '4 days', 5,
     (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'SpringMaster')),

    ('Java 중급 퀴즈 난이도가 적절하며, Collection과 제네릭에 관한 문제들이 특히 도움이 되었습니다.',
     NOW() - INTERVAL '7 days', 4,
     (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈'),
     (SELECT id FROM public.users WHERE username = 'JavaLearner'));

-- 퀴즈 리뷰 댓글 더미 데이터 생성
INSERT INTO public.quiz_review_comments (content, created_at, commenter_id, parent_review_id)
VALUES
    ('감사합니다! 계속해서 좋은 퀴즈를 제공하도록 노력하겠습니다.',
     NOW() - INTERVAL '8 days',
     (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin'),
     (SELECT id FROM public.quiz_reviews WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 기초 퀴즈'))),

    ('더 많은 고급 주제를 다루는 퀴즈도 기대하겠습니다!',
     NOW() - INTERVAL '6 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner'),
     (SELECT id FROM public.quiz_reviews WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈')));

-- 사용자 레벨 더미 데이터 생성
INSERT INTO public.user_levels (created_at, current_exp, level, required_exp, updated_at, user_id)
VALUES
    (NOW() - INTERVAL '30 days', 500, 5, 1000, NOW(),
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (NOW() - INTERVAL '20 days', 300, 3, 500, NOW(),
     (SELECT id FROM public.users WHERE username = 'SpringMaster'));

-- 사용자 배틀 통계 더미 데이터 생성
INSERT INTO public.user_battle_stats (created_at, current_streak, highest_score, highest_streak, total_battles, total_correct_answers, total_questions, total_score, updated_at, wins, user_id)
VALUES
    (NOW() - INTERVAL '30 days', 2, 120, 3, 10, 35, 50, 450, NOW(), 6,
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (NOW() - INTERVAL '20 days', 1, 100, 2, 8, 28, 40, 350, NOW(), 5,
     (SELECT id FROM public.users WHERE username = 'SpringMaster'));

-- 사용자 업적 더미 데이터 생성
INSERT INTO public.user_achievements (user_level_id, achievements)
VALUES
    ((SELECT id FROM public.user_levels WHERE user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner')), 'FIRST_QUIZ_COMPLETED'),
    ((SELECT id FROM public.user_levels WHERE user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner')), 'PERFECT_SCORE'),
    ((SELECT id FROM public.user_levels WHERE user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster')), 'FIRST_QUIZ_COMPLETED'),
    ((SELECT id FROM public.user_levels WHERE user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster')), 'QUICK_SOLVER');

-- 사용자 업적 이력 더미 데이터 생성
INSERT INTO public.user_achievement_history (achievement, achievement_name, earned_at, user_id)
VALUES
    ('FIRST_QUIZ_COMPLETED', '첫 번째 퀴즈 완료', NOW() - INTERVAL '25 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    ('PERFECT_SCORE', '만점 달성', NOW() - INTERVAL '15 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    ('FIRST_QUIZ_COMPLETED', '첫 번째 퀴즈 완료', NOW() - INTERVAL '18 days',
     (SELECT id FROM public.users WHERE username = 'SpringMaster')),

    ('QUICK_SOLVER', '빠른 해결사', NOW() - INTERVAL '10 days',
     (SELECT id FROM public.users WHERE username = 'SpringMaster'));

-- 사용자 레벨 이력 더미 데이터 생성
INSERT INTO public.user_level_history (level, previous_level, updated_at, user_id)
VALUES
    (2, 1, NOW() - INTERVAL '28 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (3, 2, NOW() - INTERVAL '22 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (4, 3, NOW() - INTERVAL '15 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (5, 4, NOW() - INTERVAL '7 days',
     (SELECT id FROM public.users WHERE username = 'JavaLearner')),

    (2, 1, NOW() - INTERVAL '18 days',
     (SELECT id FROM public.users WHERE username = 'SpringMaster')),

    (3, 2, NOW() - INTERVAL '10 days',
     (SELECT id FROM public.users WHERE username = 'SpringMaster'));

-- 추가 문제 시도 데이터 (Spring 면접 기초 퀴즈에 대한 데이터)
INSERT INTO public.question_attempts (created_at, is_correct, time_taken, user_answer, question_id, quiz_attempt_id)
VALUES
    -- Spring 면접 기초 퀴즈 첫 번째 시도의 문제 시도 (SpringMaster 사용자)
    (NOW() - INTERVAL '5 days' + INTERVAL '1 minutes', true, 15, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 0),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '2 minutes', true, 18, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 1),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '3 minutes', true, 20, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 2),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '4 minutes', true, 22, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 3),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '5 minutes', true, 17, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 4),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '6 minutes', false, 25, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 5),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '7 minutes', true, 19, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 6),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '9 minutes', true, 21, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 7),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '11 minutes', true, 16, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 8),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '5 days' + INTERVAL '13 minutes', true, 18, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') LIMIT 1 OFFSET 9),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 기초 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster')));

-- Spring 면접 중급 퀴즈에 대한 문제 시도 데이터 (SpringMaster 사용자)
INSERT INTO public.question_attempts (created_at, is_correct, time_taken, user_answer, question_id, quiz_attempt_id)
VALUES
    (NOW() - INTERVAL '3 days' + INTERVAL '2 minutes', true, 25, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 0),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '5 minutes', true, 28, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 1),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '8 minutes', false, 32, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 2),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '12 minutes', true, 30, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 3),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '16 minutes', true, 27, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 4),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '19 minutes', true, 26, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 5),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '22 minutes', false, 35, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 6),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '25 minutes', true, 31, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 7),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '28 minutes', false, 33, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 8),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster'))),

    (NOW() - INTERVAL '3 days' + INTERVAL '30 minutes', true, 29, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') LIMIT 1 OFFSET 9),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Spring 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'SpringMaster')));

-- Java 면접 중급 퀴즈에 대한 문제 시도 데이터 (JavaLearner 사용자)
INSERT INTO public.question_attempts (created_at, is_correct, time_taken, user_answer, question_id, quiz_attempt_id)
VALUES
    (NOW() - INTERVAL '8 days' + INTERVAL '3 minutes', true, 30, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 0),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '7 minutes', true, 35, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 1),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '12 minutes', false, 40, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 2),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '17 minutes', true, 32, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 3),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '21 minutes', true, 28, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 4),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '25 minutes', false, 38, 'c',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 5),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '29 minutes', true, 33, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 6),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '33 minutes', true, 30, 'b',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 7),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '36 minutes', false, 36, 'd',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 8),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner'))),

    (NOW() - INTERVAL '8 days' + INTERVAL '40 minutes', true, 28, 'a',
     (SELECT id FROM public.questions WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') LIMIT 1 OFFSET 9),
     (SELECT id FROM public.quiz_attempts WHERE quiz_id = (SELECT id FROM public.quizzes WHERE title = 'Java 면접 중급 퀴즈') AND user_id = (SELECT id FROM public.users WHERE username = 'JavaLearner')));

-- 추가 퀴즈 시도 및, Java 원격 면접 퀴즈 생성
INSERT INTO public.quizzes (attempt_count, avg_score, created_at, description, difficulty_level, is_public, question_count, quiz_type, time_limit, title, updated_at, creator_id)
VALUES
    (0, 0.0, NOW(), 'Java 원격 면접에서 자주 물어보는 문제들을 모았습니다. 기본적인 자바 기술뿐만 아니라 코드 공유 도구와 원격 협업 방식에 익숙해질 수 있는 문제들이 포함되어 있습니다.', 'INTERMEDIATE', true, 10, 'REGULAR', 1200, 'Java 원격 면접 대비 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin'));

-- Java 원격 면접 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
VALUES
    ((SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Core Java')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java 8+')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java Collections')),
    ((SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Java OOP'));

-- Java 원격 면접 대비 퀴즈에 대한 문제 생성
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    ('import java.util.concurrent.CompletableFuture;\n\npublic class Main {\n    public static void main(String[] args) {\n        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {\n            // 시간이 걸리는 작업 수행\n            try {\n                Thread.sleep(1000);\n            } catch (Exception e) {\n                e.printStackTrace();\n            }\n            return "Hello";\n        }).thenApply(s -> s + " World");\n        \n        // future 결과 가져오기\n        System.out.println(future.join());\n    }\n}',
     'b', NOW(), 'INTERMEDIATE', 'CompletableFuture는 Java 8에서 도입된 비동기 프로그래밍을 위한 클래스입니다. supplyAsync()는 비동기 작업을 시작하고, thenApply()는 이전 작업의 결과를 변환합니다. join()은 결과를 동기적으로 가져옵니다.',
     '[{"key": "a", "value": "Hello"}, {"key": "b", "value": "Hello World"}, {"key": "c", "value": "World"}, {"key": "d", "value": "코드가 컴파일되지 않음"}]',
     15, '다음 Java 8 CompletableFuture 코드의 실행 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    ('import java.util.List;\nimport java.util.stream.Collectors;\nimport java.util.Arrays;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<String> names = Arrays.asList("John", "Jane", "Adam", "Tom", "Alice");\n        \n        String result = names.stream()\n            .filter(name -> name.startsWith("J"))\n            .map(String::toUpperCase)\n            .collect(Collectors.joining(", "));\n            \n        System.out.println(result);\n    }\n}',
     'c', NOW(), 'INTERMEDIATE', 'Stream API를 사용하여 이름 목록에서 "J"로 시작하는 이름을 필터링하고, 대문자로 변환한 다음, 쉼표로 구분된 하나의 문자열로 결합합니다.',
     '[{"key": "a", "value": "John, Jane, Adam, Tom, Alice"}, {"key": "b", "value": "JOHN, JANE, ADAM, TOM, ALICE"}, {"key": "c", "value": "JOHN, JANE"}, {"key": "d", "value": "john, jane"}]',
     15, '다음 Java 8 Stream API 코드의 실행 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    (NULL, 'a', NOW(), 'INTERMEDIATE', 'Java에서 추상 클래스와 인터페이스는 다른 목적을 가집니다. 추상 클래스는 IS-A 관계를 표현하고, 일부 구현을 포함할 수 있으며, 생성자를 가질 수 있습니다. 인터페이스는 HAS-A 관계를 표현하고, 다중 상속을 지원하며, 모든 메서드가 기본적으로 public abstract입니다.',
     '[{"key": "a", "value": "하나의 클래스가 여러 인터페이스를 구현할 수 있지만, 하나의 추상 클래스만 상속할 수 있다"}, {"key": "b", "value": "추상 클래스는 생성자를 가질 수 없지만, 인터페이스는 생성자를 가질 수 있다"}, {"key": "c", "value": "인터페이스에서는 필드를 선언할 수 없지만, Java 8부터는 메서드를 구현할 수 있다"}, {"key": "d", "value": "추상 클래스의 모든 메서드는 추상적이어야 하지만, 인터페이스는 일부 구현을 포함할 수 있다"}]',
     15, '원격 면접에서 자주 물어보는 질문: Java에서 추상 클래스와 인터페이스의 차이점 중 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    (NULL, 'd', NOW(), 'INTERMEDIATE', 'Java의 HashMap은 내부적으로 해시 테이블을 사용하여 키-값 쌍을 저장합니다. 키의 hashCode()를 사용하여 버킷을 결정하고, 같은 버킷에 여러 항목이 있는 경우 equals()로 비교합니다. Java 8부터는 버킷 내 항목이 많아지면 링크드 리스트 대신 트리를 사용합니다.',
     '[{"key": "a", "value": "HashMap은 키의 삽입 순서를 보장한다"}, {"key": "b", "value": "HashMap의 모든 메서드는 동기화되어 있어 스레드 안전하다"}, {"key": "c", "value": "HashMap은 키로 null을 허용하지 않는다"}, {"key": "d", "value": "Java 8부터 HashMap은 많은 충돌이 있는 경우 링크드 리스트 대신 트리를 사용한다"}]',
     15, '원격 면접에서 자주 물어보는 질문: Java의 HashMap에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    ('public class Example {\n    public static void main(String[] args) {\n        String s1 = "Java";\n        String s2 = new String("Java");\n        String s3 = s2.intern();\n        \n        System.out.println(s1 == s2);\n        System.out.println(s1 == s3);\n        System.out.println(s2 == s3);\n    }\n}',
     'b', NOW(), 'INTERMEDIATE', 'intern() 메서드는 문자열을 문자열 풀에 넣고, 풀에 이미 있는 경우 그 참조를 반환합니다. s1은 리터럴로 풀에 있고, s2는 새 객체, s3는 s2를 intern()하여 풀의 참조를 가집니다.',
     '[{"key": "a", "value": "false, false, false"}, {"key": "b", "value": "false, true, false"}, {"key": "c", "value": "false, false, true"}, {"key": "d", "value": "true, true, true"}]',
     15, '원격 면접에서 자주 물어보는 질문: 다음 String.intern() 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    (NULL, 'c', NOW(), 'INTERMEDIATE', '자바의 Garbage Collection은 더 이상 참조되지 않는 객체의 메모리를 회수하는 프로세스입니다. Mark-and-Sweep 알고리즘을 기반으로 하며, 일반적으로 Young Generation과 Old Generation으로 나누어 관리합니다.',
     '[{"key": "a", "value": "Garbage Collection은 항상 프로그래머가 System.gc()를 호출할 때만 실행된다"}, {"key": "b", "value": "Garbage Collection은 메모리 누수를 완전히 방지한다"}, {"key": "c", "value": "Garbage Collection은 일반적으로 Young Generation과 Old Generation으로 나누어 관리된다"}, {"key": "d", "value": "Garbage Collection은 C++의 소멸자와 동일한 역할을 한다"}]',
     15, '원격 면접에서 자주 물어보는 질문: Java Garbage Collection에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    ('import java.util.Optional;\n\npublic class Main {\n    public static void main(String[] args) {\n        Optional<String> empty = Optional.empty();\n        Optional<String> value = Optional.of("Hello");\n        \n        String result1 = empty.orElse("World");\n        String result2 = value.orElse("World");\n        \n        System.out.println(result1 + " " + result2);\n    }\n}',
     'a', NOW(), 'INTERMEDIATE', 'Optional.empty()는 빈 Optional을 반환하고, Optional.of()는 값을 포함하는 Optional을 반환합니다. orElse()는 Optional이 비어있을 때 대체 값을 반환합니다.',
     '[{"key": "a", "value": "World Hello"}, {"key": "b", "value": "Hello World"}, {"key": "c", "value": "null Hello"}, {"key": "d", "value": "World World"}]',
     15, '원격 면접에서 자주 물어보는 질문: 다음 Optional API 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    ('class Resource implements AutoCloseable {\n    public Resource() {\n        System.out.print("Create ");\n    }\n    \n    public void use() {\n        System.out.print("Use ");\n        throw new RuntimeException("Exception in use");\n    }\n    \n    @Override\n    public void close() {\n        System.out.print("Close ");\n    }\n}\n\npublic class Main {\n    public static void main(String[] args) {\n        try (Resource r = new Resource()) {\n            r.use();\n        } catch (Exception e) {\n            System.out.print("Catch ");\n        }\n    }\n}',
     'c', NOW(), 'INTERMEDIATE', 'try-with-resources 구문은 AutoCloseable 인터페이스를 구현한 객체를 자동으로 닫습니다. 예외가 발생해도 close()가 호출됩니다.',
     '[{"key": "a", "value": "Create Use Catch"}, {"key": "b", "value": "Create Use"}, {"key": "c", "value": "Create Use Close Catch"}, {"key": "d", "value": "Create Use Catch Close"}]',
     15, '원격 면접에서 자주 물어보는 질문: 다음 try-with-resources 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    (NULL, 'b', NOW(), 'INTERMEDIATE', '함수형 인터페이스는 단 하나의 추상 메서드만 가진 인터페이스로, 람다 표현식이나 메서드 참조를 사용할 수 있게 해줍니다. java.util.function 패키지에는 Consumer, Function, Predicate, Supplier 등 다양한 함수형 인터페이스가 있습니다.',
     '[{"key": "a", "value": "함수형 인터페이스는 static 메서드를 가질 수 없다"}, {"key": "b", "value": "java.util.function 패키지에는 Consumer, Function, Predicate, Supplier 등 다양한 함수형 인터페이스가 있다"}, {"key": "c", "value": "함수형 인터페이스는 람다 표현식으로만 구현할 수 있고, 익명 클래스로는 구현할 수 없다"}, {"key": "d", "value": "함수형 인터페이스는 여러 개의 추상 메서드를 가질 수 있다"}]',
     15, '원격 면접에서 자주 물어보는 질문: Java 8의 함수형 인터페이스(Functional Interface)에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈')),

    ('import java.util.ArrayList;\nimport java.util.List;\n\npublic class Main {\n    public static void main(String[] args) {\n        List<Integer> list = new ArrayList<>();\n        list.add(1);\n        list.add(2);\n        list.add(3);\n        \n        int sum = list.stream()\n                .filter(n -> n % 2 == 1)\n                .mapToInt(n -> n * n)\n                .sum();\n                \n        System.out.println(sum);\n    }\n}',
     'd', NOW(), 'INTERMEDIATE', '홀수만 필터링하고(1, 3), 각 값을 제곱한 다음(1, 9), 합계를 구합니다(10).',
     '[{"key": "a", "value": "6"}, {"key": "b", "value": "14"}, {"key": "c", "value": "1"}, {"key": "d", "value": "10"}]',
     15, '원격 면접에서 자주 물어보는 질문: 다음 Stream API 코드의 출력 결과는?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Java 원격 면접 대비 퀴즈'));

-- 추가 데이터: Spring 애플리케이션 성능 최적화 퀴즈
INSERT INTO public.quizzes (attempt_count, avg_score, created_at, description, difficulty_level, is_public, question_count, quiz_type, time_limit, title, updated_at, creator_id)
VALUES
    (0, 0.0, NOW(), 'Spring 애플리케이션의 성능 최적화 기법에 관한 퀴즈입니다. 실제 기업에서 자주 발생하는 성능 문제와 그 해결책에 대한 지식을 테스트합니다.', 'ADVANCED', true, 10, 'REGULAR', 1500, 'Spring 애플리케이션 성능 최적화 퀴즈', NOW(), (SELECT id FROM public.users WHERE username = 'JavaQuizAdmin'));

-- Spring 애플리케이션 성능 최적화 퀴즈와 태그 연결
INSERT INTO public.quiz_tags (quiz_id, tag_id)
VALUES
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Core')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Boot')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring Data')),
    ((SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'), (SELECT id FROM public.tags WHERE name = 'Spring MVC'));

-- Spring 애플리케이션 성능 최적화 퀴즈에 대한 문제 생성 (3개만 예시로 추가)
INSERT INTO public.questions (code_snippet, correct_answer, created_at, difficulty_level, explanation, options, points, question_text, question_type, time_limit_seconds, updated_at, quiz_id)
VALUES
    (NULL, 'b', NOW(), 'ADVANCED', 'N+1 문제는 ORM을 사용할 때 자주 발생하는 성능 이슈로, 부모 엔티티를 조회한 후 연관된 자식 엔티티를 각각 추가 쿼리로 조회하는 상황을 말합니다. 이로 인해 데이터베이스 쿼리가 N+1개 발생하여 성능이 저하됩니다.',
     '[{"key": "a", "value": "데이터베이스 커넥션 풀이 N+1개 생성되는 문제"}, {"key": "b", "value": "부모 엔티티 조회 후 연관된 자식 엔티티를 각각 추가 쿼리로 조회하여 총 N+1개의 쿼리가 발생하는 문제"}, {"key": "c", "value": "트랜잭션이 N번 롤백된 후 1번 커밋되는 문제"}, {"key": "d", "value": "N개의 스레드가 1개의 리소스를 경쟁적으로 사용하는 문제"}]',
     20, 'Spring Data JPA에서 자주 발생하는 N+1 문제란 무엇인가요?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈')),

    (NULL, 'c', NOW(), 'ADVANCED', 'JPA의 fetch join은 JPQL의 JOIN FETCH 구문을 사용하여 연관 엔티티를 함께 조회하는 방법입니다. 이를 통해 N+1 문제를 해결할 수 있습니다.',
     '[{"key": "a", "value": "페이징 처리와 항상 함께 사용해야 한다"}, {"key": "b", "value": "중첩된 여러 컬렉션에 대해 동시에 적용할 수 있다"}, {"key": "c", "value": "엔티티 그래프를 즉시 로딩하여 N+1 문제를 해결하는 데 도움이 된다"}, {"key": "d", "value": "데이터베이스 인덱스를 자동으로 생성한다"}]',
     20, 'JPA의 fetch join에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 40, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈')),

    ('public class OrderController {\n    private final OrderService orderService;\n    \n    @Cacheable(value = "orders", key = "#id")\n    @GetMapping("/orders/{id}")\n    public Order getOrderById(@PathVariable Long id) {\n        return orderService.findById(id);\n    }\n}',
     'a', NOW(), 'ADVANCED', 'Spring의 @Cacheable 어노테이션은 메서드의 결과를 캐시하여 동일한 인자로 메서드가 다시 호출될 때 캐시된 결과를 반환합니다. 이를 통해 데이터베이스 쿼리나 비용이 많이 드는 연산을 줄일 수 있습니다.',
     '[{"key": "a", "value": "메서드 결과를 캐시하여 동일한 주문 ID로 요청이 오면 캐시된 결과를 반환하여 성능을 향상시킨다"}, {"key": "b", "value": "주문 ID를 캐시하고 실제 주문은 항상 데이터베이스에서 가져온다"}, {"key": "c", "value": "클라이언트가 캐시된 데이터만 요청하도록 강제한다"}, {"key": "d", "value": "컨트롤러의 모든 메서드 실행을 캐시한다"}]',
     20, '위 Spring 코드에서 @Cacheable 어노테이션의 역할로 올바른 것은?', 'MULTIPLE_CHOICE', 45, NOW(),
     (SELECT id FROM public.quizzes WHERE title = 'Spring 애플리케이션 성능 최적화 퀴즈'));


-- 기존 퀴즈의 view_count를 업데이트하는 쿼리
UPDATE public.quizzes SET view_count = 0 WHERE view_count IS NULL;