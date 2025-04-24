-- 다양한 CS 주제에 대한 태그 생성
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '자바스크립트', '자바스크립트 프로그래밍 언어, 프레임워크 및 모범 사례'),
    (NOW(), '파이썬', '파이썬 프로그래밍 언어, 라이브러리 및 응용 프로그램'),
    (NOW(), '데이터베이스', '데이터베이스 개념, SQL, NoSQL, 최적화 및 설계'),
    (NOW(), '알고리즘', '알고리즘 설계, 복잡도 분석 및 구현'),
    (NOW(), '자료구조', '일반적인 자료구조, 관련 연산 및 구현'),
    (NOW(), '시스템설계', '아키텍처, 분산 시스템 및 고수준 설계'),
    (NOW(), '네트워크', '네트워킹 프로토콜, 모델 및 인프라'),
    (NOW(), '운영체제', 'OS 개념, 프로세스, 메모리 관리 및 파일 시스템'),
    (NOW(), '웹개발', '웹 기술, 프레임워크 및 디자인 패턴'),
    (NOW(), '데브옵스', '지속적 통합, 배포 및 인프라 코드화'),
    (NOW(), '머신러닝', 'ML 알고리즘, 신경망 및 응용'),
    (NOW(), '보안', '보안 원칙, 취약점 및 모범 사례');

-- 웹개발 하위 태그들
WITH tag_ids AS (
    SELECT id FROM public.tags WHERE name = '웹개발'
)
INSERT INTO public.tags (created_at, name, description, parent_id)
SELECT
    NOW(),
    name,
    description,
    (SELECT id FROM tag_ids)
FROM (
         VALUES
             ('리액트', 'React.js 프론트엔드 라이브러리'),
             ('앵귤러', 'Angular 프레임워크'),
             ('뷰', 'Vue.js 프로그레시브 프레임워크')
     ) as subtags(name, description);

-- 태그 동의어 추가
INSERT INTO public.tag_synonyms (tag_id, synonym)
SELECT id, synonym
FROM public.tags t, unnest(ARRAY[
    CASE WHEN t.name = '자바스크립트' THEN 'JS' WHEN t.name = '파이썬' THEN 'Python' ELSE NULL END,
    CASE WHEN t.name = '자바스크립트' THEN '자스' WHEN t.name = '파이썬' THEN 'py' WHEN t.name = '데이터베이스' THEN 'DB' ELSE NULL END
    ]) as synonym
WHERE synonym IS NOT NULL;

-- Java 관련 태그 생성
INSERT INTO public.tags (created_at, name, description)
SELECT
    NOW(),
    'Java',
    'Java 프로그래밍 언어, 프레임워크, 개념 및 모범 사례'
WHERE NOT EXISTS (
    SELECT 1 FROM public.tags WHERE name = 'Java'
);

-- Java 태그 ID 가져오기 CTE 추가
WITH java_tag AS (
    SELECT id FROM public.tags WHERE name = 'Java'
)
-- Java 태그 동의어 추가
INSERT INTO public.tag_synonyms (tag_id, synonym)
SELECT id, synonym
FROM public.tags t, unnest(ARRAY['자바', 'JAVA', 'java']) as synonym
WHERE t.name = 'Java';