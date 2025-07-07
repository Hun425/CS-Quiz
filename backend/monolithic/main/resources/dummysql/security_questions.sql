-- 보안 관련 문제 생성
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
SELECT
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    CASE mod(seq, 5)
        WHEN 0 THEN 'SQL 인젝션 공격을 방지하는 가장 좋은 방법은?'
        WHEN 1 THEN '대칭 암호화와 비대칭 암호화의 주요 차이점은?'
        WHEN 2 THEN 'CSRF 공격이란?'
        WHEN 3 THEN '다음 중 가장 안전한 비밀번호 해싱 알고리즘은?'
        WHEN 4 THEN 'HTTPS에서 사용하는 프로토콜은?'
        END,
    'MULTIPLE_CHOICE',
    q.difficulty_level,
    CASE mod(seq, 5)
        WHEN 0 THEN '매개변수화된 쿼리 사용'
        WHEN 1 THEN '비대칭 암호화는 두 개의 다른 키를 사용한다'
        WHEN 2 THEN '사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격'
        WHEN 3 THEN 'bcrypt'
        WHEN 4 THEN 'TLS(SSL)'
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '["매개변수화된 쿼리 사용", "모든 입력 필드 숨기기", "쿼리 로깅 비활성화", "데이터베이스 계정 권한 축소"]'::jsonb
        WHEN 1 THEN '["비대칭 암호화는 두 개의 다른 키를 사용한다", "대칭 암호화가 항상 더 안전하다", "비대칭 암호화는 항상 더 빠르다", "대칭 암호화는 키가 필요 없다"]'::jsonb
        WHEN 2 THEN '["사용자의 세션을 훔치는 공격", "사용자가 자신의 의도와 다른 요청을 서버에 보내도록 속이는 공격", "데이터베이스에서 민감한 정보를 추출하는 공격", "네트워크 트래픽을 감청하는 공격"]'::jsonb
        WHEN 3 THEN '["MD5", "SHA-1", "bcrypt", "Base64"]'::jsonb
        WHEN 4 THEN '["FTP", "SSH", "TLS(SSL)", "SMTP"]'::jsonb
        END,
    CASE mod(seq, 5)
        WHEN 0 THEN '매개변수화된 쿼리(Prepared Statements)는 사용자 입력을 SQL 쿼리와 분리하여 처리하므로 SQL 인젝션 공격을 방지하는 가장 효과적인 방법입니다.'
        WHEN 1 THEN '대칭 암호화는 암호화와 복호화에 동일한 키를 사용하지만, 비대칭 암호화는 공개 키와 개인 키라는 두 개의 다른 키를 사용합니다.'
        WHEN 2 THEN 'CSRF(Cross-Site Request Forgery)는 사용자가 인증된 상태에서 의도하지 않은 요청을 서버에 보내도록 속이는 공격입니다.'
        WHEN 3 THEN 'bcrypt는 비밀번호 해싱을 위해 설계된 알고리즘으로, 느린 해시 함수와 솔트를 사용하여 무차별 대입 공격에 강합니다. MD5와 SHA-1은 취약하고, Base64는 인코딩이지 해싱이 아닙니다.'
        WHEN 4 THEN 'HTTPS는 HTTP 프로토콜에 TLS(이전에는 SSL) 암호화 계층을 추가한 것입니다. TLS는 클라이언트와 서버 간의 통신을 암호화합니다.'
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
    CASE WHEN mod(seq, 10) = 7 THEN
             '// 다음 코드는 SQL 인젝션 공격의 취약점을 시뮬레이션하는 예제입니다.
             function executeQuery(query) {
                 // 사용자 입력을 검증하지 않으면 보안 위험이 발생할 수 있습니다.
                 return database.run(query);
             }'
         ELSE NULL END
FROM public.quizzes q
         CROSS JOIN generate_series(0, q.question_count - 1) AS seq
WHERE mod(seq, 30) >= 5 AND mod(seq, 30) < 10
LIMIT 100;