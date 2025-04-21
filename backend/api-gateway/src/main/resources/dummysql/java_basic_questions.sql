-- Java 기초 면접 질문 (초급)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds,
    quiz_id, code_snippet
)
WITH quiz_ids AS (
    SELECT id FROM public.quizzes
    WHERE difficulty_level = 'BEGINNER' AND title LIKE '자바 기초%'
)
SELECT
    NOW() - (random() * INTERVAL '60 days'),
    NOW() - (random() * INTERVAL '30 days'),
    questions.question,
    'MULTIPLE_CHOICE',
    'BEGINNER',
    questions.answer,
    questions.options,
    questions.explanation,
    5, -- 초급 문제 5점
    45, -- 45초 제한시간
    (SELECT id FROM quiz_ids ORDER BY random() LIMIT 1),
    questions.code
FROM (
         VALUES
             ('JVM이란 무엇이며 어떤 역할을 하는가?',
              'Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다',
              '["Java Virtual Machine으로, 자바 바이트코드를 실행하는 가상 머신이다", "Java Variable Method로, 변수와 메소드를 관리하는 시스템이다", "Java Visual Monitor로, 화면 출력을 담당하는 시스템이다", "Java Version Manager로, 자바 버전을 관리하는 도구이다"]'::jsonb,
              'JVM(Java Virtual Machine)은 자바 바이트코드(.class 파일)를 각 운영체제에 맞게 해석하고 실행하는 가상 머신입니다. 덕분에 Java는 "Write Once, Run Anywhere"라는 특징을 갖습니다.',
              NULL),
             -- 추가 질문은 원본 SQL에서 복사해서 계속 추가할 수 있습니다
             ('자바의 기본 데이터 타입(Primitive Type)이 아닌 것은?',
              'String',
              '["int", "char", "boolean", "String"]'::jsonb,
              'String은 기본 데이터 타입이 아닌 참조 타입(Reference Type)입니다. 자바의 기본 데이터 타입은 byte, short, int, long, float, double, char, boolean의 8가지입니다.',
              NULL)
     ) AS questions(question, answer, options, explanation, code);