-- 웹개발 관련 문제 생성
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
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 24) >= 17 AND mod(seq, 24) < 22
LIMIT 100;