-- 새로운 더미 데이터 추가 (클라우드 컴퓨팅 기초)

-- 1. 필요한 태그 추가 (클라우드 컴퓨팅)
INSERT INTO public.tags (created_at, name, description)
VALUES
    (NOW(), '클라우드 컴퓨팅', '클라우드 컴퓨팅 기본 개념, 서비스 모델(IaaS, PaaS, SaaS), 배포 모델, 주요 클라우드 서비스(AWS, GCP, Azure)')
ON CONFLICT (name) DO NOTHING; -- 이미 존재하면 추가하지 않음

-- 2. 새로운 퀴즈 추가 (클라우드 컴퓨팅 기초 1개 퀴즈)

-- 관리자 ID 및 클라우드 컴퓨팅 태그 ID 가져오기
WITH admin_user AS (
    SELECT id FROM public.users WHERE role = 'ADMIN' LIMIT 1
), cloud_tag AS (
    SELECT id FROM public.tags WHERE name = '클라우드 컴퓨팅'
),
-- 클라우드 컴퓨팅 퀴즈 생성
     inserted_cloud_quiz AS (
         INSERT INTO public.quizzes (
                                     created_at, updated_at, title, description, difficulty_level,
                                     is_public, question_count, quiz_type, time_limit,
                                     creator_id, attempt_count, avg_score, view_count, valid_until
             )
             VALUES
                 -- 클라우드 컴퓨팅 기초 (중급)
                 (NOW(), NOW(), '클라우드 컴퓨팅 핵심 개념 이해', '클라우드의 기본 개념, 서비스 모델, 배포 모델 및 주요 서비스들의 역할을 학습합니다.', 'INTERMEDIATE',
                  true, 10, 'REGULAR', 35, (SELECT id FROM admin_user), floor(random() * 60 + 15), random() * 28 + 65, floor(random() * 350 + 45), NULL)
             RETURNING id, title -- 생성된 퀴즈 ID와 제목 반환
     ),
-- 생성된 퀴즈와 태그 연결
     quiz_tag_linking_cloud AS (
         INSERT INTO public.quiz_tags (quiz_id, tag_id)
             SELECT iq.id, ct.id FROM inserted_cloud_quiz iq, cloud_tag ct WHERE iq.title LIKE '클라우드 컴퓨팅%'
     )
-- 최종 SELECT 문 (결과 표시용, 실제 작업에는 영향 없음)
SELECT 'Cloud Computing Quiz and Tag inserted/linked successfully';


-- 3. 새로운 질문 추가 (클라우드 컴퓨팅 기초 퀴즈 10개)

-- 클라우드 컴퓨팅 기초 (중급) 질문 10개
WITH quiz_info AS (
    SELECT id FROM public.quizzes WHERE title = '클라우드 컴퓨팅 핵심 개념 이해' LIMIT 1
)
INSERT INTO public.questions (
    created_at, updated_at, question_text, question_type, difficulty_level,
    correct_answer, options, explanation, points, time_limit_seconds, quiz_id
)
VALUES
    (NOW(), NOW(), '클라우드 컴퓨팅의 주요 특징(장점)으로 거리가 먼 것은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '초기 높은 고정 비용 투자', '["주요 특징(장점)으로 거리가 먼 것은?", "사용한 만큼만 비용 지불 (Pay-as-you-go)", "빠른 확장성 및 탄력성 (Scalability & Elasticity)", "초기 높은 고정 비용 투자"]'::jsonb, '클라우드 컴퓨팅은 필요에 따라 리소스를 할당받고 사용한 만큼만 비용을 지불하므로, 초기 대규모 인프라 투자 비용을 줄일 수 있다는 장점이 있습니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클라우드 서비스 모델 중, 가상 서버, 스토리지, 네트워크 등 IT 인프라 자원을 제공하는 모델은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'IaaS (Infrastructure as a Service)', '["SaaS (Software as a Service)", "PaaS (Platform as a Service)", "IaaS (Infrastructure as a Service)", "FaaS (Function as a Service)"]'::jsonb, 'IaaS는 사용자가 직접 OS, 미들웨어, 애플리케이션을 관리해야 하지만, 인프라 자체는 클라우드 제공업체가 관리하는 모델입니다. (예: AWS EC2, GCP Compute Engine)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클라우드 서비스 모델 중, 애플리케이션 개발 및 실행 환경(플랫폼)을 제공하여 사용자가 인프라 관리에 신경 쓰지 않고 개발에 집중할 수 있게 하는 모델은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'PaaS (Platform as a Service)', '["SaaS", "PaaS", "IaaS", "DaaS (Desktop as a Service)"]'::jsonb, 'PaaS는 OS, 미들웨어, 런타임 등을 클라우드 제공업체가 관리하며, 사용자는 애플리케이션 코드와 데이터만 관리하면 됩니다. (예: Heroku, AWS Elastic Beanstalk, GCP App Engine)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클라우드 서비스 모델 중, 소프트웨어 애플리케이션 자체를 인터넷을 통해 제공하는 모델은? (예: Google Workspace, Microsoft 365)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'SaaS (Software as a Service)', '["SaaS", "PaaS", "IaaS", "CaaS (Container as a Service)"]'::jsonb, 'SaaS는 사용자가 별도의 설치나 관리 없이 웹 브라우저 등을 통해 바로 소프트웨어를 사용할 수 있는 모델입니다. 인프라, 플랫폼, 소프트웨어 모두 제공업체가 관리합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클라우드 배포 모델 중, 특정 기업이나 조직만이 전용으로 사용하는 클라우드 환경은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '프라이빗 클라우드 (Private Cloud)', '["퍼블릭 클라우드 (Public Cloud)", "프라이빗 클라우드 (Private Cloud)", "하이브리드 클라우드 (Hybrid Cloud)", "커뮤니티 클라우드 (Community Cloud)"]'::jsonb, '프라이빗 클라우드는 기업 내부 데이터 센터에 구축하거나 특정 기업 전용으로 호스팅되어 보안 및 통제 수준이 높지만, 구축 및 운영 비용이 발생합니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'AWS(Amazon Web Services)에서 제공하는 가상 서버(컴퓨팅 인스턴스) 서비스의 이름은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'EC2 (Elastic Compute Cloud)', '["S3 (Simple Storage Service)", "RDS (Relational Database Service)", "EC2 (Elastic Compute Cloud)", "Lambda"]'::jsonb, 'EC2는 사용자가 필요에 따라 다양한 사양의 가상 서버를 생성하고 관리할 수 있는 AWS의 대표적인 IaaS 서비스입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), 'AWS(Amazon Web Services)에서 객체 스토리지(파일 저장) 서비스를 제공하는 서비스의 이름은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'S3 (Simple Storage Service)', '["EBS (Elastic Block Store)", "S3 (Simple Storage Service)", "Glacier", "DynamoDB"]'::jsonb, 'S3는 확장성, 내구성, 가용성이 뛰어난 객체 스토리지 서비스로, 웹사이트 호스팅, 데이터 백업, 빅데이터 분석 등 다양한 용도로 사용됩니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '물리적인 서버를 여러 개의 가상 머신(VM)으로 나누어 사용하는 핵심 기술은 무엇인가?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '가상화 (Virtualization)', '["컨테이너화 (Containerization)", "오케스트레이션 (Orchestration)", "가상화 (Virtualization)", "로드 밸런싱 (Load Balancing)"]'::jsonb, '가상화 기술(하이퍼바이저 사용)을 통해 하나의 물리적 하드웨어 리소스를 여러 논리적인 단위(VM)로 분할하여 효율적으로 사용할 수 있으며, 이는 클라우드 컴퓨팅의 기반 기술입니다.', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '서버 관리 없이 코드를 실행할 수 있게 하는 클라우드 컴퓨팅 모델로, 이벤트 발생 시 코드가 실행되고 실행된 시간만큼만 비용을 지불하는 방식은?', 'MULTIPLE_CHOICE', 'INTERMEDIATE', '서버리스 컴퓨팅 (Serverless Computing)', '["서버리스 컴퓨팅 (Serverless Computing)", "엣지 컴퓨팅 (Edge Computing)", "그리드 컴퓨팅 (Grid Computing)", "클라이언트-서버 컴퓨팅"]'::jsonb, '서버리스(Serverless)는 개발자가 서버 인프라 관리에 대한 걱정 없이 애플리케이션 로직 개발에만 집중할 수 있게 해주는 모델입니다. FaaS(Function as a Service)가 대표적인 예입니다. (예: AWS Lambda, GCP Cloud Functions, Azure Functions)', 10, 45, (SELECT id FROM quiz_info)),
    (NOW(), NOW(), '클라우드 환경에서 사용자 인증 및 리소스 접근 권한을 안전하게 관리하는 서비스의 통칭은? (AWS, GCP, Azure 모두 유사한 서비스 제공)', 'MULTIPLE_CHOICE', 'INTERMEDIATE', 'IAM (Identity and Access Management)', '["VPC (Virtual Private Cloud)", "CDN (Content Delivery Network)", "IAM (Identity and Access Management)", "KMS (Key Management Service)"]'::jsonb, 'IAM은 클라우드 리소스에 접근할 수 있는 사용자(Identity)와 그 사용자가 수행할 수 있는 작업(Access/Permission)을 정의하고 관리하여 보안을 강화하는 필수적인 서비스입니다.', 10, 45, (SELECT id FROM quiz_info));