-- 새로운 더미 데이터 추가 (알고리즘 및 자료구조 심화)

-- 1. 필요한 태그 확인 (알고리즘, 자료구조)
-- '알고리즘', '자료구조' 태그는 dummy_data.sql 에 이미 존재하므로 별도 생성 불필요

-- 2. 새로운 퀴즈 추가 (알고리즘/자료구조 관련 5개 퀴즈)

-- 관리자 ID 및 관련 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), algo_tag AS (
    SELECT id FROM public.tags WHERE name = '알고리즘' -- dummy_data.sql 에 정의된 태그
), ds_tag AS (
    SELECT id FROM public.tags WHERE name = '자료구조' -- dummy_data.sql 에 정의된 태그
),
-- 알고리즘/자료구조 퀴즈 생성
     inserted_algo_ds_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 시간/공간 복잡도 분석 (중급)
                 (NOW() - INTERVAL '4 days', NOW(), '알고리즘 성능 분석: 복잡도 이해', 'Big O 표기법을 중심으로 알고리즘의 시간 및 공간 복잡도를 분석하고 비교합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 50 + 10), random() * 25 + 65, floor(random() * 300 + 30), NULL),
                 -- 주요 정렬 알고리즘 (중급)
                 (NOW() - INTERVAL '3 days', NOW(), '주요 정렬 알고리즘 비교 분석', '버블, 선택, 삽입, 병합, 퀵, 힙 정렬 등의 특징과 시간 복잡도를 비교합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 55 + 12), random() * 25 + 68, floor(random() * 330 + 35), NULL),
                 -- 주요 탐색 알고리즘 (중급)
                 (NOW() - INTERVAL '2 days', NOW(), '탐색 알고리즘: 검색과 순회', '선형 탐색, 이진 탐색, DFS, BFS 등 주요 탐색 및 순회 알고리즘을 다룹니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 60 + 14), random() * 28 + 66, floor(random() * 360 + 40), NULL),
                 -- 동적 프로그래밍 기초 (고급)
                 (NOW() - INTERVAL '1 day', NOW(), '동적 프로그래밍(DP) 첫걸음', 'DP의 기본 개념(최적 부분 구조, 중복 부분 문제)과 기초적인 문제 유형을 학습합니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 35 + 5), random() * 20 + 70, floor(random() * 220 + 25), NULL),
                 -- 주요 자료구조 특징 (중급)
                 (NOW(), NOW(), '핵심 자료구조: 특징과 활용', '배열, 연결 리스트, 스택, 큐, 해시 테이블, 트리 등의 특징과 활용 사례를 알아봅니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 65 + 16), random() * 26 + 67, floor(random() * 380 + 45), NULL)
             RETURNING id, title, difficulty_level -- 생성된 퀴즈 ID, 제목, 난이도 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_algo_ds AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             -- 알고리즘 관련 퀴즈 연결 (복잡도, 정렬, 탐색, DP)
             SELECT iq.id, at.id FROM inserted_algo_ds_quizzes iq, algo_tag at
             WHERE iq.title LIKE '%복잡도%' OR iq.title LIKE '%정렬%' OR iq.title LIKE '%탐색%' OR iq.title LIKE '%동적 프로그래밍%'
             UNION ALL
             -- 자료구조 관련 퀴즈 연결
             SELECT iq.id, dt.id FROM inserted_algo_ds_quizzes iq, ds_tag dt
             WHERE iq.title LIKE '%자료구조%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Algorithm/Data Structure Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (알고리즘/자료구조 퀴즈별 10개씩, 총 50개)

-- 시간/공간 복잡도 분석 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '알고리즘 성능 분석: 복잡도 이해' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id, code_snippet
)
VALUES
    (NOW(), NOW(), '알고리즘의 시간 복잡도를 나타내는 Big O 표기법은 무엇을 의미하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '입력 크기 증가에 따른 실행 시간의 증가율 (상한선)', '["알고리즘의 정확한 실행 시간", "알고리즘이 사용하는 메모리 공간 크기", "입력 크기 증가에 따른 실행 시간의 증가율 (상한선)", "알고리즘의 최선의 경우 실행 시간"]'::jsonb, 'Big O 표기법은 입력 크기 n이 증가할 때 알고리즘 실행 시간이 증가하는 비율의 상한선을 나타냅니다. 최악의 경우 성능을 나타내는 데 주로 사용됩니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '다음 중 시간 복잡도가 가장 낮은 (가장 효율적인) 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(1)', '["O(n²)", "O(n log n)", "O(n)", "O(1)"]'::jsonb, 'O(1)은 입력 크기와 상관없이 실행 시간이 일정한 상수 시간을 의미하며, 가장 효율적인 시간 복잡도입니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '정렬되지 않은 배열에서 특정 값을 찾는 선형 탐색(Linear Search)의 최악 시간 복잡도는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(n)', '["O(1)", "O(log n)", "O(n)", "O(n log n)"]'::jsonb, '선형 탐색은 배열의 처음부터 끝까지 하나씩 비교하므로, 최악의 경우(값이 마지막에 있거나 없을 때) 배열의 크기 n에 비례하는 시간이 걸립니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '정렬된 배열에서 특정 값을 찾는 이진 탐색(Binary Search)의 시간 복잡도는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(log n)', '["O(1)", "O(log n)", "O(n)", "O(n log n)"]'::jsonb, '이진 탐색은 매 단계마다 탐색 범위를 절반씩 줄여나가므로, 시간 복잡도는 O(log n)입니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '다음 코드 조각의 시간 복잡도는? (n은 배열의 크기)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(n²)', '["O(n)", "O(n log n)", "O(n²)", "O(n³)"]'::jsonb, '바깥쪽 루프가 n번 반복하고, 안쪽 루프도 평균적으로 n/2번 (최악 n번) 반복하므로 전체 시간 복잡도는 O(n * n) = O(n²)입니다.', 10, 45, (SELECT id FROM quiz_info), '// for (int i = 0; i < n; i++) { \n//   for (int j = i + 1; j < n; j++) { \n//     // some O(1) operation \n//   }\n// }'),
    (NOW(), NOW(), '알고리즘의 공간 복잡도(Space Complexity)는 무엇을 측정하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '알고리즘 실행 중 사용하는 총 메모리 공간', '["알고리즘의 실행 시간", "알고리즘의 코드 라인 수", "알고리즘 실행 중 사용하는 총 메모리 공간", "알고리즘의 입력 데이터 크기"]'::jsonb, '공간 복잡도는 알고리즘이 문제를 해결하기 위해 실행되는 동안 사용하는 메모리(변수, 자료구조 등)의 총량을 입력 크기 n의 함수로 나타낸 것입니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '재귀 함수(Recursive Function)를 사용할 때 주로 고려해야 할 공간 복잡도 요소는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '함수 호출 스택의 깊이', '["입력 배열의 크기", "반복문의 횟수", "함수 호출 스택의 깊이", "힙 메모리 사용량"]'::jsonb, '재귀 함수는 호출될 때마다 함수 호출 정보가 콜 스택에 쌓입니다. 따라서 재귀의 깊이가 깊어질수록 스택 공간을 많이 사용하게 되어 공간 복잡도에 영향을 줍니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), 'O(n log n) 시간 복잡도를 가지는 대표적인 정렬 알고리즘이 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '삽입 정렬 (Insertion Sort)', '["병합 정렬 (Merge Sort)", "힙 정렬 (Heap Sort)", "퀵 정렬 (Quick Sort - 평균)", "삽입 정렬 (Insertion Sort)"]'::jsonb, '병합 정렬, 힙 정렬, 퀵 정렬(평균)은 O(n log n)의 시간 복잡도를 가집니다. 삽입 정렬의 평균 및 최악 시간 복잡도는 O(n²)입니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '알고리즘의 성능 분석에서 최선(Best Case), 평균(Average Case), 최악(Worst Case) 경우를 모두 고려하는 이유는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '입력 데이터의 특성에 따라 성능이 크게 달라질 수 있기 때문', '["알고리즘의 버그를 찾기 위해", "코드의 가독성을 높이기 위해", "입력 데이터의 특성에 따라 성능이 크게 달라질 수 있기 때문", "알고리즘 구현 난이도를 평가하기 위해"]'::jsonb, '동일한 알고리즘이라도 입력 데이터의 상태(정렬 여부, 특정 값의 분포 등)에 따라 실행 시간이 크게 달라질 수 있으므로, 다양한 경우를 분석하여 알고리즘의 실질적인 성능 특성을 파악합니다.', 10, 45, (SELECT id FROM quiz_info), NULL),
    (NOW(), NOW(), '시간 복잡도 O(n)과 O(n log n) 중 일반적으로 더 성능이 좋은 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(n)', '["O(n)", "O(n log n)", "입력 크기에 따라 다르다", "항상 동일하다"]'::jsonb, '충분히 큰 입력 크기 n에 대해, n 보다는 n log n 이 더 큰 값을 가집니다. 따라서 O(n)이 O(n log n)보다 더 빠른(성능이 좋은) 시간 복잡도입니다.', 10, 45, (SELECT id FROM quiz_info), NULL);

-- 주요 정렬 알고리즘 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '주요 정렬 알고리즘 비교 분석' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '서로 인접한 원소들을 비교하여 자리를 교환하는 방식으로 정렬하는 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '버블 정렬 (Bubble Sort)', '["선택 정렬 (Selection Sort)", "삽입 정렬 (Insertion Sort)", "버블 정렬 (Bubble Sort)", "퀵 정렬 (Quick Sort)"]'::jsonb, '버블 정렬은 배열의 처음부터 끝까지 인접한 두 원소를 비교하며, 조건에 맞지 않으면 자리를 바꾸는 과정을 반복합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '배열에서 최소값(또는 최대값)을 찾아 정렬되지 않은 부분의 첫 번째 원소와 교환하는 과정을 반복하는 정렬 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '선택 정렬 (Selection Sort)', '["선택 정렬 (Selection Sort)", "삽입 정렬 (Insertion Sort)", "병합 정렬 (Merge Sort)", "힙 정렬 (Heap Sort)"]'::jsonb, '선택 정렬은 매 단계마다 정렬되지 않은 부분에서 가장 작은 원소를 찾아 정렬된 부분의 다음 위치로 옮깁니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '정렬된 부분과 정렬되지 않은 부분으로 나누어, 정렬되지 않은 부분의 원소를 하나씩 꺼내 정렬된 부분의 적절한 위치에 삽입하는 방식의 정렬 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '삽입 정렬 (Insertion Sort)', '["버블 정렬 (Bubble Sort)", "선택 정렬 (Selection Sort)", "삽입 정렬 (Insertion Sort)", "퀵 정렬 (Quick Sort)"]'::jsonb, '삽입 정렬은 이미 정렬된 부분 배열에 새로운 원소를 올바른 위치에 삽입해 나가며 정렬을 완성합니다. 이미 정렬된 데이터에 대해서는 매우 효율적입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '분할 정복(Divide and Conquer) 방식을 사용하여 배열을 절반씩 나누어 정렬한 후 병합하는 과정을 재귀적으로 수행하는 정렬 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '병합 정렬 (Merge Sort)', '["퀵 정렬 (Quick Sort)", "병합 정렬 (Merge Sort)", "힙 정렬 (Heap Sort)", "쉘 정렬 (Shell Sort)"]'::jsonb, '병합 정렬은 안정 정렬(Stable Sort)이면서 항상 O(n log n)의 시간 복잡도를 보장하는 대표적인 분할 정복 기반 정렬 알고리즘입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '피벗(Pivot) 원소를 기준으로 배열을 두 부분으로 분할하고, 각 부분을 재귀적으로 정렬하는 분할 정복 방식의 정렬 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '퀵 정렬 (Quick Sort)', '["병합 정렬 (Merge Sort)", "퀵 정렬 (Quick Sort)", "힙 정렬 (Heap Sort)", "기수 정렬 (Radix Sort)"]'::jsonb, '퀵 정렬은 평균적으로 O(n log n)의 빠른 성능을 보이지만, 피벗 선택 방식에 따라 최악의 경우 O(n²)의 시간 복잡도를 가질 수 있습니다. 일반적으로 불안정 정렬입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '힙(Heap) 자료구조를 이용하여 정렬하는 알고리즘으로, 항상 O(n log n)의 시간 복잡도를 가지는 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '힙 정렬 (Heap Sort)', '["삽입 정렬 (Insertion Sort)", "병합 정렬 (Merge Sort)", "퀵 정렬 (Quick Sort)", "힙 정렬 (Heap Sort)"]'::jsonb, '힙 정렬은 배열을 최대 힙(Max Heap) 또는 최소 힙(Min Heap)으로 구성한 후, 루트 노드(가장 큰 값 또는 가장 작은 값)를 꺼내 정렬된 부분으로 옮기는 과정을 반복합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '안정 정렬(Stable Sort)의 특징으로 올바른 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '중복된 값들의 기존 상대적 순서가 정렬 후에도 유지된다', '["항상 가장 빠른 정렬 속도를 보장한다", "추가적인 메모리 공간을 사용하지 않는다", "중복된 값들의 기존 상대적 순서가 정렬 후에도 유지된다", "역순으로 정렬된 데이터에 가장 효율적이다"]'::jsonb, '안정 정렬은 동일한 값을 가지는 원소들이 정렬 전의 순서를 정렬 후에도 그대로 유지하는 정렬 방식입니다. (예: 병합 정렬, 버블 정렬, 삽입 정렬)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '퀵 정렬(Quick Sort)의 성능에 가장 큰 영향을 미치는 요소는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '피벗(Pivot) 선택 방식', '["배열의 초기 정렬 상태", "재귀 호출의 깊이 제한", "피벗(Pivot) 선택 방식", "사용하는 프로그래밍 언어"]'::jsonb, '피벗을 어떻게 선택하느냐에 따라 분할이 균형적으로 이루어질 수도, 한쪽으로 치우칠 수도 있습니다. 불균형한 분할은 퀵 정렬의 성능을 O(n²)으로 저하시킬 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '다음 중 평균 시간 복잡도가 O(n²)인 정렬 알고리즘은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '선택 정렬 (Selection Sort)', '["병합 정렬 (Merge Sort)", "힙 정렬 (Heap Sort)", "선택 정렬 (Selection Sort)", "퀵 정렬 (Quick Sort)"]'::jsonb, '선택 정렬, 삽입 정렬, 버블 정렬은 평균적으로 O(n²)의 시간 복잡도를 가집니다. 병합, 힙, 퀵(평균)은 O(n log n)입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '정렬 알고리즘 선택 시 고려해야 할 요소가 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '알고리즘 개발자의 국적', '["데이터의 양과 초기 상태", "시간 및 공간 복잡도", "정렬의 안정성(Stable) 필요 여부", "알고리즘 개발자의 국적"]'::jsonb, '데이터의 특성(크기, 정렬 상태), 요구되는 성능(시간/공간), 안정성 필요 여부, 구현의 용이성 등을 종합적으로 고려하여 적절한 정렬 알고리즘을 선택해야 합니다.', 10, 45, (SELECT id FROM quiz_info));

-- 주요 탐색 알고리즘 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '탐색 알고리즘: 검색과 순회' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '정렬되지 않은 리스트에서 특정 원소를 찾을 때 가장 간단하게 사용할 수 있는 탐색 방법은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '선형 탐색 (Linear Search)', '["이진 탐색 (Binary Search)", "해시 탐색 (Hash Search)", "선형 탐색 (Linear Search)", "점프 탐색 (Jump Search)"]'::jsonb, '선형 탐색은 리스트의 처음부터 끝까지 순차적으로 원소를 비교하며 찾는 가장 기본적인 탐색 방법입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '이진 탐색(Binary Search)을 사용하기 위한 필수 전제 조건은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '데이터가 정렬되어 있어야 한다', '["데이터가 연결 리스트 구조여야 한다", "데이터가 해시 테이블에 저장되어 있어야 한다", "데이터가 정렬되어 있어야 한다", "데이터의 크기가 2의 거듭제곱이어야 한다"]'::jsonb, '이진 탐색은 탐색 범위를 절반씩 줄여나가기 때문에, 데이터가 반드시 정렬된 상태여야 올바르게 동작합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '그래프 탐색 방법 중 하나로, 시작 노드에서 갈 수 있는 가장 깊은 노드까지 우선적으로 탐색하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '깊이 우선 탐색 (DFS, Depth-First Search)', '["너비 우선 탐색 (BFS, Breadth-First Search)", "깊이 우선 탐색 (DFS, Depth-First Search)", "다익스트라 알고리즘 (Dijkstra Algorithm)", "A* 알고리즘"]'::jsonb, 'DFS는 스택(Stack) 자료구조 또는 재귀 함수를 이용하여 구현하며, 한 경로를 최대한 깊게 탐색한 후 막다른 길에 도달하면 이전 갈림길로 돌아와 다른 경로를 탐색합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '그래프 탐색 방법 중 하나로, 시작 노드에서 가까운 노드들부터 우선적으로 탐색하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '너비 우선 탐색 (BFS, Breadth-First Search)', '["너비 우선 탐색 (BFS, Breadth-First Search)", "깊이 우선 탐색 (DFS, Depth-First Search)", "벨만-포드 알고리즘 (Bellman-Ford Algorithm)", "플로이드-워셜 알고리즘 (Floyd-Warshall Algorithm)"]'::jsonb, 'BFS는 큐(Queue) 자료구조를 이용하여 구현하며, 시작 노드와 인접한 노드들을 먼저 모두 방문한 후, 그 다음 레벨의 노드들을 방문하는 방식으로 탐색합니다. 최단 경로 찾기에 자주 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '깊이 우선 탐색(DFS) 구현에 주로 사용되는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '스택 (Stack)', '["큐 (Queue)", "힙 (Heap)", "스택 (Stack)", "해시 테이블 (Hash Table)"]'::jsonb, 'DFS는 가장 최근에 방문한 노드의 인접 노드를 탐색해야 하므로 후입선출(LIFO) 특성의 스택이나 재귀 호출 스택을 사용합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '너비 우선 탐색(BFS) 구현에 주로 사용되는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '큐 (Queue)', '["큐 (Queue)", "스택 (Stack)", "우선순위 큐 (Priority Queue)", "연결 리스트 (Linked List)"]'::jsonb, 'BFS는 현재 레벨의 노드를 모두 방문한 후 다음 레벨로 넘어가야 하므로 선입선출(FIFO) 특성의 큐를 사용합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '이진 트리(Binary Tree)의 순회 방법 중 "루트 - 왼쪽 서브트리 - 오른쪽 서브트리" 순서로 방문하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '전위 순회 (Pre-order Traversal)', '["중위 순회 (In-order Traversal)", "후위 순회 (Post-order Traversal)", "전위 순회 (Pre-order Traversal)", "레벨 순서 순회 (Level-order Traversal)"]'::jsonb, '전위 순회는 트리를 복사하거나 폴더 구조를 표현할 때 등에 사용될 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '이진 트리(Binary Tree)의 순회 방법 중 "왼쪽 서브트리 - 루트 - 오른쪽 서브트리" 순서로 방문하는 방식은? (이진 검색 트리의 경우 오름차순 정렬 결과)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '중위 순회 (In-order Traversal)', '["중위 순회 (In-order Traversal)", "후위 순회 (Post-order Traversal)", "전위 순회 (Pre-order Traversal)", "레벨 순서 순회 (Level-order Traversal)"]'::jsonb, '중위 순회는 이진 검색 트리(BST)에서 사용할 경우 노드의 값들이 오름차순으로 정렬되어 출력됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '이진 트리(Binary Tree)의 순회 방법 중 "왼쪽 서브트리 - 오른쪽 서브트리 - 루트"" 순서로 방문하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '후위 순회 (Post-order Traversal)', '["중위 순회 (In-order Traversal)", "후위 순회 (Post-order Traversal)", "전위 순회 (Pre-order Traversal)", "레벨 순서 순회 (Level-order Traversal)"]'::jsonb, '후위 순회는 트리의 리프 노드부터 방문하게 되며, 폴더 용량 계산이나 트리를 삭제할 때 등에 사용될 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '해시 테이블(Hash Table)에서의 탐색 연산의 평균 시간 복잡도는? (충돌이 적절히 관리될 경우)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'O(1)', '["O(1)", "O(log n)", "O(n)", "O(n log n)"]'::jsonb, '해시 함수를 통해 키에 해당하는 값을 바로 찾을 수 있으므로, 이상적인 경우(충돌 없음) 또는 충돌이 잘 분산된 경우 평균적으로 O(1)의 매우 빠른 탐색 속도를 보입니다.', 10, 45, (SELECT id FROM quiz_info));

-- 동적 프로그래밍 기초 (고급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '동적 프로그래밍(DP) 첫걸음' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '동적 프로그래밍(Dynamic Programming, DP)이 적용 가능한 문제의 두 가지 핵심 특징은?', 'MULTIPLE_CHOICE', 'ADVANCED', '최적 부분 구조(Optimal Substructure)와 중복 부분 문제(Overlapping Subproblems)', '["탐욕적 선택 속성(Greedy Choice Property)과 순환 구조(Cyclic Structure)", "최적 부분 구조(Optimal Substructure)와 중복 부분 문제(Overlapping Subproblems)", "분할 정복(Divide and Conquer)과 상호 배타적 부분 문제(Mutually Exclusive Subproblems)", "선형 구조(Linear Structure)와 독립적 부분 문제(Independent Subproblems)"]'::jsonb, 'DP는 큰 문제의 최적 해결책이 작은 문제들의 최적 해결책으로 구성될 수 있고(최적 부분 구조), 동일한 작은 문제들이 반복적으로 나타나는(중복 부분 문제) 특징을 가진 문제에 효과적입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '동적 프로그래밍의 "중복 부분 문제(Overlapping Subproblems)" 특징을 해결하기 위해 사용하는 기법은?', 'MULTIPLE_CHOICE', 'ADVANCED', '메모이제이션(Memoization) 또는 타뷸레이션(Tabulation)', '["분할 정복(Divide and Conquer)", "재귀 호출(Recursive Call)", "메모이제이션(Memoization) 또는 타뷸레이션(Tabulation)", "그리디 알고리즘(Greedy Algorithm)"]'::jsonb, '반복적으로 계산되는 작은 문제의 결과를 저장해두었다가(메모이제이션 또는 타뷸레이션), 다시 필요할 때 계산 없이 가져와 사용하여 중복 계산을 피하고 효율성을 높입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '동적 프로그래밍 기법 중, 재귀 호출을 사용하면서 계산 결과를 저장하여 중복 계산을 피하는 하향식(Top-down) 접근 방식은?', 'MULTIPLE_CHOICE', 'ADVANCED', '메모이제이션 (Memoization)', '["타뷸레이션 (Tabulation)", "메모이제이션 (Memoization)", "반복적 심화 (Iterative Deepening)", "백트래킹 (Backtracking)"]'::jsonb, '메모이제이션은 큰 문제에서 시작하여 작은 문제로 내려가면서 재귀적으로 해결하되, 이미 해결한 작은 문제의 결과는 저장해두고 재활용하는 방식입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '동적 프로그래밍 기법 중, 가장 작은 부분 문제부터 시작하여 반복문을 통해 점진적으로 해를 구축해 나가는 상향식(Bottom-up) 접근 방식은?', 'MULTIPLE_CHOICE', 'ADVANCED', '타뷸레이션 (Tabulation)', '["타뷸레이션 (Tabulation)", "메모이제이션 (Memoization)", "분할 정복 (Divide and Conquer)", "유전 알고리즘 (Genetic Algorithm)"]'::jsonb, '타뷸레이션은 보통 반복문을 사용하여 가장 작은 문제의 해부터 테이블(배열 등)에 채워나가면서 최종적으로 원하는 큰 문제의 해를 구하는 방식입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '피보나치 수열을 동적 프로그래밍으로 계산할 때, F(n) = F(n-1) + F(n-2) 와 같은 식을 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'ADVANCED', '점화식 (Recurrence Relation)', '["알고리즘 (Algorithm)", "자료 구조 (Data Structure)", "점화식 (Recurrence Relation)", "시간 복잡도 (Time Complexity)"]'::jsonb, '점화식은 어떤 수열이나 함수의 항이 이전 항들과의 관계를 통해 정의되는 식을 말합니다. 동적 프로그래밍에서는 문제의 상태 간 관계를 점화식으로 표현하는 것이 중요합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '0/1 배낭 문제(0/1 Knapsack Problem)는 어떤 알고리즘 패러다임을 사용하여 최적해를 찾는 대표적인 문제인가?', 'MULTIPLE_CHOICE', 'ADVANCED', '동적 프로그래밍 (Dynamic Programming)', '["그리디 알고리즘 (Greedy Algorithm)", "분할 정복 (Divide and Conquer)", "동적 프로그래밍 (Dynamic Programming)", "백트래킹 (Backtracking)"]'::jsonb, '0/1 배낭 문제는 각 물건을 넣거나 넣지 않는 경우를 고려하여, 배낭 용량 한도 내에서 최대 가치를 얻는 조합을 찾는 문제입니다. 이는 최적 부분 구조와 중복 부분 문제 특징을 만족하여 DP로 해결할 수 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '두 문자열 사이의 가장 긴 공통된 부분 문자열(Longest Common Subsequence, LCS) 길이를 구하는 문제는 어떤 알고리즘으로 해결할 수 있는가?', 'MULTIPLE_CHOICE', 'ADVANCED', '동적 프로그래밍 (Dynamic Programming)', '["문자열 매칭 알고리즘 (KMP 등)", "정렬 알고리즘", "동적 프로그래밍 (Dynamic Programming)", "그래프 탐색 알고리즘"]'::jsonb, 'LCS 문제는 두 문자열의 각 부분 문자열에 대한 LCS 길이를 계산하고 이를 테이블에 저장하여, 최종적으로 전체 문자열의 LCS 길이를 구하는 DP 방식으로 해결 가능합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '동적 프로그래밍으로 문제를 해결할 때, 상태(State)를 정의하는 것이 중요한 이유는?', 'MULTIPLE_CHOICE', 'ADVANCED', '부분 문제의 해를 저장하고 재사용하기 위한 기준이 되기 때문', '["알고리즘의 시간 복잡도를 낮추기 위해", "코드의 가독성을 높이기 위해", "부분 문제의 해를 저장하고 재사용하기 위한 기준이 되기 때문", "그리디 선택 속성을 확인하기 위해"]'::jsonb, 'DP에서는 문제의 상태를 명확히 정의해야 해당 상태(부분 문제)에 대한 최적해를 계산하고 저장(메모이제이션 또는 타뷸레이션)할 수 있습니다. 상태 정의는 점화식을 세우는 데 필수적입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '그리디 알고리즘(Greedy Algorithm)과 동적 프로그래밍(DP)의 주요 차이점은?', 'MULTIPLE_CHOICE', 'ADVANCED', '그리디는 매 순간 최적의 선택을 하지만, DP는 모든 가능성을 고려하여 최적해를 찾는다', '["그리디는 항상 최적해를 보장하지만, DP는 근사치를 찾는다", "그리디는 매 순간 최적의 선택을 하지만, DP는 모든 가능성을 고려하여 최적해를 찾는다", "그리디는 하향식, DP는 상향식 접근만 사용한다", "그리디는 재귀를 사용하고, DP는 반복문을 사용한다"]'::jsonb, '그리디 알고리즘은 각 단계에서 당장의 최적이라고 생각되는 선택을 해나가지만, 그것이 전체적으로 최적해임을 보장하지는 않습니다. 반면 DP는 작은 부분 문제들의 최적해를 바탕으로 전체 문제의 최적해를 구합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '메모이제이션(Memoization) 방식 DP의 장점은?', 'MULTIPLE_CHOICE', 'ADVANCED', '실제로 필요한 부분 문제만 계산한다', '["반복문 구현이 항상 더 간결하다", "함수 호출 스택을 사용하지 않는다", "실제로 필요한 부분 문제만 계산한다", "타뷸레이션보다 항상 공간 효율성이 좋다"]'::jsonb, '메모이제이션은 재귀 호출을 통해 필요한 부분 문제만 계산하게 되므로, 타뷸레이션 방식처럼 모든 부분 문제의 해를 미리 계산할 필요가 없어 특정 경우에는 더 효율적일 수 있습니다.', 15, 60, (SELECT id FROM quiz_info));

-- 주요 자료구조 특징 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '핵심 자료구조: 특징과 활용' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '데이터를 순차적으로 저장하며, 인덱스를 통해 특정 위치의 원소에 O(1) 시간으로 접근 가능한 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '배열 (Array)', '["연결 리스트 (Linked List)", "배열 (Array)", "해시 테이블 (Hash Table)", "스택 (Stack)"]'::jsonb, '배열은 메모리 상에 연속적으로 데이터를 저장하므로, 인덱스를 이용한 임의 접근(Random Access) 속도가 매우 빠릅니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터 삽입/삭제 시(특히 중간 위치) 배열(Array)에 비해 일반적으로 더 효율적인 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '연결 리스트 (Linked List)', '["배열 (Array)", "연결 리스트 (Linked List)", "힙 (Heap)", "큐 (Queue)"]'::jsonb, '연결 리스트는 각 노드가 다음 노드를 가리키는 포인터를 가지므로, 중간 삽입/삭제 시 해당 노드의 포인터만 변경하면 되어 O(1) 시간이 걸립니다(단, 해당 위치 탐색 시간 별도). 배열은 삽입/삭제 시 원소들을 이동시켜야 해서 O(n)이 걸릴 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '후입선출(LIFO, Last-In First-Out) 방식으로 데이터가 처리되는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '스택 (Stack)', '["큐 (Queue)", "스택 (Stack)", "덱 (Deque)", "우선순위 큐 (Priority Queue)"]'::jsonb, '스택은 가장 마지막에 삽입된 데이터가 가장 먼저 제거되는 구조입니다. 함수 호출 스택, 뒤로 가기 기능 등에 활용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '선입선출(FIFO, First-In First-Out) 방식으로 데이터가 처리되는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '큐 (Queue)', '["스택 (Stack)", "큐 (Queue)", "트리 (Tree)", "그래프 (Graph)"]'::jsonb, '큐는 가장 먼저 삽입된 데이터가 가장 먼저 제거되는 구조입니다. 대기열 관리, 너비 우선 탐색(BFS) 등에 활용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '키(Key)와 값(Value)을 쌍으로 저장하며, 키를 통해 값을 빠르게(평균 O(1)) 검색, 삽입, 삭제할 수 있는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '해시 테이블 (Hash Table)', '["배열 (Array)", "연결 리스트 (Linked List)", "해시 테이블 (Hash Table)", "이진 검색 트리 (Binary Search Tree)"]'::jsonb, '해시 테이블은 해시 함수를 사용하여 키를 배열의 인덱스로 변환하여 값을 저장하므로, 평균적으로 매우 빠른 데이터 접근 속도를 제공합니다. (Python의 dictionary, Java의 HashMap 등)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '해시 테이블에서 서로 다른 키가 해시 함수에 의해 같은 인덱스로 매핑되는 현상을 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '해시 충돌 (Hash Collision)', '["오버플로우 (Overflow)", "언더플로우 (Underflow)", "해시 충돌 (Hash Collision)", "키 중복 (Key Duplication)"]'::jsonb, '해시 충돌은 해시 테이블의 성능 저하를 유발할 수 있으며, 이를 해결하기 위해 체이닝(Chaining)이나 개방 주소법(Open Addressing) 등의 기법이 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '계층적인 데이터를 표현하는 데 적합하며, 부모-자식 관계를 가지는 노드들로 구성된 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '트리 (Tree)', '["그래프 (Graph)", "스택 (Stack)", "큐 (Queue)", "트리 (Tree)"]'::jsonb, '트리는 파일 시스템 디렉토리 구조, 조직도, DOM(Document Object Model) 등 계층 구조를 나타내는 데 널리 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '이진 검색 트리(Binary Search Tree, BST)의 특징으로 올바른 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '모든 노드의 왼쪽 서브트리 값은 노드 값보다 작고, 오른쪽 서브트리 값은 노드 값보다 크다', '["모든 노드는 최대 2개의 자식 노드를 가진다", "루트 노드의 값이 항상 가장 크다", "모든 리프 노드는 같은 레벨에 있다", "모든 노드의 왼쪽 서브트리 값은 노드 값보다 작고, 오른쪽 서브트리 값은 노드 값보다 크다"]'::jsonb, '이진 검색 트리는 이 속성을 이용하여 효율적인 검색(평균 O(log n))이 가능하도록 설계된 트리 구조입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '정점(Vertex)들과 이들을 연결하는 간선(Edge)들로 구성되어, 네트워크 연결망, 소셜 네트워크 관계 등을 표현하는 데 사용되는 자료구조는?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '그래프 (Graph)', '["트리 (Tree)", "그래프 (Graph)", "해시 테이블 (Hash Table)", "힙 (Heap)"]'::jsonb, '그래프는 노드(정점)와 이들 사이의 관계(간선)를 표현하는 범용적인 자료구조입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '완전 이진 트리(Complete Binary Tree)의 일종으로, 최대값 또는 최소값을 빠르게(O(log n)) 찾고 삽입/삭제할 수 있는 자료구조는?',
     'MULTIPLE_CHOICE',
     'INTERMEDIATE',
     '힙 (Heap)',
     to_jsonb(ARRAY[
         '우선순위 큐 (Priority Queue)는 힙으로 구현될 수 있다',
         '힙 (Heap)',
         '이진 검색 트리 (Binary Search Tree)',
         '균형 이진 트리 (AVL Tree)'
         ]),
     '힙은 부모 노드의 값이 항상 자식 노드의 값보다 크거나 같은(최대 힙) 또는 작거나 같은(최소 힙) 속성을 만족하는 트리 기반 자료구조입니다. 우선순위 큐 구현에 주로 사용됩니다.',
     10, 45,
     (SELECT id FROM quiz_info));