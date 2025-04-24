-- 파이썬 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 5)
        WHEN 0 THEN '파이썬에서 리스트를 생성하는 올바른 방법은?'
        WHEN 1 THEN '파이썬에서 딕셔너리를 순회하는 올바른 방법은?'
        WHEN 2 THEN '파이썬의 리스트 컴프리헨션으로 올바르게 작성된 것은?'
        WHEN 3 THEN '파이썬에서 문자열을 포맷팅하는 방법 중 올바르지 않은 것은?'
        WHEN 4 THEN '파이썬 함수 정의에서 "*args"의 의미는?'
        END,
    'MULTIPLE_CHOICE',
    q.difficulty_level,
    CASE mod(seq, 5)
        WHEN 0 THEN 'my_list = [1, 2, 3]'
        WHEN 1 THEN 'for key, value in my_dict.items():'
        WHEN 2 THEN '[x ** 2 for x in range(10)]'
        WHEN 3 THEN 'print("Hello" + 123)'  -- 문제가 되는 f-string 예제를 다른 예제로 변경
        WHEN 4 THEN '가변 개수의 위치 인자를 받는 매개변수'
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN to_jsonb(ARRAY['my_list = [1, 2, 3]', 'my_list = list(1, 2, 3)', 'my_list = array(1, 2, 3)', 'my_list = (1, 2, 3)'])
        WHEN 1 THEN to_jsonb(ARRAY['for key, value in my_dict.items():', 'for key, value in my_dict:', 'for item in my_dict.items():', 'for key in my_dict:'])
        WHEN 2 THEN to_jsonb(ARRAY['[x ** 2 for x in range(10)]', '[for x in range(10): x ** 2]', '[x ** 2 in range(10)]', '[x for x ** 2 in range(10)]'])
        WHEN 3 THEN to_jsonb(ARRAY['print("Hello" + 123)', 'print("{0}".format("Hello"))', 'print("%s" % "Hello")', 'print("Hello" + str(123))'])  -- 중괄호 관련 예제를 모두 변경
        WHEN 4 THEN to_jsonb(ARRAY['가변 개수의 위치 인자를 받는 매개변수', '가변 개수의 키워드 인자를 받는 매개변수', '기본값이 있는 매개변수', '위치 인자만 받는 매개변수'])
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '파이썬에서 리스트는 대괄호([])를 사용하여 생성합니다. list()는 이터러블 객체에서 리스트를 생성하는 함수입니다.'
        WHEN 1 THEN '딕셔너리의 키와 값을 함께 순회하려면 items() 메서드를 사용해야 합니다.'
        WHEN 2 THEN '리스트 컴프리헨션은 [표현식 for 변수 in 이터러블] 형태로 작성합니다.'
        WHEN 3 THEN '파이썬에서는 문자열과 숫자를 직접 연결할 수 없습니다. 숫자를 문자열로 변환해야 합니다.'
        WHEN 4 THEN '*args는 가변 개수의 위치 인자를 받기 위한 구문입니다. 키워드 인자는 **kwargs로 받습니다.'
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
    CASE WHEN mod(seq, 10) = 2 THEN
             '# 다음 코드의 결과는?
             squares = [x ** 2 for x in range(5)]
             print(squares)'
         ELSE NULL END
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 12) >= 5 AND mod(seq, 12) < 10
LIMIT 100;