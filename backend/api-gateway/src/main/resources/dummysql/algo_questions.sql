-- 알고리즘, 자료구조 관련 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 5)
        WHEN 0 THEN '시간 복잡도가 O(n log n)인 정렬 알고리즘은?'
        WHEN 1 THEN '이진 검색 트리에서 삽입 연산의 시간 복잡도는?'
        WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는?'
        WHEN 3 THEN '그래프 탐색에 사용되지 않는 알고리즘은?'
        WHEN 4 THEN '다음 중 그리디 알고리즘을 사용하는 문제는?'
        END,
    'MULTIPLE_CHOICE',
    q.difficulty_level,
    CASE mod(seq, 5)
        WHEN 0 THEN '퀵 정렬'
        WHEN 1 THEN 'O(log n)'
        WHEN 2 THEN 'O(n)'
        WHEN 3 THEN '삽입 정렬'
        WHEN 4 THEN '다익스트라 최단 경로'
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '["버블 정렬", "삽입 정렬", "퀵 정렬", "계수 정렬"]'::jsonb
        WHEN 1 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
        WHEN 2 THEN '["O(1)", "O(log n)", "O(n)", "O(n²)"]'::jsonb
        WHEN 3 THEN '["깊이 우선 탐색", "너비 우선 탐색", "다익스트라", "삽입 정렬"]'::jsonb
        WHEN 4 THEN '["최단 경로 찾기", "최소 신장 트리", "다익스트라 최단 경로", "0/1 배낭 문제"]'::jsonb
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '퀵 정렬, 병합 정렬, 힙 정렬은 평균적으로 O(n log n)의 시간 복잡도를 가집니다. 버블 정렬과 삽입 정렬은 O(n²)입니다.'
        WHEN 1 THEN '이진 검색 트리에서 삽입은 트리의 높이에 비례하며, 균형 잡힌 트리의 경우 O(log n)입니다.'
        WHEN 2 THEN '해시 테이블의 최악 시간 복잡도는 충돌이 많이 발생할 경우 O(n)입니다.'
        WHEN 3 THEN '삽입 정렬은 정렬 알고리즘이며, 그래프 탐색 알고리즘이 아닙니다. DFS, BFS, 다익스트라는 그래프 탐색 알고리즘입니다.'
        WHEN 4 THEN '다익스트라 알고리즘은 그리디 알고리즘의 대표적인 예입니다. 항상 현재 가장 최적인 경로를 선택합니다.'
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
    CASE WHEN mod(seq, 10) = 4 THEN
             '// 다음 알고리즘의 시간 복잡도는?
             function search(arr, target) {
                 let left = 0;
                 let right = arr.length - 1;

                 while (left <= right) {
                     let mid = Math.floor((left + right) / 2);
                     if (arr[mid] === target) return mid;
                     if (arr[mid] < target) left = mid + 1;
                     else right = mid - 1;
                 }
                 return -1;
             }'
         ELSE NULL END
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 12) >= 10
LIMIT 100;