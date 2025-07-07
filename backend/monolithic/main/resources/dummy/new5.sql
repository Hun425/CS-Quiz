-- 새로운 더미 데이터 추가 (데이터베이스 초급, 중급, 고급)

-- 1. 필요한 태그 확인 (데이터베이스)
-- '데이터베이스' 태그는 dummy_data.sql 에 이미 존재하므로 별도 생성 불필요

-- 2. 새로운 퀴즈 추가 (데이터베이스 초급, 중급, 고급 - 각 1개씩, 총 3개 퀴즈)

-- 관리자 ID 및 데이터베이스 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), db_tag AS (
    SELECT id FROM public.tags WHERE name = '데이터베이스' -- dummy_data.sql 에 정의된 태그 사용
),
-- 데이터베이스 퀴즈 생성
     inserted_db_quizzes AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 데이터베이스 초급
                 (NOW() - INTERVAL '2 days', NOW(), '데이터베이스 기본 개념 튼튼히', 'RDBMS/NoSQL, 기본 SQL, 키 등 필수 기초 지식을 점검합니다.', 'BEGINNER',
                  true, 10, 'REGULAR', 25, (SELECT id FROM admin_user), floor(random() * 80 + 20), random() * 30 + 60, floor(random() * 450 + 60), NULL),
                 -- 데이터베이스 중급
                 (NOW() - INTERVAL '1 day', NOW(), '데이터베이스 핵심 원리 및 SQL 활용', 'JOIN, 인덱스, 트랜잭션, 정규화 등 핵심 원리와 SQL 활용 능력을 평가합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 65 + 15), random() * 25 + 65, floor(random() * 380 + 50), NULL),
                 -- 데이터베이스 고급
                 (NOW(), NOW(), '데이터베이스 심화 및 성능 최적화', '인덱스 최적화, 격리 수준, 실행 계획, 락 등 성능 관련 심화 주제를 다룹니다.', 'ADVANCED',
                  true, 10, 'REGULAR', 45, (SELECT id FROM admin_user), floor(random() * 50 + 10), random() * 15 + 70, floor(random() * 300 + 40), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_db AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, dt.id FROM inserted_db_quizzes iq, db_tag dt WHERE iq.title LIKE '데이터베이스%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Database Quizzes and Tags inserted/linked successfully';


-- 3. 새로운 질문 추가 (데이터베이스 퀴즈별 10개씩, 총 30개)

-- 데이터베이스 초급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '데이터베이스 기본 개념 튼튼히' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '관계형 데이터베이스(RDBMS)의 특징으로 올바르지 않은 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '스키마 없이 자유롭게 데이터 저장 가능', '["데이터를 테이블 형태로 저장", "SQL을 사용하여 데이터 조작", "스키마 없이 자유롭게 데이터 저장 가능", "데이터 간의 관계 정의 가능"]'::jsonb, '관계형 데이터베이스는 미리 정의된 스키마(테이블 구조)에 따라 데이터를 저장하며, 스키마 없이 자유롭게 저장하는 것은 NoSQL의 특징 중 하나입니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 테이블에서 특정 행(Row)을 고유하게 식별하는 데 사용되는 키는?', 'MULTIPLE_CHOICE', 'BEGINNER', '기본 키 (Primary Key)', '["외래 키 (Foreign Key)", "후보 키 (Candidate Key)", "기본 키 (Primary Key)", "대체 키 (Alternate Key)"]'::jsonb, '기본 키(Primary Key)는 테이블 내 각 행을 유일하게 구분할 수 있는 식별자 역할을 합니다. NULL 값을 허용하지 않으며 중복될 수 없습니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL 문에서 테이블의 모든 컬럼을 조회할 때 사용하는 기호는?', 'MULTIPLE_CHOICE', 'BEGINNER', '*', '["%", "_", "*", "&"]'::jsonb, '별표(`*`)는 `SELECT` 문에서 해당 테이블의 모든 컬럼을 선택하라는 의미로 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL 문에서 특정 조건을 만족하는 행만 조회하기 위해 사용하는 절(Clause)은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'WHERE', '["FROM", "SELECT", "WHERE", "ORDER BY"]'::jsonb, '`WHERE` 절은 `SELECT`, `UPDATE`, `DELETE` 문에서 조건을 지정하여 특정 행들만 대상으로 작업하도록 합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '테이블에 새로운 행(데이터)을 추가하기 위해 사용하는 SQL 명령어는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'INSERT', '["CREATE", "UPDATE", "INSERT", "ADD"]'::jsonb, '`INSERT INTO` 문은 테이블에 새로운 데이터를 삽입하는 데 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '테이블의 기존 데이터를 수정하기 위해 사용하는 SQL 명령어는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'UPDATE', '["MODIFY", "UPDATE", "ALTER", "CHANGE"]'::jsonb, '`UPDATE` 문은 테이블 내 특정 행의 컬럼 값을 변경하는 데 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '테이블의 특정 행(데이터)을 삭제하기 위해 사용하는 SQL 명령어는?', 'MULTIPLE_CHOICE', 'BEGINNER', 'DELETE', '["DROP", "REMOVE", "DELETE", "TRUNCATE"]'::jsonb, '`DELETE FROM` 문은 테이블에서 특정 조건을 만족하는 행을 삭제합니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '다른 테이블의 기본 키(Primary Key)를 참조하여 테이블 간의 관계를 맺는 데 사용되는 키는?', 'MULTIPLE_CHOICE', 'BEGINNER', '외래 키 (Foreign Key)', '["기본 키 (Primary Key)", "외래 키 (Foreign Key)", "슈퍼 키 (Super Key)", "고유 키 (Unique Key)"]'::jsonb, '외래 키는 한 테이블의 컬럼이 다른 테이블의 기본 키 값을 참조하도록 하여, 두 테이블 간의 관계를 설정하고 데이터 무결성을 유지하는 데 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스에서 문자열 데이터를 저장하는 데 가장 일반적으로 사용되는 데이터 타입은?', 'MULTIPLE_CHOICE', 'BEGINNER', 'VARCHAR', '["INT", "DATE", "BOOLEAN", "VARCHAR"]'::jsonb, 'VARCHAR(Variable Character) 타입은 가변 길이의 문자열 데이터를 저장하는 데 널리 사용됩니다.', 5, 30, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'NoSQL 데이터베이스의 유형이 아닌 것은?', 'MULTIPLE_CHOICE', 'BEGINNER', '관계형 (Relational)', '["키-값 (Key-Value)", "문서 (Document)", "컬럼 패밀리 (Column Family)", "관계형 (Relational)"]'::jsonb, '키-값, 문서, 컬럼 패밀리, 그래프 등은 NoSQL 데이터베이스의 주요 유형입니다. 관계형은 RDBMS를 지칭합니다.', 5, 30, (SELECT id FROM quiz_info));

-- 데이터베이스 중급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '데이터베이스 핵심 원리 및 SQL 활용' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), 'SQL에서 두 테이블 간에 공통된 값을 가진 행들만 연결하여 조회하는 JOIN 유형은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'INNER JOIN', '["LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", "INNER JOIN"]'::jsonb, 'INNER JOIN은 두 테이블 모두에 조인 조건과 일치하는 데이터가 있는 행들만 결과로 반환합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '왼쪽 테이블의 모든 행과, 오른쪽 테이블에서 조인 조건에 맞는 행을 연결하여 조회하는 JOIN 유형은? (오른쪽 테이블에 일치하는 행이 없으면 NULL로 표시)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'LEFT JOIN (또는 LEFT OUTER JOIN)', '["INNER JOIN", "LEFT JOIN (또는 LEFT OUTER JOIN)", "RIGHT JOIN (또는 RIGHT OUTER JOIN)", "CROSS JOIN"]'::jsonb, 'LEFT JOIN은 왼쪽 테이블 기준으로 모든 행을 포함하고, 오른쪽 테이블에서는 조인 조건을 만족하는 행만 가져옵니다. 만족하는 행이 없으면 NULL 값을 채웁니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 인덱스(Index)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '테이블의 특정 컬럼 값을 빠르게 찾기 위한 자료구조', '["테이블의 모든 데이터를 복사해두는 것", "데이터 삽입(INSERT) 속도를 항상 향상시킴", "테이블의 특정 컬럼 값을 빠르게 찾기 위한 자료구조", "데이터베이스의 물리적 저장 공간을 줄이는 기술"]'::jsonb, '인덱스는 책의 색인과 같이 테이블의 특정 컬럼(들)의 값과 해당 데이터의 위치 정보를 미리 정렬하여 저장함으로써, 검색(SELECT) 쿼리의 성능을 크게 향상시키는 자료구조입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 트랜잭션(Transaction)의 ACID 속성 중, 트랜잭션이 성공적으로 완료되면 그 결과가 영구적으로 데이터베이스에 반영되어야 한다는 속성은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '지속성 (Durability)', '["원자성 (Atomicity)", "일관성 (Consistency)", "격리성 (Isolation)", "지속성 (Durability)"]'::jsonb, '지속성(Durability)은 성공적으로 커밋된 트랜잭션의 결과는 시스템 장애가 발생하더라도 손실되지 않고 영구적으로 유지되어야 함을 의미합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 정규화(Normalization)의 주요 목적으로 가장 적합한 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '데이터 중복을 최소화하고 데이터 무결성을 높이기 위해', '["쿼리 성능을 극대화하기 위해", "테이블 개수를 최소화하기 위해", "데이터 중복을 최소화하고 데이터 무결성을 높이기 위해", "모든 데이터를 하나의 큰 테이블에 저장하기 위해"]'::jsonb, '정규화는 데이터의 중복성을 제거하고 데이터 모델의 구조를 개선하여, 데이터 삽입/수정/삭제 시 발생할 수 있는 이상 현상(Anomaly)을 방지하고 데이터 무결성을 유지하는 것을 목표로 합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL에서 그룹화된 결과에 대한 조건을 지정하여 특정 그룹만 필터링하는 데 사용하는 절(Clause)은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'HAVING', '["WHERE", "GROUP BY", "HAVING", "ORDER BY"]'::jsonb, '`HAVING` 절은 `GROUP BY`로 그룹화된 결과에 대해 집계 함수(COUNT, SUM, AVG 등)를 이용한 조건을 적용하여, 특정 그룹들만 선택하는 데 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL 집계 함수(Aggregate Function)가 아닌 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'CONCAT', '["COUNT", "SUM", "AVG", "CONCAT"]'::jsonb, 'COUNT, SUM, AVG, MAX, MIN 등은 여러 행의 값을 바탕으로 하나의 결과값을 계산하는 집계 함수입니다. CONCAT은 문자열을 연결하는 함수입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '테이블에서 특정 컬럼에 중복된 값이 들어가지 않도록 보장하는 제약 조건은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'UNIQUE 제약 조건', '["PRIMARY KEY 제약 조건", "FOREIGN KEY 제약 조건", "CHECK 제약 조건", "UNIQUE 제약 조건"]'::jsonb, 'UNIQUE 제약 조건은 해당 컬럼(들)에 들어가는 모든 값이 고유해야 함을 보장합니다. PRIMARY KEY와 유사하지만 NULL 값을 허용할 수 있다는 차이가 있습니다(단, 여러 NULL 허용 여부는 DBMS마다 다름).', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '인덱스가 설정된 컬럼에 대한 데이터 수정(UPDATE) 또는 삭제(DELETE) 작업 시 발생할 수 있는 일은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '인덱스도 함께 수정되어야 하므로 성능 저하 발생 가능', '["항상 성능이 향상된다", "인덱스는 자동으로 삭제된다", "인덱스도 함께 수정되어야 하므로 성능 저하 발생 가능", "아무런 영향이 없다"]'::jsonb, '데이터가 수정되거나 삭제되면 해당 데이터와 연결된 인덱스 정보도 갱신되어야 합니다. 이 과정에서 추가적인 작업이 필요하므로 INSERT, UPDATE, DELETE 작업 시 인덱스가 없는 경우보다 성능이 저하될 수 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스에서 원자성(Atomicity)을 보장한다는 것은 무엇을 의미하는가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '트랜잭션 내의 모든 작업이 전부 성공하거나 전부 실패해야 한다', '["데이터는 항상 일관된 상태를 유지해야 한다", "동시에 실행되는 트랜잭션은 서로 영향을 주지 않아야 한다", "트랜잭션 내의 모든 작업이 전부 성공하거나 전부 실패해야 한다", "성공한 트랜잭션 결과는 영구적으로 저장되어야 한다"]'::jsonb, '원자성은 트랜잭션을 구성하는 모든 연산들이 하나의 논리적인 단위로 취급되어, 모두 성공적으로 실행되거나 아니면 모두 실행되지 않아야 함(All or Nothing)을 의미합니다.', 10, 45, (SELECT id FROM quiz_info));


-- 데이터베이스 고급 질문 (10개)
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '데이터베이스 심화 및 성능 최적화' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '데이터베이스 인덱스 전략 중, 쿼리에 필요한 모든 컬럼이 인덱스에 포함되어 있어 테이블 접근 없이 인덱스만으로 쿼리를 처리하는 인덱스는?', 'MULTIPLE_CHOICE', 'ADVANCED', '커버링 인덱스 (Covering Index)', '["클러스터형 인덱스 (Clustered Index)", "복합 인덱스 (Composite Index)", "커버링 인덱스 (Covering Index)", "전문 검색 인덱스 (Full-text Index)"]'::jsonb, '커버링 인덱스는 SELECT, WHERE, ORDER BY 등에 사용되는 모든 컬럼이 인덱스 자체에 포함되어 있어, 실제 테이블 데이터에 접근할 필요 없이 인덱스 스캔만으로 쿼리를 완료할 수 있게 하여 성능을 향상시킵니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '트랜잭션 격리 수준(Isolation Level)에서 "Repeatable Read"와 "Serializable"의 주요 차이점은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'Serializable은 팬텀 리드(Phantom Read)까지 방지한다', '["Repeatable Read는 Dirty Read를 허용한다", "Serializable은 Non-Repeatable Read를 허용한다", "Serializable은 팬텀 리드(Phantom Read)까지 방지한다", "Repeatable Read는 트랜잭션 시작 시 스냅샷을 사용하지 않는다"]'::jsonb, 'Repeatable Read는 한 트랜잭션 내에서 조회한 데이터의 일관성을 보장하지만(Non-Repeatable Read 방지), 다른 트랜잭션에 의해 새로운 행이 추가/삭제되어 결과 집합이 달라지는 팬텀 리드는 막지 못합니다. Serializable은 팬텀 리드까지 방지하여 가장 높은 격리 수준을 제공합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL 쿼리 성능 최적화 시, 데이터베이스 옵티마이저가 해당 쿼리를 어떻게 처리할 계획인지 보여주는 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '실행 계획 (Execution Plan)', '["인덱스 통계 (Index Statistics)", "테이블 스키마 (Table Schema)", "데이터베이스 로그 (Database Log)", "실행 계획 (Execution Plan)"]'::jsonb, '실행 계획은 특정 SQL 쿼리에 대해 데이터베이스 옵티마이저가 어떤 방식으로 테이블에 접근하고(예: 풀 테이블 스캔, 인덱스 스캔), 어떤 순서로 조인을 수행하며, 어떤 알고리즘을 사용할지 등을 상세하게 보여주는 처리 절차입니다. 이를 분석하여 쿼리 튜닝의 단서를 얻을 수 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '두 개 이상의 트랜잭션이 서로 상대방의 자원이 해제되기를 기다리며 무한정 대기하는 상태를 무엇이라고 하는가?', 'MULTIPLE_CHOICE', 'ADVANCED', '교착 상태 (Deadlock)', '["락 에스컬레이션 (Lock Escalation)", "경합 상태 (Race Condition)", "교착 상태 (Deadlock)", "무한 루프 (Infinite Loop)"]'::jsonb, '교착 상태는 둘 이상의 트랜잭션이 각각 점유한 리소스에 대한 락을 가진 채, 서로 상대방이 점유한 리소스의 락을 얻으려고 기다리는 상황에서 발생합니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스 복제(Replication)의 주된 목적이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '쓰기 성능 향상', '["고가용성 확보 (장애 대비)", "읽기 부하 분산", "데이터 백업", "쓰기 성능 향상"]'::jsonb, '복제는 주로 원본(Master) 서버의 데이터를 하나 이상의 복제(Slave/Replica) 서버에 복사하여, 원본 서버 장애 시 서비스 연속성을 보장하고(고가용성), 읽기 요청을 복제 서버로 분산시켜 부하를 줄이는 데 사용됩니다. 쓰기 작업은 여전히 원본 서버에서 처리되므로 쓰기 성능 향상이 주 목적은 아닙니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'SQL Injection 공격을 방어하는 방법으로 가장 효과적인 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '매개변수화된 쿼리(Prepared Statement) 사용', '["사용자 입력값에서 특수문자 제거", "웹 방화벽(WAF) 사용", "데이터베이스 접근 권한 최소화", "매개변수화된 쿼리(Prepared Statement) 사용"]'::jsonb, 'Prepared Statement는 사용자 입력을 SQL 쿼리 구조와 분리하여 처리하므로, 입력값이 SQL 문의 일부로 해석되는 것을 원천적으로 차단하여 SQL Injection 공격을 방지하는 가장 근본적이고 효과적인 방법입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'MVCC(Multi-Version Concurrency Control) 메커니즘에 대한 설명으로 올바른 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '데이터 변경 시 이전 버전 데이터를 함께 유지하여 읽기 작업이 쓰기 작업을 차단하지 않도록 함', '["모든 트랜잭션이 데이터를 읽기 전에 배타적 락(Exclusive Lock)을 획득", "데이터 변경 시 이전 버전 데이터를 함께 유지하여 읽기 작업이 쓰기 작업을 차단하지 않도록 함", "트랜잭션 시작 시 타임스탬프를 부여하고 순서대로 처리", "교착 상태 발생 시 자동으로 감지하고 해결"]'::jsonb, 'MVCC는 데이터 변경 시 새로운 버전을 생성하고 이전 버전의 데이터를 일정 기간 보존하여, 읽기 트랜잭션은 특정 시점의 버전을 조회하고 쓰기 트랜잭션은 새로운 버전을 생성함으로써 읽기와 쓰기가 서로 차단(Blocking) 없이 동시에 이루어지도록 하는 동시성 제어 기법입니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '대규모 읽기 작업이 많은 서비스에서 데이터베이스 부하를 줄이기 위해 자주 사용되는 캐싱(Caching) 전략은?', 'MULTIPLE_CHOICE', 'ADVANCED', '데이터베이스 앞단에 캐시 서버(예: Redis, Memcached) 운영', '["모든 테이블에 인덱스 추가", "데이터베이스 서버의 메모리 증설", "데이터베이스 앞단에 캐시 서버(예: Redis, Memcached) 운영", "쿼리 결과를 파일로 저장"]'::jsonb, '자주 조회되지만 변경 빈도가 낮은 데이터를 메모리 기반의 캐시 서버(Redis, Memcached 등)에 저장해두고, 요청이 오면 데이터베이스 대신 캐시에서 먼저 조회하여 응답 속도를 높이고 데이터베이스 부하를 줄일 수 있습니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '데이터베이스에서 특정 쿼리가 비효율적으로 동작할 때, 인덱스를 사용하지 못하는 일반적인 원인이 아닌 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', 'SELECT 절에 인덱스 컬럼 포함', '["인덱스 컬럼에 함수나 연산 적용 (예: WHERE function(col) = ?)", "데이터 분포도가 나빠 인덱스 사용 효과가 미미할 때", "WHERE 절 조건이 인덱스의 첫 번째 컬럼이 아닐 때 (복합 인덱스)", "SELECT 절에 인덱스 컬럼 포함"]'::jsonb, 'WHERE 절이나 JOIN 조건 등에서 인덱스 컬럼을 가공하거나(함수 사용 등), 복합 인덱스의 선행 컬럼 없이 후행 컬럼만 사용하거나, 데이터 분포도 문제로 옵티마이저가 인덱스를 타지 않기로 결정하는 등 다양한 이유가 있습니다. SELECT 절에 인덱스 컬럼이 포함되는 것 자체는 인덱스 사용 여부의 직접적인 원인이 아닙니다.', 15, 60, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'B-Tree 인덱스 구조에 대한 설명으로 적절하지 않은 것은?', 'MULTIPLE_CHOICE', 'ADVANCED', '해시 테이블(Hash Table) 구조를 사용한다', '["균형 잡힌 트리 구조로 모든 리프 노드는 같은 레벨에 있다", "데이터는 정렬된 상태로 저장된다", "검색, 삽입, 삭제 연산의 시간 복잡도가 평균적으로 O(log n)이다", "해시 테이블(Hash Table) 구조를 사용한다"]'::jsonb, 'B-Tree 인덱스는 자식 노드를 2개 이상 가질 수 있는 균형 잡힌 트리 구조입니다. 데이터는 키 값 기준으로 정렬되어 저장되며, 특정 값을 찾는 데 효율적입니다. 해시 테이블 구조를 사용하는 것은 해시 인덱스입니다.', 15, 60, (SELECT id FROM quiz_info));