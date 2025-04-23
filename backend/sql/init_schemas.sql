-- 각 모듈의 스키마 생성
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS quiz_schema;
CREATE SCHEMA IF NOT EXISTS battle_schema;

-- 스키마 권한 설정
GRANT ALL PRIVILEGES ON SCHEMA user_schema TO quizuser;
GRANT ALL PRIVILEGES ON SCHEMA quiz_schema TO quizuser;
GRANT ALL PRIVILEGES ON SCHEMA battle_schema TO quizuser;

-- 사용자 지정
ALTER ROLE quizuser SET search_path TO user_schema, quiz_schema, battle_schema, public;

-- 기본 스키마 순서 설정
SET search_path TO user_schema, quiz_schema, battle_schema, public; 