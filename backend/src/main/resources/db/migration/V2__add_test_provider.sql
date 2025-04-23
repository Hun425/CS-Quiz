-- 기존 제약조건 삭제
ALTER TABLE users DROP CONSTRAINT users_provider_check;

-- 새로운 제약조건 추가 (TEST 포함)
ALTER TABLE users ADD CONSTRAINT users_provider_check 
    CHECK (provider IN ('GOOGLE', 'GITHUB', 'KAKAO', 'TEST'));
