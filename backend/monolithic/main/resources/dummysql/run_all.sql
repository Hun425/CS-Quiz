-- 실행할 때는 순서대로 각 스크립트를 실행하거나,
-- 이 파일을 한번에 실행하기 전에 모든 테이블이 이미 생성되어 있는지 확인하세요.

-- 1. 기존 데이터 초기화 (필요한 경우)
-- TRUNCATE TABLE users, user_levels, user_battle_stats, user_achievements, quiz_attempts, question_attempts,
--              quizzes, questions, tags, quiz_tags, tag_synonyms, battle_rooms, battle_participants, battle_answers,
--              quiz_reviews, quiz_review_comments, user_achievement_history, user_level_history CASCADE;

-- 2. 기본 사용자 데이터 생성
-- users.sql 내용

-- 3. 태그 데이터 생성
-- tags.sql 내용

-- 4. 퀴즈 데이터 생성 (순서대로)
-- daily_quizzes.sql 내용
-- topic_quizzes.sql 내용
-- custom_quizzes.sql 내용
-- java_quizzes.sql 내용

-- 5. 퀴즈-태그 연결
-- quiz_tags.sql 내용

-- 6. 문제 데이터 생성 (다양한 주제)
-- js_questions.sql 내용
-- py_questions.sql 내용
-- algo_questions.sql 내용
-- network_questions.sql 내용
-- web_questions.sql 내용
-- os_questions.sql 내용
-- db_questions.sql 내용
-- security_questions.sql 내용
-- java_basic_questions.sql 내용 (필요한 문제를 더 추가해야 함)