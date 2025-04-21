-- 자바스크립트 문제 생성
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
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 12) < 5
LIMIT 100;