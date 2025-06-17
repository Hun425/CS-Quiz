-- 새로운 더미 데이터 추가 (스프링 초급, 중급, 고급)

-- 1. 필요한 태그 추가 (스프링)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '스프링', '스프링 프레임워크(Spring Framework) 및 스프링 부트(Spring Boot) 관련 개념, 사용법, 생태계')
ON CONFLICT (name) DO NOTHING; -- 이미 존재하면 추가하지 않음

-- (선택) 스프링 태그를 백엔드 태그의 하위 태그로 설정할 경우
-- WITH backend_tag AS (SELECT id FROM public.tags WHERE name = '백엔드'),
--      spring_tag_update AS (UPDATE public.tags SET parent_id = (SELECT id FROM backend_tag) WHERE name = '스프링' AND parent_id IS NULL)
-- SELECT 'Spring tag parent updated if necessary';

-- 2. 새로운 퀴즈 추가 (스프링 초급, 중급, 고급 - 각 1개씩, 총 3개 퀴즈)

-- 관리자 ID 및 스프링 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), spring_tag AS (
    SELECT id FROM public.tags WHERE name = '스프링'
),
-- 스프링 퀴즈 생성
     inserted_spring_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 스프링 초급
                 (NOW() - INTERVAL '2 days', NOW(), '스프링 프레임워크 기초 다지기', 'IoC, DI, Bean 등 스프링의 핵심 기본 개념을 묻는 퀴즈입니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 60 + 10), random() * 30 + 65, floor(random() * 350 + 40), NULL),
                 -- 스프링 중급
                 (NOW() - INTERVAL '1 day', NOW(), '스프링 핵심 원리 및 활용', 'AOP, Spring Boot, Spring Data JPA 등 스프링의 주요 기능 활용법을 다룹니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 50 + 8), random() * 25 + 70, floor(random() * 280 + 30), NULL),
                 -- 스프링 고급
                 (NOW(), NOW(), '스프링 심화 및 Spring Boot 활용', 'Spring Security, Batch, Cloud 등 고급 주제와 실전 활용 능력을 평가합니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 40 + 5), random() * 15 + 75, floor(random() * 200 + 20), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_spring AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, st.id FROM inserted_spring_quizzes iq, spring_tag st WHERE iq.title LIKE '스프링%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Spring Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (스프링 퀴즈별 10개씩, 총 30개)

-- 스프링 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '스프링 프레임워크 기초 다지기' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '스프링 프레임워크의 핵심 개념 중 하나로, 객체의 생성과 관리를 프레임워크가 담당하는 것을 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'BEGINNER', 'IoC (Inversion of Control)', '["AOP (Aspect-Oriented Programming)", "DI (Dependency Injection)", "IoC (Inversion of Control)", "MVC (Model-View-Controller)"]'::jsonb, 'IoC(제어의 역전)는 개발자가 직접 객체를 생성하고 관리하는 것이 아니라, 스프링 컨테이너가 그 역할을 대신하는 것을 의미합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 객체 간의 의존 관계를 외부(컨테이너)에서 주입해주는 디자인 패턴은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'DI (Dependency Injection)', '["싱글톤 패턴 (Singleton Pattern)", "팩토리 패턴 (Factory Pattern)", "DI (Dependency Injection)", "프록시 패턴 (Proxy Pattern)"]'::jsonb, 'DI(의존성 주입)는 객체가 필요로 하는 다른 객체(의존성)를 직접 생성하거나 찾는 것이 아니라, 외부(스프링 컨테이너)로부터 주입받는 방식입니다. IoC를 구현하는 대표적인 방법입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 컨테이너가 관리하는 자바 객체를 무엇이라고 부르는가?', 'MULTIPLE_CHOICE', 'BEGINNER', '빈 (Bean)', '["컴포넌트 (Component)", "서비스 (Service)", "빈 (Bean)", "모듈 (Module)"]'::jsonb, '스프링 IoC 컨테이너에 의해 생성되고 관리되는 객체를 빈(Bean)이라고 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 클래스를 빈으로 등록하기 위해 사용하는 가장 기본적인 애노테이션은?', 'MULTIPLE_CHOICE', 'BEGINNER', '@Component', '["@Autowired", "@Bean", "@Component", "@Service"]'::jsonb, '@Component 애노테이션은 해당 클래스가 스프링 컴포넌트 스캔의 대상이 되어 빈으로 등록되도록 표시하는 가장 기본적인 스테레오타입 애노테이션입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 의존성을 자동으로 주입받기 위해 사용하는 애노테이션은?', 'MULTIPLE_CHOICE', 'BEGINNER', '@Autowired', '["@Inject", "@Resource", "@Autowired", "@Qualifier"]'::jsonb, '@Autowired는 스프링 컨테이너가 관리하는 빈 중에서 타입이 일치하는 빈을 찾아 자동으로 의존성을 주입해주는 애노테이션입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 MVC 패턴에서 클라이언트의 요청을 가장 먼저 받아 처리하고 적절한 컨트롤러에게 전달하는 역할을 하는 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'DispatcherServlet', '["HandlerMapping", "ViewResolver", "Controller", "DispatcherServlet"]'::jsonb, 'DispatcherServlet은 스프링 MVC의 핵심 컴포넌트로, 모든 클라이언트 요청을 받아 적절한 핸들러(컨트롤러)를 찾고 요청을 위임하며, 결과를 받아 뷰를 렌더링하는 프론트 컨트롤러 역할을 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 웹 요청을 처리하는 메서드를 정의하는 컨트롤러 클래스에 주로 사용하는 애노테이션은?', 'MULTIPLE_CHOICE', 'BEGINNER', '@Controller', '["@Service", "@Repository", "@Component", "@Controller"]'::jsonb, '@Controller 애노테이션은 해당 클래스가 스프링 MVC의 컨트롤러 역할을 함을 나타냅니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 비즈니스 로직을 처리하는 서비스 계층의 컴포넌트에 주로 사용하는 애노테이션은?', 'MULTIPLE_CHOICE', 'BEGINNER', '@Service', '["@Controller", "@Service", "@Repository", "@Component"]'::jsonb, '@Service 애노테이션은 해당 클래스가 비즈니스 로직을 담당하는 서비스 계층의 컴포넌트임을 나타냅니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 데이터 접근 계층(DAO)의 컴포넌트에 주로 사용하며, 데이터베이스 관련 예외를 스프링 예외로 변환해주는 기능도 있는 애노테이션은?', 'MULTIPLE_CHOICE', 'BEGINNER', '@Repository', '["@Component", "@Service", "@Repository", "@Entity"]'::jsonb, '@Repository 애노테이션은 해당 클래스가 데이터 접근 계층의 컴포넌트임을 나타내며, 플랫폼별 데이터베이스 예외를 스프링의 DataAccessException으로 변환하는 기능을 포함합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 빈의 기본 스코프(Scope)는 무엇인가?', 'MULTIPLE_CHOICE', 'BEGINNER', 'singleton', '["prototype", "request", "session", "singleton"]'::jsonb, '별도로 스코프를 지정하지 않으면 스프링 빈은 기본적으로 싱글톤(singleton) 스코프를 가집니다. 즉, 스프링 컨테이너 내에서 해당 타입의 빈은 단 하나만 생성됩니다.', 5, 30, (SELECT id FROM quiz_info));

-- 스프링 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '스프링 핵심 원리 및 활용' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '스프링 AOP(Aspect-Oriented Programming)에서 공통 관심사(Cross-cutting concern)를 모듈화한 것을 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Aspect', '["Join Point", "Advice", "Pointcut", "Aspect"]'::jsonb, 'Aspect는 로깅, 트랜잭션, 보안 등 여러 객체에 걸쳐 공통적으로 나타나는 부가 기능(공통 관심사)을 모듈화한 단위입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 AOP에서 Aspect가 적용될 수 있는 위치(메서드 실행, 필드 접근 등)를 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Join Point', '["Advice", "Pointcut", "Join Point", "Target"]'::jsonb, 'Join Point는 메서드 실행 시점, 생성자 호출 시점, 필드 값 접근 시점 등 Aspect를 적용할 수 있는 모든 지점을 의미합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Boot의 주요 특징으로, 필요한 라이브러리 의존성들을 모아놓은 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Starter', '["Auto-configuration", "Actuator", "CLI (Command Line Interface)", "Starter"]'::jsonb, 'Spring Boot Starter는 특정 기능을 개발하는 데 필요한 의존성 그룹을 미리 정의해 놓은 것으로, 개발자가 일일이 의존성을 관리하는 번거로움을 줄여줍니다 (예: `spring-boot-starter-web`).', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Boot가 클래스패스 설정과 기존 빈들을 기반으로 애플리케이션 설정을 자동으로 구성해주는 기능은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Auto-configuration', '["Actuator", "Starter", "Auto-configuration", "Externalized Configuration"]'::jsonb, 'Auto-configuration(자동 설정)은 Spring Boot가 개발자의 개입 없이도 애플리케이션에 필요한 설정을 자동으로 추측하고 구성해주는 기능입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Boot 애플리케이션의 상태 모니터링 및 관리를 위한 엔드포인트를 제공하는 기능은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Actuator', '["DevTools", "Actuator", "Thymeleaf", "Spring Data"]'::jsonb, 'Spring Boot Actuator는 애플리케이션의 상태 확인(health check), 메트릭 수집, 환경 변수 조회 등 운영에 필요한 다양한 엔드포인트를 제공합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Data JPA에서 데이터베이스 작업을 위한 인터페이스를 정의할 때 주로 상속받는 인터페이스는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'JpaRepository', '["EntityManager", "JdbcTemplate", "JpaRepository", "CrudRepository"]'::jsonb, '`JpaRepository` (또는 그 상위 인터페이스인 `CrudRepository`, `PagingAndSortingRepository`)를 상속받으면 기본적인 CRUD 및 페이징/정렬 기능을 별도 구현 없이 사용할 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 발생하는 예외를 전역적으로 처리하기 위해 사용하는 애노테이션은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '@ControllerAdvice', '["@ExceptionHandler", "@ControllerAdvice", "@ResponseStatus", "@Valid"]'::jsonb, '@ControllerAdvice 애노테이션을 클래스에 붙이면 여러 컨트롤러에서 발생하는 예외를 한 곳에서 처리하는 전역 예외 핸들러를 정의할 수 있습니다. `@ExceptionHandler`와 함께 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 특정 환경(개발, 운영 등)에 따라 다른 설정(빈 구성)을 활성화하는 기능은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Profile', '["Environment", "Properties", "Profile", "Configuration"]'::jsonb, '스프링 Profile 기능을 사용하면 `@Profile` 애노테이션이나 환경 변수 등을 통해 특정 환경에서만 활성화될 빈이나 설정을 정의할 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring MVC에서 컨트롤러 메서드의 파라미터로 HTTP 요청 본문(body)의 데이터를 받기 위해 사용하는 애노테이션은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '@RequestBody', '["@RequestParam", "@PathVariable", "@RequestBody", "@ModelAttribute"]'::jsonb, '@RequestBody 애노테이션은 HTTP 요청의 본문 내용을 자바 객체로 변환하여 메서드 파라미터로 전달받을 때 사용됩니다. 주로 JSON이나 XML 데이터를 처리할 때 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'AOP에서 대상 객체(Target)의 메서드를 감싸서 부가 기능을 제공하는 객체를 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'Proxy', '["Aspect", "Advice", "Proxy", "Weaving"]'::jsonb, '스프링 AOP는 기본적으로 프록시 기반으로 동작합니다. 대상 객체 대신 프록시 객체를 생성하여, 메서드 호출 시 프록시 객체가 먼저 호출되어 부가 기능(Advice)을 수행한 후 실제 대상 객체의 메서드를 호출합니다.', 10, 45, (SELECT id FROM quiz_info));

-- 스프링 고급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '스프링 심화 및 Spring Boot 활용' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'Spring Security에서 인증(Authentication)과 인가(Authorization)의 차이점으로 올바른 설명은?', 'MULTIPLE_CHOICE', 'ADVANCED', '인증은 사용자가 누구인지 확인하는 과정, 인가는 사용자가 특정 리소스에 접근할 권한이 있는지 확인하는 과정', '["인증은 권한 부여, 인가는 신원 확인", "인증은 필터 체인, 인가는 인터셉터", "인증은 사용자가 누구인지 확인하는 과정, 인가는 사용자가 특정 리소스에 접근할 권한이 있는지 확인하는 과정", "인증은 공개 API, 인가는 보호된 API"]'::jsonb, '인증은 사용자의 신원을 확인하는 과정(로그인 등)이고, 인가는 인증된 사용자가 특정 자원이나 기능에 접근할 수 있는 권한이 있는지를 검사하는 과정입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Security의 필터 체인(Filter Chain)에 대한 설명으로 적절하지 않은 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '모든 필터는 반드시 실행된다', '["요청이 들어올 때 여러 보안 필터들이 순서대로 실행된다", "필터마다 특정 보안 기능을 담당한다", "설정을 통해 특정 필터를 추가하거나 제외할 수 있다", "모든 필터는 반드시 실행된다"]'::jsonb, '스프링 시큐리티는 여러 보안 필터로 구성된 체인을 통해 요청을 처리합니다. 각 필터는 특정 보안 로직을 수행하며, 조건에 따라 다음 필터로 요청을 전달하거나 처리를 중단할 수 있습니다. 즉, 모든 필터가 항상 실행되는 것은 아닙니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Batch에서 대규모 데이터 처리를 위한 기본 구성 요소가 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'JobScheduler', '["Job", "Step", "ItemReader", "JobScheduler"]'::jsonb, 'Spring Batch의 핵심 구성 요소는 Job(배치 작업 단위), Step(Job의 독립적인 단계), ItemReader(데이터 읽기), ItemProcessor(데이터 가공), ItemWriter(데이터 쓰기) 등입니다. JobScheduler는 Spring Batch 자체 구성 요소라기보다는 배치 작업을 실행시키는 외부 스케줄러(예: Spring Scheduler, Quartz)를 의미하는 경우가 많습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Cloud 환경에서 서비스 검색(Service Discovery) 기능을 제공하는 컴포넌트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Eureka Server', '["Config Server", "API Gateway (Zuul/Spring Cloud Gateway)", "Eureka Server", "Hystrix"]'::jsonb, 'Eureka Server는 마이크로서비스들이 자신을 등록하고 다른 서비스의 위치(IP, 포트)를 찾을 수 있도록 하는 서비스 검색 서버 역할을 합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'Spring Cloud 환경에서 마이크로서비스들의 진입점(Entry Point) 역할을 하며 라우팅, 인증 등을 처리하는 컴포넌트는?', 'MULTIPLE_CHOICE', 'ADVANCED', 'API Gateway', '["Eureka Server", "Config Server", "API Gateway", "Ribbon"]'::jsonb, 'API Gateway는 클라이언트의 요청을 받아 적절한 마이크로서비스로 라우팅하고, 인증/인가, 로깅, 로드 밸런싱 등 공통적인 기능을 처리하는 역할을 합니다. (예: Spring Cloud Gateway, Zuul)', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 트랜잭션 처리 애노테이션 `@Transactional`의 `propagation` 속성 중, 이미 진행 중인 트랜잭션이 있으면 참여하고 없으면 새로 시작하는 기본 전파 속성은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'REQUIRED', '["REQUIRED", "REQUIRES_NEW", "SUPPORTS", "MANDATORY"]'::jsonb, '`REQUIRED`는 `@Transactional`의 기본 전파 속성으로, 부모 트랜잭션이 있으면 참여하고 없으면 새로운 트랜잭션을 시작합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 단위 테스트(Unit Test)와 통합 테스트(Integration Test)의 주요 차이점은?', 'MULTIPLE_CHOICE', 'ADVANCED', '통합 테스트는 스프링 컨텍스트를 로드하여 여러 컴포넌트를 함께 테스트', '["단위 테스트는 Mock 객체를 사용할 수 없다", "통합 테스트는 실행 속도가 더 빠르다", "단위 테스트는 전체 애플리케이션을 실행한다", "통합 테스트는 스프링 컨텍스트를 로드하여 여러 컴포넌트를 함께 테스트"]'::jsonb, '단위 테스트는 특정 클래스나 메서드 등 작은 단위를 독립적으로 테스트하는 반면, 통합 테스트는 스프링 컨테이너를 실행하여 데이터베이스 연동 등 여러 컴포넌트가 통합된 상태에서 동작을 검증합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 부트 테스트에서 슬라이스 테스트(Slice Test)를 위해 사용하는 애노테이션이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '@SpringBootTest', '["@WebMvcTest", "@DataJpaTest", "@RestClientTest", "@SpringBootTest"]'::jsonb, '슬라이스 테스트는 특정 계층(Web, Data 등)만 로드하여 테스트하는 방식입니다. `@WebMvcTest`, `@DataJpaTest`, `@RestClientTest` 등이 슬라이스 테스트 애노테이션입니다. `@SpringBootTest`는 전체 애플리케이션 컨텍스트를 로드하여 통합 테스트에 사용됩니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링 빈의 생명주기 콜백(Lifecycle Callback) 메서드를 지정하는 방법이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '`@PostConstruct` 애노테이션 사용', '["InitializingBean 인터페이스 구현", "DisposableBean 인터페이스 구현", "`@PostConstruct` 애노테이션 사용", "`@PreDestroy` 애노테이션 사용"]'::jsonb, '빈 초기화 콜백은 `InitializingBean` 인터페이스 구현 또는 `@PostConstruct` 애노테이션으로, 빈 소멸 콜백은 `DisposableBean` 인터페이스 구현 또는 `@PreDestroy` 애노테이션으로 지정할 수 있습니다. `@PostConstruct`는 초기화 콜백입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '스프링에서 비동기 메서드 실행을 가능하게 하는 애노테이션은?', 'MULTIPLE_CHOICE', 'ADVANCED', '@Async', '["@EnableAsync", "@Async", "@Scheduled", "@Transactional"]'::jsonb, '메서드에 `@Async` 애노테이션을 붙이면 해당 메서드가 별도의 스레드에서 비동기적으로 실행됩니다. 이를 사용하기 위해서는 설정 클래스에 `@EnableAsync` 애노테이션을 추가해야 합니다.', 15, 60, (SELECT id FROM quiz_info));