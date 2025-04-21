-- 데일리 퀴즈 관련 스키마 업데이트

-- QuizAttempt 테이블에 quiz_type 컬럼 추가
ALTER TABLE quiz_attempts 
    ADD COLUMN IF NOT EXISTS quiz_type VARCHAR(20) DEFAULT 'REGULAR';

-- Quiz 테이블 업데이트 (이미 quiz_type 컬럼이 있음을 확인)
-- 기본 값을 REGULAR로 설정
UPDATE quizzes SET quiz_type = 'REGULAR' WHERE quiz_type IS NULL;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_quiz_attempts_user_quiz_type ON quiz_attempts (user_id, quiz_type);
CREATE INDEX IF NOT EXISTS idx_quizzes_quiz_type_valid_until ON quizzes (quiz_type, valid_until);
