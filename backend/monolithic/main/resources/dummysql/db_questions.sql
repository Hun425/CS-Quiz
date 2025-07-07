-- 데이터베이스 관련 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 5)
        WHEN 0 THEN 'SQL에서 DELETE와 TRUNCATE의 차이점은?'
        WHEN 1 THEN '정규화의 주요 목적은?'
        WHEN 2 THEN 'ACID 속성에 포함되지 않는 것은?'
        WHEN 3 THEN '다음 중 비관계형 데이터베이스는?'
        WHEN 4 THEN 'GROUP BY 절과 함께 사용할 수 없는 집계 함수는?'
        END,
    'MULTIPLE_CHOICE',
    q.difficulty_level,
    CASE mod(seq, 5)
        WHEN 0 THEN 'TRUNCATE는 롤백이 불가능하다'
        WHEN 1 THEN '데이터 중복 최소화'
        WHEN 2 THEN '확장성'
        WHEN 3 THEN 'MongoDB'
        WHEN 4 THEN 'TOP'
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '["DELETE는 조건절을 사용할 수 없다", "TRUNCATE는 인덱스를 삭제한다", "DELETE는 트리거를 발생시킨다", "TRUNCATE는 롤백이 불가능하다"]'::jsonb
        WHEN 1 THEN '["데이터 중복 최소화", "쿼리 성능 향상", "데이터베이스 크기 증가", "외래 키 제약 완화"]'::jsonb
        WHEN 2 THEN '["원자성", "일관성", "격리성", "확장성"]'::jsonb
        WHEN 3 THEN '["MySQL", "Oracle", "PostgreSQL", "MongoDB"]'::jsonb
        WHEN 4 THEN '["COUNT", "AVG", "TOP", "SUM"]'::jsonb
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN 'DELETE는 트랜잭션 로그에 각 행의 삭제를 기록하여 롤백이 가능하지만, TRUNCATE는 테이블의 데이터 페이지를 할당 해제하는 방식으로 빠르게 모든 데이터를 제거하며 로그를 거의 기록하지 않아 롤백이 불가능합니다.'
        WHEN 1 THEN '정규화의 주요 목적은 데이터 중복을 최소화하여 데이터 무결성을 유지하고 저장 공간을 효율적으로 사용하는 것입니다.'
        WHEN 2 THEN 'ACID 속성은 원자성(Atomicity), 일관성(Consistency), 격리성(Isolation), 지속성(Durability)입니다. 확장성(Scalability)은 ACID 속성에 포함되지 않습니다.'
        WHEN 3 THEN 'MongoDB는 문서 기반 NoSQL 데이터베이스입니다. MySQL, Oracle, PostgreSQL은 모두 관계형 데이터베이스입니다.'
        WHEN 4 THEN 'TOP은 SQL Server에서 결과 집합의 행 수를 제한하는 키워드로, 집계 함수가 아닙니다. COUNT, AVG, SUM은 집계 함수입니다.'
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
    CASE WHEN mod(seq, 10) = 8 THEN
             '-- 다음 SQL 쿼리의 결과는?
             SELECT department, COUNT(*) as emp_count
             FROM employees
             WHERE salary > 50000
             GROUP BY department
             HAVING COUNT(*) > 5
             ORDER BY emp_count DESC;'
         ELSE NULL END
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 30) >= 0 AND mod(seq, 30) < 5
LIMIT 100;