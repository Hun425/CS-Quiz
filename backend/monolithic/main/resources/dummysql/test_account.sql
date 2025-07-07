-- 테스트용 계정 생성 (OAuth2 기반 - 모든 필수 필드 포함)
INSERT INTO public.users (
    id, provider, provider_id, email, username, role, 
    is_active, total_points, level, experience, required_experience,
    created_at, updated_at, last_login
)
VALUES (
    9999, 'TEST', 'test-provider-9999', 'test@example.com', 'k6tester', 'USER',
    true, 100, 1, 50, 100,
    NOW(), NOW(), NOW()
)
ON CONFLICT (email) DO NOTHING;

-- 테스트 계정에 몇 가지 퀴즈 연결
INSERT INTO public.quizzes (id, title, description, creator_id, quiz_type, difficulty_level, time_limit, created_at, updated_at, is_public, view_count, attempt_count, avg_score)
VALUES 
    (9001, '테스트 퀴즈 1', 'k6 테스트용 퀴즈입니다.', 9999, 'REGULAR', 'BEGINNER', 600, NOW(), NOW(), true, 0, 0, 0),
    (9002, '테스트 퀴즈 2', 'k6 테스트용 퀴즈입니다.', 9999, 'REGULAR', 'INTERMEDIATE', 600, NOW(), NOW(), true, 0, 0, 0),
    (9003, '테스트 퀴즈 3', 'k6 테스트용 퀴즈입니다.', 9999, 'REGULAR', 'ADVANCED', 600, NOW(), NOW(), true, 0, 0, 0)
ON CONFLICT (id) DO NOTHING;

-- 퀴즈에 태그 연결 (동적 태그 ID 조회)
INSERT INTO public.quiz_tags (quiz_id, tag_id)
SELECT quiz_id, tag_id FROM (
    VALUES 
        (9001, (SELECT id FROM public.tags WHERE name = '자바스크립트' LIMIT 1)),
        (9001, (SELECT id FROM public.tags WHERE name = '파이썬' LIMIT 1)),
        (9002, (SELECT id FROM public.tags WHERE name = '알고리즘' LIMIT 1)),
        (9002, (SELECT id FROM public.tags WHERE name = '데이터베이스' LIMIT 1)),
        (9003, (SELECT id FROM public.tags WHERE name = '시스템설계' LIMIT 1))
) AS quiz_tag_mappings(quiz_id, tag_id)
WHERE tag_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 각 퀴즈에 간단한 문제 추가
INSERT INTO public.questions (id, quiz_id, question_type, question_text, options, correct_answer, explanation, difficulty_level, points)
VALUES 
    (9001, 9001, 'MULTIPLE_CHOICE', '자바의 기본 자료형이 아닌 것은?', '["int", "float", "String", "boolean"]', 'String', 'String은 클래스이며 참조형입니다.', 'BEGINNER', 10),
    (9002, 9001, 'MULTIPLE_CHOICE', 'Java에서 객체를 생성하는 키워드는?', '["new", "create", "make", "instance"]', 'new', 'new 키워드를 사용하여 객체를 생성합니다.', 'BEGINNER', 10),
    
    (9003, 9002, 'MULTIPLE_CHOICE', '스프링 MVC의 컨트롤러를 지정하는 어노테이션은?', '["@Controller", "@Service", "@Repository", "@Component"]', '@Controller', '@Controller는 스프링 MVC에서 컨트롤러 역할을 하는 클래스에 사용됩니다.', 'INTERMEDIATE', 15),
    (9004, 9002, 'MULTIPLE_CHOICE', '스프링에서 의존성 주입을 위한 어노테이션이 아닌 것은?', '["@Autowired", "@Inject", "@Resource", "@Bean"]', '@Bean', '@Bean은 의존성 주입이 아닌 빈 등록을 위한 어노테이션입니다.', 'INTERMEDIATE', 15),
    
    (9005, 9003, 'MULTIPLE_CHOICE', '자바의 메모리 영역 중 객체가 생성되는 영역은?', '["Stack", "Heap", "Method Area", "PC Register"]', 'Heap', '객체는 Heap 영역에 생성됩니다.', 'ADVANCED', 20),
    (9006, 9003, 'MULTIPLE_CHOICE', '자바에서 Thread-safe한 컬렉션이 아닌 것은?', '["Vector", "Hashtable", "ArrayList", "ConcurrentHashMap"]', 'ArrayList', 'ArrayList는 Thread-safe하지 않습니다. Vector, Hashtable, ConcurrentHashMap은 Thread-safe합니다.', 'ADVANCED', 20)
ON CONFLICT (id) DO NOTHING;

-- 이 SQL을 실행한 후 확인할 사용자 및 퀴즈 정보 출력
SELECT 'test@example.com' as email, 'OAuth2 기반 테스트 계정' as info, 'TEST 제공자 사용' as provider_info;
SELECT id FROM public.quizzes WHERE creator_id = 9999 ORDER BY id LIMIT 10;
