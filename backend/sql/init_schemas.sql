-- CS-Quiz 플랫폼 스키마 초기화 스크립트

-- 확장 프로그램 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 사용자 모듈 스키마
CREATE SCHEMA IF NOT EXISTS user_schema;

-- 퀴즈 모듈 스키마  
CREATE SCHEMA IF NOT EXISTS quiz_schema;

-- 배틀 모듈 스키마
CREATE SCHEMA IF NOT EXISTS battle_schema;

-- 사용자에게 스키마 권한 부여
GRANT ALL ON SCHEMA user_schema TO quizuser;
GRANT ALL ON SCHEMA quiz_schema TO quizuser;
GRANT ALL ON SCHEMA battle_schema TO quizuser;

-- 기본 검색 경로 설정
ALTER DATABASE quiz_platform SET search_path TO user_schema, quiz_schema, battle_schema, public;

-- 각 스키마에 대한 기본 권한 설정
GRANT ALL ON ALL TABLES IN SCHEMA user_schema TO quizuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA user_schema TO quizuser;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA user_schema TO quizuser;

GRANT ALL ON ALL TABLES IN SCHEMA quiz_schema TO quizuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA quiz_schema TO quizuser;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA quiz_schema TO quizuser;

GRANT ALL ON ALL TABLES IN SCHEMA battle_schema TO quizuser;
GRANT ALL ON ALL SEQUENCES IN SCHEMA battle_schema TO quizuser;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA battle_schema TO quizuser;

-- 향후 생성될 객체에 대한 기본 권한 설정
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL ON TABLES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL ON SEQUENCES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL ON FUNCTIONS TO quizuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA quiz_schema GRANT ALL ON TABLES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA quiz_schema GRANT ALL ON SEQUENCES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA quiz_schema GRANT ALL ON FUNCTIONS TO quizuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA battle_schema GRANT ALL ON TABLES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA battle_schema GRANT ALL ON SEQUENCES TO quizuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA battle_schema GRANT ALL ON FUNCTIONS TO quizuser; 