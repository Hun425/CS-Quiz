-- 퀴즈와 태그 연결 (안전한 방식)
-- 퀴즈와 태그가 모두 존재하는지 먼저 확인
DO $$
BEGIN
    -- 퀴즈와 태그가 둘 다 존재하는 경우에만 실행
    IF (SELECT COUNT(*) FROM public.quizzes) > 0 AND (SELECT COUNT(*) FROM public.tags) > 0 THEN
        -- 간단한 방식으로 퀴즈와 태그 연결
        INSERT INTO public.quiz_tags (quiz_id, tag_id)
        SELECT 
            q.id AS quiz_id,
            t.id AS tag_id
        FROM (
            SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn 
            FROM public.quizzes
        ) q
        JOIN (
            SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn 
            FROM public.tags
        ) t ON q.rn = t.rn OR (q.rn > (SELECT COUNT(*) FROM public.tags) AND t.rn = 1)
        WHERE NOT EXISTS (
            SELECT 1 FROM public.quiz_tags WHERE quiz_id = q.id AND tag_id = t.id
        );
        
        RAISE NOTICE '퀴즈-태그 연결이 성공적으로 완료되었습니다.';
    ELSE
        RAISE NOTICE '퀴즈 또는 태그가 없어서 연결을 건너뜁니다. 퀴즈: %, 태그: %', 
            (SELECT COUNT(*) FROM public.quizzes), 
            (SELECT COUNT(*) FROM public.tags);
    END IF;
END $$;