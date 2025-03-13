-- 운영체제 관련 문제 생성
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
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 24) >= 22
LIMIT 100;