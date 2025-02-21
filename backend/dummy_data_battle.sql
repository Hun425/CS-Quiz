-- 먼저 배틀룸 생성을 위한 기초 데이터를 준비합니다
WITH room_base_data AS (
    -- 퀴즈 ID와 일반 사용자 ID를 미리 가져옵니다
    SELECT
        q.id as quiz_id,
        array_agg(u.id) as user_ids
    FROM public.quizzes q
             CROSS JOIN (
        SELECT id FROM public.users WHERE role = 'USER'
    ) u
    GROUP BY q.id
    LIMIT 3  -- 3개의 배틀룸을 만들기 위한 기초 데이터
    ),
-- 배틀룸 생성
    inserted_battle_rooms AS (
INSERT INTO public.battle_rooms (
    id,
    created_at,
    updated_at,
    room_code,
    status,
    max_participants,
    current_question_index,
    start_time,
    end_time,
    version,
    quiz_id
)
SELECT
    gen_random_uuid(),  -- 룸 ID
    NOW() - interval '1 hour' * rn,  -- 생성 시간
    NOW() - interval '1 hour' * rn + interval '5 minutes',  -- 마지막 업데이트 시간
    'BATTLE_' || LPAD(rn::text, 4, '0'),  -- 룸 코드 (예: BATTLE_0001)
    CASE rn % 3
    WHEN 0 THEN 'WAITING'      -- 대기 중인 방
    WHEN 1 THEN 'IN_PROGRESS'  -- 진행 중인 방
    ELSE 'FINISHED'            -- 종료된 방
    END,
    4,  -- 최대 참가자 수
    CASE rn % 3
    WHEN 0 THEN 0  -- 대기 중인 방은 0번 문제
    WHEN 1 THEN floor(random() * 3 + 1)  -- 진행 중인 방은 1~3번 문제
    ELSE 5  -- 종료된 방은 마지막 문제
    END,
    CASE rn % 3
    WHEN 0 THEN NULL  -- 대기 중인 방은 시작 시간 없음
    ELSE NOW() - interval '1 hour' * rn + interval '1 minute'  -- 시작 시간
    END,
    CASE rn % 3
    WHEN 2 THEN NOW() - interval '1 hour' * rn + interval '30 minutes'  -- 종료된 방만 종료 시간
    ELSE NULL
    END,
    1,  -- 버전
    quiz_id
FROM room_base_data,
    generate_series(1, 3) as rn
    RETURNING *
    ),
-- 배틀 참가자 생성
    inserted_participants AS (
INSERT INTO public.battle_participants (
    id,
    created_at,
    last_activity,
    current_score,
    is_ready,
    is_active,
    current_streak,
    battle_room_id,
    user_id
)
SELECT
    gen_random_uuid(),  -- 참가자 ID
    br.created_at + interval '10 seconds' * participant_num,  -- 참가 시간
    CASE br.status
    WHEN 'WAITING' THEN br.created_at + interval '10 seconds' * participant_num
    WHEN 'IN_PROGRESS' THEN NOW()
    ELSE br.end_time
    END,  -- 마지막 활동 시간
    CASE br.status
    WHEN 'WAITING' THEN 0
    WHEN 'IN_PROGRESS' THEN floor(random() * 200)
    ELSE floor(random() * 500)  -- 종료된 방은 더 높은 점수 가능
    END,  -- 현재 점수
    CASE br.status
    WHEN 'WAITING' THEN random() > 0.5  -- 대기 중인 방은 일부만 ready
    ELSE true  -- 진행/종료된 방은 모두 ready
    END,  -- ready 상태
    br.status != 'FINISHED',  -- active 상태
    CASE br.status
    WHEN 'FINISHED' THEN floor(random() * 5)
    ELSE floor(random() * 3)
    END,  -- 현재 연승
    br.id,  -- 배틀룸 ID
    u.id    -- 사용자 ID
FROM inserted_battle_rooms br
    CROSS JOIN generate_series(1, 3) as participant_num
    CROSS JOIN (
    SELECT id FROM public.users WHERE role = 'USER' ORDER BY random() LIMIT 1
    ) u
    RETURNING *
    ),
-- 배틀 답변 생성
    inserted_battle_answers AS (
INSERT INTO public.battle_answers (
    id,
    created_at,
    answer_time,
    answer,
    is_correct,
    earned_points,
    time_bonus,
    time_taken,
    participant_id,
    question_id
)
SELECT
    gen_random_uuid(),  -- 답변 ID
    p.created_at + interval '1 minute' * answer_num,  -- 답변 생성 시간
    p.created_at + interval '1 minute' * answer_num + interval '10 seconds' * floor(random() * 6),  -- 실제 답변 시간
    CASE floor(random() * 4)  -- 랜덤한 답변 선택
    WHEN 0 THEN 'Option A'
    WHEN 1 THEN 'Option B'
    WHEN 2 THEN 'Option C'
    ELSE 'Option D'
    END,
    random() > 0.4,  -- 60% 확률로 정답
    CASE
    WHEN random() > 0.4 THEN floor(random() * 50) + 50  -- 정답인 경우 50-100점
    ELSE 0  -- 오답인 경우 0점
    END,
    floor(random() * 30),  -- 0-30점의 시간 보너스
    floor(random() * 20) + 5,  -- 5-25초의 응답 시간
    p.id,  -- 참가자 ID
    q.id   -- 문제 ID
FROM inserted_participants p
    CROSS JOIN generate_series(1, 5) as answer_num  -- 각 참가자당 5개의 답변
    CROSS JOIN (
    SELECT id FROM public.questions ORDER BY random() LIMIT 1
    ) q
WHERE EXISTS (
    SELECT 1 FROM inserted_battle_rooms br
    WHERE br.id = p.battle_room_id
  AND br.status != 'WAITING'
    )  -- 대기 중인 방의 참가자는 답변 생성하지 않음
    ),
-- 승자 업데이트
    winners_update AS (
-- 먼저 각 방의 승자를 결정합니다
WITH winner_selection AS (
    SELECT DISTINCT ON (bp.battle_room_id)
    bp.battle_room_id,
    bp.id AS winner_id
    FROM inserted_participants bp
    JOIN inserted_battle_rooms br ON br.id = bp.battle_room_id
    WHERE br.status = 'FINISHED'
    ORDER BY bp.battle_room_id, bp.current_score DESC
    )
-- 승자 정보로 배틀룸을 업데이트합니다
UPDATE public.battle_rooms
SET winner_id = ws.winner_id
FROM winner_selection ws
WHERE battle_rooms.id = ws.battle_room_id
  AND battle_rooms.status = 'FINISHED'
    )
-- 최종 결과 확인
SELECT 'Battle system data insertion completed successfully.' as result;