-- 새로운 더미 데이터 추가 (소프트웨어 공학 및 설계 원칙)

-- 1. 필요한 태그 추가 (소프트웨어 설계 원칙)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '소프트웨어 설계 원칙', 'SOLID, 디자인 패턴, 클린 코드, 테스트 등 좋은 소프트웨어 설계를 위한 원칙과 기법')
ON CONFLICT (name) DO NOTHING; -- 이미 존재하면 추가하지 않음

-- 2. 새로운 퀴즈 추가 (설계 원칙 관련 3개 퀴즈)

-- 관리자 ID 및 관련 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), design_principle_tag AS (
    SELECT id FROM public.tags WHERE name = '소프트웨어 설계 원칙'
),
-- 설계 원칙 관련 퀴즈 생성
     inserted_design_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- SOLID 원칙 (중급)
                 (NOW() - INTERVAL '2 days', NOW(), 'SOLID 원칙과 객체 지향 설계', '객체 지향 설계의 5가지 핵심 원칙(SOLID)의 개념과 중요성을 이해합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 45 + 8), random() * 25 + 68, floor(random() * 280 + 35), NULL),
                 -- GoF 디자인 패턴 (중급)
                 (NOW() - INTERVAL '1 day', NOW(), 'GoF 디자인 패턴 기초', '생성, 구조, 행위 패턴 등 자주 사용되는 GoF 디자인 패턴의 목적과 구조를 학습합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 40, (SELECT id FROM admin_user), floor(random() * 50 + 10), random() * 28 + 65, floor(random() * 310 + 40), NULL),
                 -- 테스팅 및 클린 코드 (중급)
                 (NOW(), NOW(), '테스팅 및 클린 코드 원칙', 'TDD, 단위/통합 테스트의 개념과 중요성, 클린 코드 작성 원칙을 알아봅니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 55 + 12), random() * 26 + 67, floor(random() * 340 + 45), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_design AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, dpt.id FROM inserted_design_quizzes iq, design_principle_tag dpt
             WHERE iq.title LIKE '%SOLID%' OR iq.title LIKE '%디자인 패턴%' OR iq.title LIKE '%테스팅%' OR iq.title LIKE '%클린 코드%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Software Design Principle Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (설계 원칙 관련 퀴즈별 10개씩, 총 30개)

-- SOLID 원칙 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = 'SOLID 원칙과 객체 지향 설계' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'SOLID 원칙 중, "한 클래스는 하나의 책임만 가져야 한다"는 원칙은 무엇인가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'SRP (Single Responsibility Principle, 단일 책임 원칙)', '["SRP (Single Responsibility Principle, 단일 책임 원칙)", "OCP (Open-Closed Principle, 개방-폐쇄 원칙)", "LSP (Liskov Substitution Principle, 리스코프 치환 원칙)", "ISP (Interface Segregation Principle, 인터페이스 분리 원칙)"]'::jsonb, 'SRP는 클래스를 변경해야 하는 이유는 단 하나여야 한다는 원칙입니다. 즉, 클래스는 응집도 높은 책임 하나만 수행해야 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SOLID 원칙 중, "소프트웨어 요소(클래스, 모듈, 함수 등)는 확장에는 열려 있어야 하고, 변경에는 닫혀 있어야 한다"는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'OCP (Open-Closed Principle, 개방-폐쇄 원칙)', '["SRP", "OCP", "LSP", "DIP (Dependency Inversion Principle, 의존관계 역전 원칙)"]'::jsonb, 'OCP는 기존 코드를 수정하지 않고도 기능을 추가하거나 변경할 수 있도록 설계해야 한다는 원칙입니다. 주로 추상화와 다형성을 통해 달성됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SOLID 원칙 중, "자식 클래스는 언제나 부모 클래스를 대체할 수 있어야 한다"는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'LSP (Liskov Substitution Principle, 리스코프 치환 원칙)', '["SRP", "OCP", "LSP", "ISP"]'::jsonb, 'LSP는 부모 타입 객체를 사용하는 곳에서 자식 타입 객체로 바꾸어도 프로그램의 정확성이 깨지지 않아야 한다는 원칙입니다. 즉, 자식 클래스는 부모 클래스의 규약을 지켜야 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SOLID 원칙 중, "클라이언트는 자신이 사용하지 않는 메서드에 의존 관계를 맺으면 안 된다"는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'ISP (Interface Segregation Principle, 인터페이스 분리 원칙)', '["LSP", "ISP", "DIP", "SRP"]'::jsonb, 'ISP는 하나의 범용적인 인터페이스보다는, 특정 클라이언트를 위한 여러 개의 구체적인 인터페이스로 분리하는 것이 좋다는 원칙입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SOLID 원칙 중, "상위 수준 모듈은 하위 수준 모듈에 의존해서는 안 되며, 둘 다 추상화에 의존해야 한다. 추상화는 세부 사항에 의존해서는 안 된다"는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'DIP (Dependency Inversion Principle, 의존관계 역전 원칙)', '["OCP", "LSP", "ISP", "DIP"]'::jsonb, 'DIP는 구체적인 구현 클래스에 직접 의존하기보다, 인터페이스나 추상 클래스와 같은 추상화에 의존해야 한다는 원칙입니다. 의존성 주입(DI)은 이 원칙을 구현하는 방법 중 하나입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '단일 책임 원칙(SRP)을 위반할 경우 발생할 수 있는 문제점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '하나의 변경이 관련 없는 다른 기능에 영향을 줄 수 있다', '["코드 재사용성이 극대화된다", "클래스의 크기가 항상 작아진다", "하나의 변경이 관련 없는 다른 기능에 영향을 줄 수 있다", "객체 간의 결합도가 낮아진다"]'::jsonb, '하나의 클래스가 여러 책임을 가지면, 한 책임의 변경이 다른 책임과 관련된 코드에 예기치 않은 영향을 미칠 수 있어 유지보수가 어려워집니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '개방-폐쇄 원칙(OCP)을 잘 따르는 설계의 장점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '기존 코드를 수정하지 않고 새로운 기능을 쉽게 추가할 수 있다', '["코드의 양이 항상 줄어든다", "컴파일 시간이 단축된다", "기존 코드를 수정하지 않고 새로운 기능을 쉽게 추가할 수 있다", "모든 클래스가 구체 클래스에만 의존한다"]'::jsonb, 'OCP를 준수하면 새로운 요구사항이 발생했을 때 기존 코드의 변경을 최소화하면서 기능을 확장할 수 있어 유연하고 안정적인 시스템을 만들 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '리스코프 치환 원칙(LSP)을 위반하는 예시는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '자식 클래스에서 부모 클래스의 메서드를 오버라이드하며 사전 조건을 강화하는 경우', '["자식 클래스가 부모 클래스의 모든 메서드를 구현하는 경우", "자식 클래스에서 부모 클래스의 메서드를 오버라이드하며 사전 조건을 강화하는 경우", "자식 클래스가 새로운 메서드를 추가하는 경우", "부모 클래스 타입으로 자식 클래스 객체를 참조하는 경우"]'::jsonb, '자식 클래스에서 메서드의 사전 조건(메서드 실행 전에 만족해야 하는 조건)을 강화하면, 부모 타입을 기대하는 코드에서 해당 자식 객체를 사용할 때 예기치 않은 오류가 발생할 수 있어 LSP를 위반합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '인터페이스 분리 원칙(ISP)을 적용하면 얻을 수 있는 이점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '클라이언트가 불필요한 메서드에 의존하지 않게 되어 결합도가 낮아진다', '["인터페이스의 개수가 항상 줄어든다", "클라이언트가 불필요한 메서드에 의존하지 않게 되어 결합도가 낮아진다", "모든 메서드가 public으로 선언된다", "클래스 상속 구조가 단순해진다"]'::jsonb, '인터페이스를 역할과 책임에 맞게 잘 분리하면, 클라이언트는 자신이 필요한 메서드만 포함된 인터페이스에 의존하게 되어 시스템의 결합도를 낮추고 유연성을 높일 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '의존관계 역전 원칙(DIP)을 적용하지 않고 구체 클래스에 직접 의존할 경우 발생하는 문제점은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '하위 수준 모듈의 변경이 상위 수준 모듈에 직접적인 영향을 미친다', '["상위 수준 모듈을 재사용하기 쉬워진다", "코드의 가독성이 항상 향상된다", "하위 수준 모듈의 변경이 상위 수준 모듈에 직접적인 영향을 미친다", "추상화 계층이 불필요해진다"]'::jsonb, '상위 모듈이 구체적인 하위 모듈에 직접 의존하면, 하위 모듈의 구현이 변경될 때마다 상위 모듈도 함께 수정해야 하는 경직된 구조가 됩니다.', 10, 45, (SELECT id FROM quiz_info));

-- GoF 디자인 패턴 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = 'GoF 디자인 패턴 기초' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '객체 생성을 위한 패턴 중, 특정 클래스의 인스턴스가 오직 하나만 존재하도록 보장하고 어디서든 접근 가능하게 하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '싱글톤 패턴 (Singleton Pattern)', '["팩토리 메서드 패턴 (Factory Method Pattern)", "추상 팩토리 패턴 (Abstract Factory Pattern)", "싱글톤 패턴 (Singleton Pattern)", "빌더 패턴 (Builder Pattern)"]'::jsonb, '싱글톤 패턴은 애플리케이션 내에서 유일해야 하는 객체(예: 설정 관리자, 로거)를 생성할 때 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '객체 생성 책임을 서브클래스에게 위임하여, 어떤 클래스의 인스턴스를 만들지는 서브클래스가 결정하도록 하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '팩토리 메서드 패턴 (Factory Method Pattern)', '["팩토리 메서드 패턴 (Factory Method Pattern)", "프로토타입 패턴 (Prototype Pattern)", "싱글톤 패턴 (Singleton Pattern)", "추상 팩토리 패턴 (Abstract Factory Pattern)"]'::jsonb, '팩토리 메서드 패턴은 객체 생성 로직을 캡슐화하여 클라이언트 코드와 구체적인 생성 클래스 간의 결합도를 낮춥니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '서로 관련 있거나 의존적인 객체들의 집합을 구체적인 클래스를 지정하지 않고 생성할 수 있게 하는 인터페이스를 제공하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '추상 팩토리 패턴 (Abstract Factory Pattern)', '["빌더 패턴 (Builder Pattern)", "추상 팩토리 패턴 (Abstract Factory Pattern)", "팩토리 메서드 패턴 (Factory Method Pattern)", "싱글톤 패턴 (Singleton Pattern)"]'::jsonb, '추상 팩토리 패턴은 여러 종류의 관련 객체들을 일관된 방식으로 생성해야 할 때 유용합니다. (예: 특정 테마의 UI 요소 생성)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '알고리즘 군(ตระกูล)을 정의하고 각각을 캡슐화하여 서로 교체 가능하게 만드는 패턴은? 이를 통해 클라이언트로부터 알고리즘 구현을 분리할 수 있다.', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '전략 패턴 (Strategy Pattern)', '["템플릿 메서드 패턴 (Template Method Pattern)", "상태 패턴 (State Pattern)", "전략 패턴 (Strategy Pattern)", "커맨드 패턴 (Command Pattern)"]'::jsonb, '전략 패턴은 동일한 문제를 해결하는 여러 알고리즘이 있을 때, 이들을 각각 별도의 클래스로 캡슐화하고 실행 중에 동적으로 교체하여 사용할 수 있게 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '한 객체의 상태가 변경되면 그 객체에 의존하는 다른 객체들에게 자동으로 알림이 가서 업데이트되도록 하는 1:N 의존 관계를 정의하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '옵저버 패턴 (Observer Pattern)', '["중재자 패턴 (Mediator Pattern)", "옵저버 패턴 (Observer Pattern)", "상태 패턴 (State Pattern)", "방문자 패턴 (Visitor Pattern)"]'::jsonb, '옵저버 패턴은 주제(Subject) 객체와 관찰자(Observer) 객체 간의 느슨한 결합을 통해, 상태 변화 전파 및 이벤트 처리 시스템 구현에 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '객체에 동적으로 새로운 책임(기능)을 추가할 수 있게 하는 패턴은? 서브클래싱을 통한 기능 확장보다 유연한 대안을 제공한다.', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '데코레이터 패턴 (Decorator Pattern)', '["프록시 패턴 (Proxy Pattern)", "어댑터 패턴 (Adapter Pattern)", "퍼사드 패턴 (Facade Pattern)", "데코레이터 패턴 (Decorator Pattern)"]'::jsonb, '데코레이터 패턴은 기존 객체를 감싸는(Decorating) 방식으로, 객체의 원래 인터페이스를 유지하면서 동적으로 기능을 추가하거나 변경합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '호환되지 않는 인터페이스를 가진 클래스들을 함께 동작하도록, 하나의 인터페이스를 다른 인터페이스로 변환해주는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '어댑터 패턴 (Adapter Pattern)', '["브릿지 패턴 (Bridge Pattern)", "컴포지트 패턴 (Composite Pattern)", "어댑터 패턴 (Adapter Pattern)", "데코레이터 패턴 (Decorator Pattern)"]'::jsonb, '어댑터 패턴은 기존 클래스의 코드를 변경하지 않고도, 클라이언트가 요구하는 인터페이스에 맞게 변환하여 재사용성을 높여줍니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '복잡한 서브시스템을 더 쉽게 사용할 수 있도록, 단순화된 통합 인터페이스를 제공하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '퍼사드 패턴 (Facade Pattern)', '["퍼사드 패턴 (Facade Pattern)", "플라이웨이트 패턴 (Flyweight Pattern)", "프록시 패턴 (Proxy Pattern)", "체인 오브 리스판서빌리티 패턴 (Chain of Responsibility Pattern)"]'::jsonb, '퍼사드 패턴은 서브시스템의 복잡한 내부 구조를 숨기고, 클라이언트에게 사용하기 쉬운 단일 인터페이스(Facade)를 제공하여 시스템 사용 편의성을 높입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '알고리즘의 구조(뼈대)를 메서드에 정의하고, 일부 단계는 서브클래스에서 구현하도록 하는 패턴은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '템플릿 메서드 패턴 (Template Method Pattern)', '["전략 패턴 (Strategy Pattern)", "템플릿 메서드 패턴 (Template Method Pattern)", "상태 패턴 (State Pattern)", "빌더 패턴 (Builder Pattern)"]'::jsonb, '템플릿 메서드 패턴은 알고리즘의 전체 구조는 상위 클래스에서 정의하되, 구체적인 처리 내용만 서브클래스에서 오버라이드하여 변경할 수 있게 함으로써 코드 중복을 줄이고 일관된 구조를 유지합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '디자인 패턴을 사용하는 주된 이유로 적절하지 않은 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '코드의 양을 항상 최소화하기 위해', '["재사용 가능하고 유지보수하기 쉬운 코드를 작성하기 위해", "개발자 간의 의사소통을 원활하게 하기 위해", "특정 문제 상황에 대한 검증된 해결책을 적용하기 위해", "코드의 양을 항상 최소화하기 위해"]'::jsonb, '디자인 패턴은 특정 문제를 해결하는 좋은 구조를 제공하여 코드의 유연성, 재사용성, 유지보수성을 높이는 데 목적이 있습니다. 패턴 적용으로 인해 코드 양이 늘어날 수도 있습니다.', 10, 45, (SELECT id FROM quiz_info));

-- 테스팅 및 클린 코드 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '테스팅 및 클린 코드 원칙' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '테스트 주도 개발(TDD, Test-Driven Development)의 기본적인 개발 사이클은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Red - Green - Refactor', '["Design - Code - Test", "Code - Test - Refactor", "Red - Green - Refactor", "Test - Code - Document"]'::jsonb, 'TDD는 실패하는 테스트 코드(Red)를 먼저 작성하고, 이 테스트를 통과하는 최소한의 코드(Green)를 작성한 후, 코드의 구조를 개선(Refactor)하는 과정을 반복합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '소프트웨어 테스트 유형 중, 개별 함수나 클래스 등 가장 작은 코드 단위의 기능을 검증하는 테스트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '단위 테스트 (Unit Test)', '["단위 테스트 (Unit Test)", "통합 테스트 (Integration Test)", "인수 테스트 (Acceptance Test)", "E2E (End-to-End) 테스트"]'::jsonb, '단위 테스트는 코드의 특정 부분이 의도한 대로 정확히 작동하는지 독립적으로 검증합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '소프트웨어 테스트 유형 중, 여러 모듈이나 컴포넌트들이 함께 연동될 때 발생하는 상호작용 및 인터페이스를 검증하는 테스트는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '통합 테스트 (Integration Test)', '["단위 테스트 (Unit Test)", "통합 테스트 (Integration Test)", "성능 테스트 (Performance Test)", "부하 테스트 (Load Test)"]'::jsonb, '통합 테스트는 단위 테스트를 통과한 모듈들이 결합되었을 때도 정상적으로 동작하는지 확인합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '단위 테스트 작성 시, 테스트 대상 코드의 외부 의존성(예: 데이터베이스, 네트워크 호출)을 실제 객체 대신 가짜 객체로 대체하는 기법을 통칭하여 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '테스트 더블 (Test Double)', '["테스트 스위트 (Test Suite)", "테스트 하네스 (Test Harness)", "테스트 더블 (Test Double)", "테스트 커버리지 (Test Coverage)"]'::jsonb, '테스트 더블에는 Mock, Stub, Fake, Dummy, Spy 등이 있으며, 외부 환경에 의존하지 않고 단위 테스트를 격리하여 실행할 수 있게 돕습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '테스트 더블(Test Double) 중, 미리 정해진 답변을 반환하도록 설정되어 테스트 대상 코드의 특정 실행 경로를 유도하는 데 사용되는 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '스텁 (Stub)', '["목 (Mock)", "스텁 (Stub)", "페이크 (Fake)", "스파이 (Spy)"]'::jsonb, '스텁은 테스트 중에 호출되었을 때 미리 준비된 단순한 값을 반환하여, 테스트가 외부 의존성의 실제 로직 없이 진행될 수 있도록 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클린 코드(Clean Code) 원칙 중, 변수, 함수, 클래스 등의 이름을 그 역할과 의도를 명확히 알 수 있도록 짓는 것을 강조하는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '의미 있는 이름 사용 (Use Meaningful Names)', '["작은 함수 만들기 (Small Functions)", "주석은 필요악 (Comments are Liars)", "의미 있는 이름 사용 (Use Meaningful Names)", "단위 테스트 작성 (Write Unit Tests)"]'::jsonb, '이름만 보고도 해당 코드 요소가 무엇을 하는지, 왜 존재하는지 쉽게 파악할 수 있어야 가독성과 유지보수성이 높아집니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클린 코드 원칙 중, 함수는 한 가지 작업만 수행하고, 그 작업을 잘 해야 하며, 가능한 한 작게 만들어야 한다는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '작은 함수 만들기 (Small Functions / Do One Thing)', '["오류 처리 잘하기 (Handle Errors Gracefully)", "작은 함수 만들기 (Small Functions / Do One Thing)", "중복을 피하라 (DRY - Dont Repeat Yourself)", "형식을 맞추어라 (Formatting Matters)"]'::jsonb, '함수가 작고 단일 책임을 가지면 이해하기 쉽고, 테스트하기 용이하며, 재사용성이 높아집니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클린 코드 원칙 중, 동일한 코드 조각이나 로직이 여러 곳에 반복적으로 나타나지 않도록 해야 한다는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'DRY (Don’t Repeat Yourself)', '["KISS (Keep It Simple, Stupid)", "YAGNI (You Aint Gonna Need It)", "DRY (Don’t Repeat Yourself)", "SoC (Separation of Concerns)"]'::jsonb, '코드 중복은 유지보수를 어렵게 만들고 버그 발생 가능성을 높입니다. 중복되는 부분은 함수나 클래스 등으로 추상화하여 재사용해야 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클린 코드 관점에서 주석(Comment)에 대한 올바른 태도는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '코드로 의도를 표현할 수 없을 때 최후의 수단으로 사용한다', '["모든 코드 라인에 주석을 다는 것이 좋다", "주석은 많을수록 코드 이해에 도움이 된다", "코드로 의도를 표현할 수 없을 때 최후의 수단으로 사용한다", "주석 대신 긴 변수명을 사용해야 한다"]'::jsonb, '가장 좋은 코드는 주석 없이도 쉽게 이해할 수 있는 코드입니다. 주석은 코드 자체로 표현하기 어려운 의도나 배경 설명을 보충하는 용도로 최소한으로 사용해야 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클린 코드 원칙 중, 현재 필요하지 않은 기능은 미리 예측하여 구현하지 말라는 원칙은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'YAGNI (You Ain’t Gonna Need It)', '["DRY (Don’t Repeat Yourself)", "YAGNI (You Ain’t Gonna Need It)", "SOLID", "KISS (Keep It Simple, Stupid)"]'::jsonb, 'YAGNI 원칙은 미래에 필요할 것이라는 가정 하에 미리 기능을 추가하는 것은 복잡성을 증가시키고 불필요한 작업이 될 수 있으므로, 현재 요구사항에 필요한 기능만 구현하라는 의미입니다.', 10, 45, (SELECT id FROM quiz_info));