-- 퀴즈와 태그 연결
WITH all_quizzes AS (
    SELECT id FROM public.quizzes
),
     tag_info AS (
         SELECT id, name, row_number() OVER (ORDER BY id) - 1 AS tag_index
         FROM public.tags
     )
INSERT INTO public.quiz_tags (quiz_id, tag_id)
-- 모든 퀴즈에 주 태그 배정
SELECT
    q.id AS quiz_id,
    t.id AS tag_id
FROM all_quizzes q
         JOIN tag_info t ON (abs(hashtext(q.id::text)) % (SELECT count(*) FROM tag_info)) = t.tag_index
WHERE NOT EXISTS (
    SELECT 1 FROM public.quiz_tags WHERE quiz_id = q.id
)

UNION ALL

-- 일부 퀴즈에 추가 태그 배정 (25% 확률)
SELECT
    q.id AS quiz_id,
    t.id AS tag_id
FROM public.quizzes q
         CROSS JOIN public.tags t
WHERE random() < 0.25
  AND NOT EXISTS (
    SELECT 1 FROM public.quiz_tags WHERE quiz_id = q.id AND tag_id = t.id
)
LIMIT 30;